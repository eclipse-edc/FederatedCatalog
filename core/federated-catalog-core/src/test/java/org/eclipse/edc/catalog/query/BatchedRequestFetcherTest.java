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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonObject;
import org.eclipse.edc.catalog.cache.query.BatchedRequestFetcher;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogConstants;
import org.eclipse.edc.catalog.spi.CatalogRequestMessage;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.message.Range;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.jsonld.util.JacksonJsonLd.createObjectMapper;
import static org.eclipse.edc.spi.result.Result.success;
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
    private TypeTransformerRegistry transformerRegistry;
    private ObjectMapper objectMapper;
    private TitaniumJsonLd jsonLdService;

    @BeforeEach
    void setup() {
        dispatcherMock = mock(RemoteMessageDispatcherRegistry.class);
        transformerRegistry = mock(TypeTransformerRegistry.class);
        objectMapper = createObjectMapper();
        jsonLdService = new TitaniumJsonLd(mock(Monitor.class));
        fetcher = new BatchedRequestFetcher(dispatcherMock, mock(Monitor.class), objectMapper, transformerRegistry, jsonLdService);
    }

    @Test
    void fetchAll() throws JsonProcessingException {
        var cat1 = createCatalog(5);
        var cat2 = createCatalog(5);
        var cat3 = createCatalog(3);
        when(dispatcherMock.send(eq(byte[].class), any(CatalogRequestMessage.class)))
                .thenReturn(completedFuture(toBytes(cat1)))
                .thenReturn(completedFuture(toBytes(cat2)))
                .thenReturn(completedFuture(toBytes(cat3)))
                .thenReturn(completedFuture(toBytes(emptyCatalog())));

        when(transformerRegistry.transform(any(JsonObject.class), eq(Catalog.class)))
                .thenReturn(success(cat1))
                .thenReturn(success(cat2))
                .thenReturn(success(cat3));

        var request = createRequest();

        var offers = fetcher.fetch(request, 0, 5);
        assertThat(offers).isCompletedWithValueMatching(list -> list.getContractOffers().size() == 13 &&
                list.getContractOffers().stream().allMatch(o -> o.getId().matches("(id)\\d|1[0-3]")));


        var captor = forClass(CatalogRequestMessage.class);
        verify(dispatcherMock, times(3)).send(eq(byte[].class), captor.capture());

        // verify the sequence of requests
        assertThat(captor.getAllValues())
                .extracting(l -> l.getQuerySpec().getRange())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new Range(0, 5), new Range(5, 10), new Range(10, 15));
    }

    private byte[] toBytes(Catalog cat1) throws JsonProcessingException {
        var json = objectMapper.writeValueAsString(cat1);
        var jo = objectMapper.readValue(json, JsonObject.class);
        var expanded = jsonLdService.expand(jo);
        var expandedStr = objectMapper.writeValueAsString(expanded);
        return expandedStr.getBytes();
    }

    private CatalogRequestMessage createRequest() {
        return CatalogRequestMessage.Builder.newInstance()
                .counterPartyAddress("test-address")
                .protocol(CatalogConstants.DATASPACE_PROTOCOL)
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
                        .build())
                .collect(Collectors.toList());

        return Catalog.Builder.newInstance().id("catalog").contractOffers(contractOffers).build();
    }
}
