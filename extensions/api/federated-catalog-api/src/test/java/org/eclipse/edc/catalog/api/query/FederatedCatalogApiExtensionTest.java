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

package org.eclipse.edc.catalog.api.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.jsonld.util.JacksonJsonLd;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.eclipse.edc.catalog.api.query.FederatedCatalogApiExtension.CATALOG_QUERY_SCOPE;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.VOCAB;
import static org.eclipse.edc.jsonld.spi.Namespaces.DCAT_PREFIX;
import static org.eclipse.edc.jsonld.spi.Namespaces.DCAT_SCHEMA;
import static org.eclipse.edc.jsonld.spi.Namespaces.DCT_PREFIX;
import static org.eclipse.edc.jsonld.spi.Namespaces.DCT_SCHEMA;
import static org.eclipse.edc.jsonld.spi.Namespaces.DSPACE_PREFIX;
import static org.eclipse.edc.jsonld.spi.Namespaces.DSPACE_SCHEMA;
import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_PREFIX;
import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_SCHEMA;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_PREFIX;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DependencyInjectionExtension.class)
public class FederatedCatalogApiExtensionTest {

    private final JsonLd jsonLd = mock();
    private final TypeManager typeManager = mock();
    private final ObjectMapper mapper = JacksonJsonLd.createObjectMapper();

    @BeforeEach
    void setUp(ServiceExtensionContext context) {
        context.registerService(JsonLd.class, jsonLd);
        context.registerService(TypeManager.class, typeManager);
        when(typeManager.getMapper()).thenReturn(mapper);
    }

    @Test
    void initialize_shouldRegisterNamespaces(FederatedCatalogApiExtension extension, ServiceExtensionContext context) {
        extension.initialize(context);

        verify(jsonLd).registerNamespace(DCAT_PREFIX, DCAT_SCHEMA, CATALOG_QUERY_SCOPE);
        verify(jsonLd).registerNamespace(DCT_PREFIX, DCT_SCHEMA, CATALOG_QUERY_SCOPE);
        verify(jsonLd).registerNamespace(ODRL_PREFIX, ODRL_SCHEMA, CATALOG_QUERY_SCOPE);
        verify(jsonLd).registerNamespace(VOCAB, EDC_NAMESPACE, CATALOG_QUERY_SCOPE);
        verify(jsonLd).registerNamespace(EDC_PREFIX, EDC_NAMESPACE, CATALOG_QUERY_SCOPE);
        verify(jsonLd).registerNamespace(DSPACE_PREFIX, DSPACE_SCHEMA, CATALOG_QUERY_SCOPE);
    }

}
