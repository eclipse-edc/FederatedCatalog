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
import jakarta.json.Json;
import org.eclipse.edc.catalog.cache.query.CacheQueryAdapterImpl;
import org.eclipse.edc.catalog.cache.query.CacheQueryAdapterRegistryImpl;
import org.eclipse.edc.catalog.cache.query.QueryEngineImpl;
import org.eclipse.edc.catalog.spi.CacheQueryAdapter;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.catalog.store.InMemoryFederatedCacheStore;
import org.eclipse.edc.catalog.transform.JsonObjectToCatalogTransformer;
import org.eclipse.edc.catalog.transform.JsonObjectToDataServiceTransformer;
import org.eclipse.edc.catalog.transform.JsonObjectToDatasetTransformer;
import org.eclipse.edc.catalog.transform.JsonObjectToDistributionTransformer;
import org.eclipse.edc.connector.core.agent.NoOpParticipantIdMapper;
import org.eclipse.edc.jsonld.util.JacksonJsonLd;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.protocol.dsp.catalog.transform.from.JsonObjectFromCatalogTransformer;
import org.eclipse.edc.protocol.dsp.catalog.transform.from.JsonObjectFromDataServiceTransformer;
import org.eclipse.edc.protocol.dsp.catalog.transform.from.JsonObjectFromDatasetTransformer;
import org.eclipse.edc.protocol.dsp.catalog.transform.from.JsonObjectFromDistributionTransformer;
import org.eclipse.edc.query.CriterionOperatorRegistryImpl;
import org.eclipse.edc.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.util.concurrency.LockManager;
import org.eclipse.edc.web.jersey.testfixtures.RestControllerTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.util.stream.IntStream.range;
import static org.eclipse.edc.catalog.test.TestUtil.createCatalog;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ApiTest
class FederatedCatalogApiControllerTest extends RestControllerTestBase {
    private final CacheQueryAdapterRegistryImpl cacheQueryAdapterRegistry = new CacheQueryAdapterRegistryImpl();
    private InMemoryFederatedCacheStore store;

    @BeforeEach
    void setup() {
        store = new InMemoryFederatedCacheStore(new LockManager(new ReentrantReadWriteLock()), CriterionOperatorRegistryImpl.ofDefaults());
        var adapter = new CacheQueryAdapterImpl(store);
        cacheQueryAdapterRegistry.register(adapter);
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
    void queryApi_whenResultsReturned() {
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
    void queryApi_whenQueryUnsuccessful() {
        var adapter = mock(CacheQueryAdapter.class);
        when(adapter.executeQuery(any())).thenThrow(new RuntimeException("test exception"));
        when(adapter.canExecute(any())).thenReturn(true);
        cacheQueryAdapterRegistry.register(adapter);

        baseRequest()
                .contentType(JSON)
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post("/federatedcatalog")
                .then()
                .statusCode(500);
    }

    @Override
    protected Object controller() {
        var typeTransformerRegistry = new TypeTransformerRegistryImpl();
        var factory = Json.createBuilderFactory(Map.of());
        var mapper = JacksonJsonLd.createObjectMapper();
        typeTransformerRegistry.register(new JsonObjectToCatalogTransformer());
        typeTransformerRegistry.register(new JsonObjectFromDatasetTransformer(factory, mapper));
        typeTransformerRegistry.register(new JsonObjectFromDistributionTransformer(factory));
        typeTransformerRegistry.register(new JsonObjectFromDataServiceTransformer(factory));
        typeTransformerRegistry.register(new JsonObjectFromCatalogTransformer(factory, mapper, new NoOpParticipantIdMapper()));
        typeTransformerRegistry.register(new JsonObjectToDatasetTransformer());
        typeTransformerRegistry.register(new JsonObjectToDataServiceTransformer());
        typeTransformerRegistry.register(new JsonObjectToDistributionTransformer());
        return new FederatedCatalogApiController(new QueryEngineImpl(cacheQueryAdapterRegistry), typeTransformerRegistry);
    }

    private RequestSpecification baseRequest() {
        return given()
                .baseUri("http://localhost:" + port)
                .when();
    }
}
