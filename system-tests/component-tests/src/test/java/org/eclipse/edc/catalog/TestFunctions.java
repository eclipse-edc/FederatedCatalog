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

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.FederatedCacheNode;
import org.eclipse.edc.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.asset.Asset;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.eclipse.edc.junit.testfixtures.TestUtils.getFreePort;

public class TestFunctions {
    public static final String BASE_PATH = "/api";
    public static final int PORT = getFreePort();
    private static final String PATH = "/federatedcatalog";
    private static final TypeRef<List<ContractOffer>> CONTRACT_OFFER_LIST_TYPE = new TypeRef<>() {
    };

    public static CompletableFuture<Catalog> emptyCatalog() {
        return completedFuture(catalogBuilder()
                .build());
    }

    public static Catalog.Builder catalogBuilder() {
        return Catalog.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .contractOffers(Collections.emptyList());
    }

    public static CompletableFuture<Catalog> catalogOf(ContractOffer... offers) {
        return completedFuture(catalogBuilder().contractOffers(asList(offers)).build());
    }

    public static CompletableFuture<Catalog> randomCatalog(int howMany) {
        return completedFuture(catalogBuilder()
                .contractOffers(IntStream.range(0, howMany).mapToObj(i -> createOffer("Offer_" + UUID.randomUUID())).collect(Collectors.toList()))
                .build());
    }

    public static ContractOffer createOffer(String id) {
        return ContractOffer.Builder.newInstance()
                .id(id)
                .asset(Asset.Builder.newInstance().id(id).build())
                .policy(Policy.Builder.newInstance().build())
                .contractStart(ZonedDateTime.now())
                .contractEnd(ZonedDateTime.now().plus(365, ChronoUnit.DAYS))
                .build();
    }

    public static void insertSingle(FederatedCacheNodeDirectory directory) {
        directory.insert(new FederatedCacheNode("test-node", "http://test-node.com", singletonList("ids-multipart")));
    }

    public static List<ContractOffer> queryCatalogApi() {
        return baseRequest()
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post(PATH)
                .body()
                .as(CONTRACT_OFFER_LIST_TYPE);
    }

    private static RequestSpecification baseRequest() {
        return given()
                .baseUri("http://localhost:" + PORT)
                .basePath(BASE_PATH)
                .contentType(ContentType.JSON)
                .when();
    }
}
