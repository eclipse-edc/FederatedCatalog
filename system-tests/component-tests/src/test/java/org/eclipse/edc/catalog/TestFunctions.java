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

package org.eclipse.edc.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.json.JsonObject;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogConstants;
import org.eclipse.edc.catalog.spi.DataService;
import org.eclipse.edc.catalog.spi.Dataset;
import org.eclipse.edc.catalog.spi.Distribution;
import org.eclipse.edc.catalog.spi.FederatedCacheNode;
import org.eclipse.edc.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.policy.model.Policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;
import static org.eclipse.edc.junit.testfixtures.TestUtils.getFreePort;

public class TestFunctions {
    public static final String BASE_PATH = "/api";
    public static final int PORT = getFreePort();
    private static final String PATH = "/federatedcatalog";
    private static final TypeReference<List<Catalog>> CONTRACT_OFFER_LIST_TYPE = new TypeReference<>() {
    };


    public static CompletableFuture<byte[]> emptyCatalog(Function<Catalog, byte[]> transformationFunction) {
        return completedFuture(transformationFunction.apply(catalogBuilder().build()));
    }

    public static CompletableFuture<byte[]> emptyCatalog(Function<Catalog, byte[]> transformationFunction, String catalogId) {
        return completedFuture(transformationFunction.apply(catalogBuilder().id(catalogId).build()));
    }

    public static Catalog.Builder catalogBuilder() {
        return Catalog.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .properties(new HashMap<>())
                .dataServices(new ArrayList<>())
                .datasets(new ArrayList<>())
                .contractOffers(Collections.emptyList());
    }

    public static CompletableFuture<byte[]> catalogOf(Function<Catalog, byte[]> transformationFunction, String catId, Dataset... datasets) {
        return completedFuture(transformationFunction.apply(catalogBuilder().id(catId).datasets(asList(datasets)).build()));
    }

    public static CompletableFuture<byte[]> randomCatalog(Function<Catalog, byte[]> transformationFunction, String id, int howMany) {
        return completedFuture(transformationFunction.apply(catalogBuilder()
                .id(id)
                .datasets(IntStream.range(0, howMany).mapToObj(i -> createDataset("DataSet_" + UUID.randomUUID())).collect(toList()))
                .build()));
    }

    public static ContractOffer createOffer(String id) {
        return ContractOffer.Builder.newInstance()
                .id(id)
                .assetId(id)
                .policy(Policy.Builder.newInstance().build())
                .build();
    }

    public static void insertSingle(FederatedCacheNodeDirectory directory) {
        directory.insert(new FederatedCacheNode("test-node", "http://test-node.com", singletonList(CatalogConstants.DATASPACE_PROTOCOL)));
    }

    public static List<Catalog> queryCatalogApi(Function<JsonObject, Catalog> transformerFunction) {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var body = baseRequest()
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post(PATH)
                .body();

        try {
            return objectMapper.readValue(body.asString(), CONTRACT_OFFER_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    private static RequestSpecification baseRequest() {
        return given()
                .baseUri("http://localhost:" + PORT)
                .basePath(BASE_PATH)
                .contentType(ContentType.JSON)
                .when();
    }

    public static Dataset createDataset(String dataset1) {
        return Dataset.Builder.newInstance()
                .offer("test-offer", Policy.Builder.newInstance().build())
                .distribution(Distribution.Builder.newInstance().format("test-format").dataService(DataService.Builder.newInstance().build()).build())
                .id(dataset1)
                .build();
    }
}
