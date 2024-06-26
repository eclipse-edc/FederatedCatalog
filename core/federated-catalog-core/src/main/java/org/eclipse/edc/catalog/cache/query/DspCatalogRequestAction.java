/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.catalog.cache.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.catalog.spi.CatalogConstants;
import org.eclipse.edc.catalog.spi.model.CatalogUpdateResponse;
import org.eclipse.edc.connector.controlplane.catalog.spi.Catalog;
import org.eclipse.edc.connector.controlplane.catalog.spi.CatalogRequestMessage;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataService;
import org.eclipse.edc.connector.controlplane.catalog.spi.Dataset;
import org.eclipse.edc.crawler.spi.CrawlerAction;
import org.eclipse.edc.crawler.spi.model.UpdateRequest;
import org.eclipse.edc.crawler.spi.model.UpdateResponse;
import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class DspCatalogRequestAction implements CrawlerAction {
    private static final int INITIAL_OFFSET = 0;
    private static final int BATCH_SIZE = 100;
    private final PagingCatalogRequestFetcher fetcher;
    private final boolean preserveHierarchy;

    public DspCatalogRequestAction(RemoteMessageDispatcherRegistry dispatcherRegistry, Monitor monitor, ObjectMapper objectMapper, TypeTransformerRegistry transformerRegistry, JsonLd jsonLdService) {
        this(dispatcherRegistry, monitor, objectMapper, transformerRegistry, jsonLdService, true);
    }

    public DspCatalogRequestAction(RemoteMessageDispatcherRegistry dispatcherRegistry, Monitor monitor, ObjectMapper objectMapper, TypeTransformerRegistry transformerRegistry, JsonLd jsonLdService, boolean preserveHierarchy) {
        fetcher = new PagingCatalogRequestFetcher(dispatcherRegistry, monitor, objectMapper, transformerRegistry, jsonLdService);
        this.preserveHierarchy = preserveHierarchy;
    }

    @Override
    public CompletableFuture<UpdateResponse> apply(UpdateRequest request) {
        var catalogRequest = createCatalogRequest(request);
        var catalogFuture = fetcher.fetch(catalogRequest, INITIAL_OFFSET, BATCH_SIZE);

        return catalogFuture
                .thenCompose(catalog -> expandCatalog(catalog, preserveHierarchy))
                .thenApply(catalog -> new CatalogUpdateResponse(request.nodeUrl(), catalog));
    }

    private CatalogRequestMessage createCatalogRequest(UpdateRequest request) {
        return CatalogRequestMessage.Builder.newInstance()
                .protocol(CatalogConstants.DATASPACE_PROTOCOL)
                .counterPartyAddress(request.nodeUrl())
                .counterPartyId(request.nodeId())
                .build();
    }

    private CompletableFuture<Catalog> expandCatalog(Catalog rootCatalog, boolean preserveHierarchy) {
        var partitions = rootCatalog.getDatasets().stream().collect(Collectors.groupingBy(Dataset::getClass));

        var datasets = partitions.get(Dataset.class);
        var subCatalogs = partitions.get(Catalog.class);

        if (subCatalogs == null || subCatalogs.isEmpty()) {
            return completedFuture(rootCatalog);
        }

        var expandedSubCatalogs = subCatalogs.stream()
                .map(ds -> (Catalog) ds)
                .map(subCatalog -> subCatalog.getDataServices().stream()
                        .map(DataService::getEndpointUrl)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .map(url -> {
                            var id = ofNullable(subCatalog.getParticipantId()).orElseGet(rootCatalog::getParticipantId);
                            return new UpdateRequest(id, url);
                        })
                        .orElse(null))
                .filter(Objects::nonNull)
                .map(this) //recursively call this.apply()
                .map(CompletableFuture::join)
                .map(ur -> (CatalogUpdateResponse) ur)
                .map(CatalogUpdateResponse::getCatalog);

        if (preserveHierarchy) {
            expandedSubCatalogs.forEach(datasets::add);
            return completedFuture(CatalogUtil.copyCatalog(rootCatalog, datasets).build());
        } else {
            var catalogCopy = CatalogUtil.copyCatalog(rootCatalog, datasets).build();
            var mergedSubCatalog = expandedSubCatalogs.reduce(CatalogUtil::merge)
                    .orElse(null);

            if (mergedSubCatalog != null) {
                return completedFuture(CatalogUtil.merge(catalogCopy, mergedSubCatalog));
            }

            return completedFuture(rootCatalog);
        }


    }

}
