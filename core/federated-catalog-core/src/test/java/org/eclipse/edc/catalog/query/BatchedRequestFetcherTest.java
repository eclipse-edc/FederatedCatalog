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

package org.eclipse.edc.catalog.query;

import org.eclipse.edc.catalog.cache.query.BatchedRequestFetcher;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogRequestMessage;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.message.Range;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchedRequestFetcherTest {

    private BatchedRequestFetcher fetcher;
    private RemoteMessageDispatcherRegistry dispatcherMock;

    @BeforeEach
    void setup() {
        dispatcherMock = mock(RemoteMessageDispatcherRegistry.class);
        fetcher = new BatchedRequestFetcher(dispatcherMock, mock(Monitor.class));
    }

    @Test
    void fetchAll() {
        when(dispatcherMock.send(eq(Catalog.class), any(CatalogRequestMessage.class)))
                .thenReturn(completedFuture(createCatalog(5)))
                .thenReturn(completedFuture(createCatalog(5)))
                .thenReturn(completedFuture(createCatalog(3)))
                .thenReturn(completedFuture(emptyCatalog()));

        var request = createRequest();

        var offers = fetcher.fetch(request, 0, 5);
        assertThat(offers).isCompletedWithValueMatching(list -> list.size() == 13 &&
                list.stream().allMatch(o -> o.getId().matches("(id)\\d|1[0-3]")));


        var captor = forClass(CatalogRequestMessage.class);
        verify(dispatcherMock, times(4)).send(eq(Catalog.class), captor.capture());

        // verify the sequence of requests
        assertThat(captor.getAllValues())
                .extracting(l -> l.getQuerySpec().getRange())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new Range(0, 5), new Range(5, 10), new Range(10, 15), new Range(15, 20));
    }

    private CatalogRequestMessage createRequest() {
        return CatalogRequestMessage.Builder.newInstance()
                .connectorId("test-connector")
                .callbackAddress("test-address")
                .protocol("ids-multipart")
                .build();
    }

    private Catalog emptyCatalog() {
        return Catalog.Builder.newInstance().id("id").contractOffers(Collections.emptyList()).build();
    }

    private Catalog createCatalog(int howManyOffers) {
        var contractOffers = IntStream.range(0, howManyOffers)
                .mapToObj(i -> ContractOffer.Builder.newInstance()
                        .id("id" + i)
                        .policy(Policy.Builder.newInstance().build())
                        .assetId("asset" + i)
                        .contractStart(ZonedDateTime.now())
                        .contractEnd(ZonedDateTime.now().plus(365, ChronoUnit.DAYS))
                        .build())
                .collect(Collectors.toList());

        return Catalog.Builder.newInstance().id("catalog").contractOffers(contractOffers).build();
    }
}
