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
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.catalog.transform.TestInput.getExpanded;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DATA_SERVICE_TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_ENDPOINT_DESCRIPTION_ATTRIBUTE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_ENDPOINT_URL_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;


class JsonObjectToDataServiceTransformerTest {

    private static final String DATA_SERVICE_ID = "dataServiceId";

    private final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(Map.of());
    private final TransformerContext context = mock(TransformerContext.class);

    private JsonObjectToDataServiceTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new JsonObjectToDataServiceTransformer();
    }

    @Test
    void transform_returnDataService() {
        var terms = "terms";
        var url = "url";

        var dataService = jsonFactory.createObjectBuilder()
                .add(ID, DATA_SERVICE_ID)
                .add(TYPE, DCAT_DATA_SERVICE_TYPE)
                .add(DCAT_ENDPOINT_DESCRIPTION_ATTRIBUTE, terms)
                .add(DCAT_ENDPOINT_URL_ATTRIBUTE, url)
                .build();

        var result = transformer.transform(getExpanded(dataService), context);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(DATA_SERVICE_ID);
        assertThat(result.getEndpointDescription()).isEqualTo(terms);
        assertThat(result.getEndpointUrl()).isEqualTo(url);

        verifyNoInteractions(context);
    }

    @Test
    void transform_invalidType_reportProblem() {
        var dataService = jsonFactory.createObjectBuilder().add(TYPE, "not-a-data-service").build();

        transformer.transform(getExpanded(dataService), context);

        verify(context, never()).reportProblem(anyString());
    }
}
