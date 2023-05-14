/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.catalog.cache.query;

import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogRequestMessage;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.spi.message.Range;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.QuerySpec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

/**
 * Helper class that runs through a loop and sends {@link CatalogRequestMessage}s until no more {@link ContractOffer}s are
 * received. This is useful to avoid overloading the provider connector by chunking the resulting response payload
 * size.
 */
public class BatchedRequestFetcher {
    private final RemoteMessageDispatcherRegistry dispatcherRegistry;
    private final Monitor monitor;

    public BatchedRequestFetcher(RemoteMessageDispatcherRegistry dispatcherRegistry, Monitor monitor) {
        this.dispatcherRegistry = dispatcherRegistry;
        this.monitor = monitor;
    }

    private static Catalog copyCatalogWithoutNulls(Catalog catalog) {
        return Catalog.Builder.newInstance().id(catalog.getId())
                .contractOffers(catalog.getContractOffers())
                .properties(new HashMap<>())
                .dataServices(new ArrayList<>())
                .datasets(new ArrayList<>())
                .build();
    }


    /**
     * Gets all contract offers. Requests are split in digestible chunks to match {@code batchSize} until no more offers
     * can be obtained.
     *
     * @param catalogRequest The catalog request. This will be copied for every request.
     * @param from           The (zero-based) index of the first item
     * @param batchSize      The size of one batch
     * @return A list of {@link ContractOffer} objects
     */
    public @NotNull CompletableFuture<Catalog> fetch(CatalogRequestMessage catalogRequest, int from, int batchSize) {

        var range = new Range(from, from + batchSize);
        var rq = catalogRequest.toBuilder().querySpec(QuerySpec.Builder.newInstance().range(range).build()).build();

        return dispatcherRegistry.send(Catalog.class, rq)
                .thenCompose(catalog -> CompletableFuture.completedFuture(copyCatalogWithoutNulls(catalog)))
                .thenCompose(catalog -> {
                    var offers = catalog.getContractOffers();
                    if (offers.size() >= batchSize) {
                        monitor.debug(format("Fetching next batch from %s to %s", from, from + batchSize));
                        return fetch(rq, range.getFrom() + batchSize, batchSize)
                                .thenApply(o -> concat(catalog, o));
                    } else {
                        return CompletableFuture.completedFuture(catalog);
                    }
                });
    }

    private QuerySpec forRange(Range range) {
        return QuerySpec.Builder.newInstance().range(range).build();
    }

    private Catalog concat(Catalog target, Catalog source) {
        target.getContractOffers().addAll(source.getContractOffers());
        return target;
    }

    private List<ContractOffer> concat(List<ContractOffer> list1, List<ContractOffer> list2) {
        list1.addAll(list2);
        return list1;
    }
}
