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

package org.eclipse.edc.end2end;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromCatalogTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromDataServiceTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromDatasetTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromDistributionTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromPolicyTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToCatalogTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToDataServiceTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToDatasetTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToDistributionTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonValueToGenericTypeTransformer;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.jsonld.util.JacksonJsonLd;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static java.time.Duration.ofSeconds;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.core.transform.transformer.OdrlTransformersFactory.jsonObjectToOdrlTransformers;
import static org.eclipse.edc.end2end.TestFunctions.createContractDef;
import static org.eclipse.edc.end2end.TestFunctions.createPolicy;
import static org.mockito.Mockito.mock;

@EndToEndTest
class FederatedCatalogTest {

    public static final Duration TIMEOUT = ofSeconds(60);
    private final TypeTransformerRegistry typeTransformerRegistry = new TypeTransformerRegistryImpl();
    private final ObjectMapper mapper = JacksonJsonLd.createObjectMapper();
    private final ManagementApiClient apiClient = new ManagementApiClient(mapper, new TitaniumJsonLd(mock(Monitor.class)), typeTransformerRegistry);

    @BeforeEach
    void setUp() {
        //needed for ZonedDateTime
        mapper.registerModule(new JavaTimeModule());
        JsonBuilderFactory factory = Json.createBuilderFactory(Map.of());
        typeTransformerRegistry.register(new JsonObjectFromCatalogTransformer(factory, mapper));
        typeTransformerRegistry.register(new JsonObjectFromDatasetTransformer(factory, mapper));
        typeTransformerRegistry.register(new JsonObjectFromDataServiceTransformer(factory));
        typeTransformerRegistry.register(new JsonObjectFromPolicyTransformer(factory));
        typeTransformerRegistry.register(new JsonObjectFromDistributionTransformer(factory));
        typeTransformerRegistry.register(new JsonObjectToCatalogTransformer());
        typeTransformerRegistry.register(new JsonObjectToDatasetTransformer());
        typeTransformerRegistry.register(new JsonObjectToDataServiceTransformer());
        jsonObjectToOdrlTransformers().forEach(typeTransformerRegistry::register);
        typeTransformerRegistry.register(new JsonObjectToDistributionTransformer());
        typeTransformerRegistry.register(new JsonValueToGenericTypeTransformer(mapper));
    }

    @Test
    void crawl_whenOfferAvailable_shouldContainOffer(TestInfo testInfo) {
        // setup
        var id = String.format("%s-%s", testInfo.getDisplayName(), UUID.randomUUID());
        var asset = TestFunctions.createAssetJson(id);
        var r = apiClient.postAsset(asset);
        assertThat(r.succeeded()).withFailMessage(getError(r)).isTrue();

        var assetId = r.getContent();

        var policyId = "policy-" + id;
        var policy = createPolicy(policyId, id);
        var pr = apiClient.postPolicy(policy);
        assertThat(r.succeeded()).withFailMessage(getError(pr)).isTrue();

        policyId = pr.getContent();

        var request = createContractDef("def-" + id, policyId, policyId, assetId);

        var dr = apiClient.postContractDefinition(request);
        assertThat(dr.succeeded()).withFailMessage(getError(dr)).isTrue();

        var assetIdBase64 = Base64.getEncoder().encodeToString(assetId.getBytes());
        // act-assert
        await().pollDelay(ofSeconds(1))
                .pollInterval(ofSeconds(1))
                .atMost(TIMEOUT)
                .untilAsserted(() -> {
                    var catalogs = apiClient.getContractOffers();

                    assertThat(catalogs).hasSizeGreaterThanOrEqualTo(1);
                    assertThat(catalogs).anySatisfy(catalog -> {
                        assertThat(catalog.getDatasets())
                                .anySatisfy(dataset -> {
                                    assertThat(dataset.getOffers()).hasSizeGreaterThanOrEqualTo(1);
                                    assertThat(dataset.getOffers().keySet()).anyMatch(key -> key.contains(assetIdBase64));
                                });
                    });

                });
    }

    private String getError(Result<String> r) {
        return ofNullable(r.getFailureDetail()).orElse("No error");
    }
}
