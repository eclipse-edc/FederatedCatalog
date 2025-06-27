/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.catalog.transform;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataService;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.catalog.transform.TestInput.getExpanded;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DATASET_TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DISTRIBUTION_ATTRIBUTE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.ODRL_POLICY_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JsonObjectToDatasetTransformerTest {

    private static final String DATASET_ID = "datasetId";

    private final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(Map.of());
    private final TransformerContext context = mock();

    private JsonObjectToDatasetTransformer transformer;

    private Policy policy;
    private Distribution distribution;

    @BeforeEach
    void setUp() {
        transformer = new JsonObjectToDatasetTransformer();

        policy = Policy.Builder.newInstance().build();
        distribution = Distribution.Builder.newInstance()
                .format("format")
                .dataService(DataService.Builder.newInstance().build())
                .build();

        when(context.transform(any(JsonObject.class), eq(Policy.class)))
                .thenReturn(policy);
        when(context.transform(any(JsonObject.class), eq(Distribution.class)))
                .thenReturn(distribution);
    }

    @Test
    void transform_returnDataset() {
        var policyId = "policy-id";
        var policyJson = getJsonObject(policyId, "policy");
        var distributionJson = getJsonObject("data-service-id", "dataService");

        var dataset = jsonFactory.createObjectBuilder()
                .add(ID, DATASET_ID)
                .add(TYPE, DCAT_DATASET_TYPE)
                .add(ODRL_POLICY_ATTRIBUTE, policyJson)
                .add(DCAT_DISTRIBUTION_ATTRIBUTE, distributionJson)
                .build();

        var result = transformer.transform(getExpanded(dataset), context);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(DATASET_ID);
        assertThat(result.getOffers()).hasSize(1);
        assertThat(result.getOffers()).containsEntry(policyId, policy);
        assertThat(result.getDistributions()).hasSize(1);
        assertThat(result.getDistributions().get(0)).isEqualTo(distribution);

        verify(context, never()).reportProblem(anyString());
        verify(context, times(1)).transform(isA(JsonObject.class), eq(Policy.class));
        verify(context, times(1)).transform(isA(JsonObject.class), eq(Distribution.class));
    }

    @Test
    void transform_datasetWithAdditionalProperty_returnDataset() {
        var propertyKey = "dataset:prop:key";
        var propertyValue = "value";

        when(context.transform(any(JsonValue.class), eq(Object.class))).thenReturn(propertyValue);

        var dataset = jsonFactory.createObjectBuilder()
                .add(ID, DATASET_ID)
                .add(TYPE, DCAT_DATASET_TYPE)
                .add(ODRL_POLICY_ATTRIBUTE, jsonFactory.createObjectBuilder().build())
                .add(DCAT_DISTRIBUTION_ATTRIBUTE, jsonFactory.createObjectBuilder().build())
                .add(propertyKey, propertyValue)
                .build();

        var result = transformer.transform(getExpanded(dataset), context);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(DATASET_ID);
        assertThat(result.getProperties()).hasSize(1);
        assertThat(result.getProperties()).containsEntry(propertyKey, propertyValue);

        verify(context, never()).reportProblem(anyString());
        verify(context, times(1)).transform(any(JsonValue.class), eq(Object.class));
    }

    @Test
    void transform_dataset_withoutDistribution() {
        var policyId = "policy-id";
        var policyJson = getJsonObject(policyId, "policy");
        var distributionJson = jsonFactory.createArrayBuilder().build();

        var dataset = jsonFactory.createObjectBuilder()
                .add(ID, DATASET_ID)
                .add(TYPE, DCAT_DATASET_TYPE)
                .add(ODRL_POLICY_ATTRIBUTE, policyJson)
                .add(DCAT_DISTRIBUTION_ATTRIBUTE, distributionJson)
                .build();

        var result = transformer.transform(getExpanded(dataset), context);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(DATASET_ID);
        assertThat(result.getOffers()).hasSize(1);
        assertThat(result.getOffers()).containsEntry(policyId, policy);
        assertThat(result.getDistributions()).isNotNull().isEmpty();

        verify(context, never()).reportProblem(anyString());
        verify(context, times(1)).transform(isA(JsonObject.class), eq(Policy.class));
        verify(context, never()).transform(isA(JsonObject.class), eq(Distribution.class));
    }

    private JsonObject getJsonObject(String id, String type) {
        return jsonFactory.createObjectBuilder()
                .add(ID, id)
                .add(TYPE, type)
                .build();
    }

}