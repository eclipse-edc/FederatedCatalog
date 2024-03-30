/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - Initial implementation
 *
 */

package org.eclipse.edc.catalog.cache.query;

import org.eclipse.edc.catalog.spi.CacheQueryAdapterRegistry;
import org.eclipse.edc.catalog.spi.QueryEngine;
import org.eclipse.edc.catalog.spi.QueryResponse;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.connector.controlplane.catalog.spi.Catalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.catalog.test.TestUtil.createCatalog;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QueryEngineImplTest {

    private static final Catalog CATALOG_ABC = createCatalog("ABC");
    private static final Catalog CATALOG_DEF = createCatalog("DEF");
    private static final Catalog CATALOG_XYZ = createCatalog("XYZ");

    @Test
    void getCatalog() {
        CacheQueryAdapterRegistry registry = mock(CacheQueryAdapterRegistry.class);

        when(registry.executeQuery(any())).thenReturn(QueryResponse.ok(List.of(CATALOG_ABC, CATALOG_DEF, CATALOG_XYZ)));

        QueryEngine queryEngine = new QueryEngineImpl(registry);

        QueryResponse catalog = queryEngine.getCatalog(FederatedCatalogCacheQuery.Builder.newInstance().build());
        assertThat(catalog.getStatus()).isEqualTo(QueryResponse.Status.ACCEPTED);
        assertThat(catalog.getErrors()).isEmpty();
        assertThat(catalog.getCatalogs()).containsExactlyInAnyOrder(CATALOG_ABC, CATALOG_DEF, CATALOG_XYZ);

        verify(registry).executeQuery(any());
    }

    @Test
    void getCatalog_withErrors() {
        CacheQueryAdapterRegistry registry = mock(CacheQueryAdapterRegistry.class);

        when(registry.executeQuery(any())).thenReturn(QueryResponse.Builder.newInstance()
                .catalogs(List.of(CATALOG_ABC, CATALOG_DEF, CATALOG_XYZ))
                .error("some error")
                .build());

        QueryEngine queryEngine = new QueryEngineImpl(registry);

        QueryResponse catalog = queryEngine.getCatalog(FederatedCatalogCacheQuery.Builder.newInstance().build());
        assertThat(catalog.getStatus()).isEqualTo(QueryResponse.Status.ACCEPTED);
        assertThat(catalog.getErrors()).hasSize(1).containsExactly("some error");
        assertThat(catalog.getCatalogs()).containsExactlyInAnyOrder(CATALOG_ABC, CATALOG_DEF, CATALOG_XYZ);
        verify(registry).executeQuery(any());
    }

    @Test
    void getCatalog_notAccepted() {
        CacheQueryAdapterRegistry registry = mock(CacheQueryAdapterRegistry.class);

        when(registry.executeQuery(any())).thenReturn(QueryResponse.Builder.newInstance()
                .status(QueryResponse.Status.NO_ADAPTER_FOUND)
                .error("no adapter was found for that query")
                .build());

        QueryEngine queryEngine = new QueryEngineImpl(registry);

        QueryResponse catalog = queryEngine.getCatalog(FederatedCatalogCacheQuery.Builder.newInstance().build());
        assertThat(catalog.getStatus()).isEqualTo(QueryResponse.Status.NO_ADAPTER_FOUND);
        assertThat(catalog.getErrors()).hasSize(1);
        assertThat(catalog.getCatalogs()).isEmpty();
        verify(registry).executeQuery(any());
    }
}