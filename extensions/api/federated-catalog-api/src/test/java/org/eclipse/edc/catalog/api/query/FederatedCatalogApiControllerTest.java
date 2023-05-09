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

package org.eclipse.edc.catalog.api.query;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.eclipse.edc.catalog.spi.CacheQueryAdapter;
import org.eclipse.edc.catalog.spi.CacheQueryAdapterRegistry;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.FederatedCacheStore;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.catalog.test.TestUtil.createCatalog;
import static org.eclipse.edc.junit.testfixtures.TestUtils.getFreePort;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ApiTest
@ExtendWith(EdcExtension.class)
class FederatedCatalogApiControllerTest {
    private static final String BASE_PATH = "/api";
    private static final TypeRef<List<Catalog>> CONTRACT_OFFER_LIST_TYPE = new TypeRef<>() {
    };
    private final int port = getFreePort();

    @BeforeEach
    void setUp(EdcExtension extension) {
        extension.setConfiguration(Map.of(
                "web.http.port", String.valueOf(port),
                "web.http.path", BASE_PATH
        ));
    }

    @Test
    void queryApi_whenEmptyResult() {
        var response = baseRequest()
                .contentType(ContentType.JSON)
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post("/federatedcatalog")
                .then()
                .statusCode(200)
                .extract()
                .as(CONTRACT_OFFER_LIST_TYPE);

        assertThat(response).isEmpty();
    }

    @Test
    void queryApi_whenResultsReturned(FederatedCacheStore store) {
        int numberEntries = 3;

        // generate entries and populate the store
        List<Catalog> entries = new ArrayList<>();
        for (int i = 0; i < numberEntries; i++) {
            entries.add(createCatalog("some-offer-" + i));
        }
        entries.forEach(store::save);

        var offers = baseRequest()
                .contentType(ContentType.JSON)
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post("/federatedcatalog")
                .then()
                .statusCode(200)
                .extract()
                .as(CONTRACT_OFFER_LIST_TYPE);

        // test
        compareByAssetId(offers, entries);
    }

    @Test
    void queryApi_whenQueryUnsuccessful(CacheQueryAdapterRegistry adapterRegistry) {
        var adapter = mock(CacheQueryAdapter.class);
        when(adapter.executeQuery(any())).thenThrow(new RuntimeException("test exception"));
        when(adapter.canExecute(any())).thenReturn(true);
        adapterRegistry.register(adapter);

        baseRequest()
                .contentType(ContentType.JSON)
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post("/federatedcatalog")
                .then()
                .statusCode(500);

    }

    private void compareByAssetId(List<Catalog> actual, List<Catalog> expected) {
        var actualAssetIds = actual.stream().flatMap(e -> e.getContractOffers().stream()).map(ContractOffer::getAssetId).collect(toList());
        var expectedAssetIds = expected.stream().flatMap(e -> e.getContractOffers().stream()).map(ContractOffer::getAssetId).collect(toList());
        assertThat(actualAssetIds).isEqualTo(expectedAssetIds);
    }

    private RequestSpecification baseRequest() {
        return given()
                .baseUri("http://localhost:" + port)
                .basePath(BASE_PATH)
                .when();
    }
}