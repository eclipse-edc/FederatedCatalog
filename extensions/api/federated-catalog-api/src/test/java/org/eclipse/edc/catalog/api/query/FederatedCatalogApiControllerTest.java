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

import io.restassured.specification.RequestSpecification;
import org.eclipse.edc.catalog.spi.CacheQueryAdapter;
import org.eclipse.edc.catalog.spi.CacheQueryAdapterRegistry;
import org.eclipse.edc.catalog.spi.FederatedCacheStore;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.util.stream.IntStream.range;
import static org.eclipse.edc.catalog.test.TestUtil.createCatalog;
import static org.eclipse.edc.junit.testfixtures.TestUtils.getFreePort;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ApiTest
@ExtendWith(EdcExtension.class)
class FederatedCatalogApiControllerTest {
    private static final String BASE_PATH = "/api";
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
        baseRequest()
                .contentType(JSON)
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post("/federatedcatalog")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("size()", is(0));
    }

    @Test
    void queryApi_whenResultsReturned(FederatedCacheStore store) {
        range(0, 3).mapToObj(i -> createCatalog("some-offer-" + i)).forEach(store::save);

        baseRequest()
                .contentType(JSON)
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post("/federatedcatalog")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("size()", is(3));
    }

    @Test
    void queryApi_whenQueryUnsuccessful(CacheQueryAdapterRegistry adapterRegistry) {
        var adapter = mock(CacheQueryAdapter.class);
        when(adapter.executeQuery(any())).thenThrow(new RuntimeException("test exception"));
        when(adapter.canExecute(any())).thenReturn(true);
        adapterRegistry.register(adapter);

        baseRequest()
                .contentType(JSON)
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post("/federatedcatalog")
                .then()
                .statusCode(500);
    }

    private RequestSpecification baseRequest() {
        return given()
                .baseUri("http://localhost:" + port)
                .basePath(BASE_PATH)
                .when();
    }
}
