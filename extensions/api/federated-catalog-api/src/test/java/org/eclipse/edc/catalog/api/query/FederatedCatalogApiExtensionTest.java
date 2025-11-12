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
import static org.eclipse.edc.jsonld.spi.Namespaces.DSPACE_CONTEXT_2025_1;
import static org.eclipse.edc.jsonld.spi.Namespaces.EDC_DSPACE_CONTEXT;
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

        verify(jsonLd).registerContext(DSPACE_CONTEXT_2025_1, CATALOG_QUERY_SCOPE);
        verify(jsonLd).registerContext(EDC_DSPACE_CONTEXT, CATALOG_QUERY_SCOPE);
    }

}
