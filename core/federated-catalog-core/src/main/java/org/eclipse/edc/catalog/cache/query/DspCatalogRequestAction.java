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
import org.eclipse.edc.catalog.spi.CatalogRequestMessage;
import org.eclipse.edc.catalog.spi.model.CatalogUpdateResponse;
import org.eclipse.edc.crawler.spi.CrawlerAction;
import org.eclipse.edc.crawler.spi.model.UpdateRequest;
import org.eclipse.edc.crawler.spi.model.UpdateResponse;
import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import java.util.concurrent.CompletableFuture;

public class DspCatalogRequestAction implements CrawlerAction {
    private static final int INITIAL_OFFSET = 0;
    private static final int BATCH_SIZE = 100;
    private final BatchedCatalogRequestFetcher fetcher;

    public DspCatalogRequestAction(RemoteMessageDispatcherRegistry dispatcherRegistry, Monitor monitor, ObjectMapper objectMapper, TypeTransformerRegistry transformerRegistry, JsonLd jsonLdService) {
        fetcher = new BatchedCatalogRequestFetcher(dispatcherRegistry, monitor, objectMapper, transformerRegistry, jsonLdService);
    }

    @Override
    public CompletableFuture<UpdateResponse> apply(UpdateRequest request) {
        var catalogRequest = CatalogRequestMessage.Builder.newInstance()
                .protocol(CatalogConstants.DATASPACE_PROTOCOL)
                .counterPartyAddress(request.nodeUrl())
                .counterPartyId(request.nodeId())
                .build();
        var catalogFuture = fetcher.fetch(catalogRequest, INITIAL_OFFSET, BATCH_SIZE);

        return catalogFuture.thenApply(catalog -> new CatalogUpdateResponse(request.nodeUrl(), catalog));
    }
}
