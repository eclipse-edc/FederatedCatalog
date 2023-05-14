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

package org.eclipse.edc.catalog.cache;

import org.eclipse.edc.catalog.cache.query.DspNodeQueryAdapter;
import org.eclipse.edc.catalog.spi.CatalogConstants;
import org.eclipse.edc.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.edc.catalog.spi.FederatedCacheNodeFilter;
import org.eclipse.edc.catalog.spi.FederatedCacheStore;
import org.eclipse.edc.catalog.spi.NodeQueryAdapter;
import org.eclipse.edc.catalog.spi.model.UpdateResponse;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.health.HealthCheckService;
import org.eclipse.edc.spi.system.injection.ObjectFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.catalog.test.TestUtil.TEST_PROTOCOL;
import static org.eclipse.edc.catalog.test.TestUtil.createCatalog;
import static org.eclipse.edc.catalog.test.TestUtil.createNode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DependencyInjectionExtension.class)
class FederatedCatalogCacheExtensionTest {
    private final FederatedCacheStore storeMock = mock(FederatedCacheStore.class);
    private final FederatedCacheNodeDirectory nodeDirectoryMock = mock(FederatedCacheNodeDirectory.class);
    private FederatedCatalogCacheExtension extension;
    private ServiceExtensionContext context;

    @BeforeEach
    void setUp(ServiceExtensionContext context, ObjectFactory factory) {
        this.context = spy(context);
        this.context.registerService(FederatedCacheNodeDirectory.class, nodeDirectoryMock);
        this.context.registerService(FederatedCacheStore.class, storeMock);
        this.context.registerService(FederatedCacheNodeFilter.class, null);
        extension = factory.constructInstance(FederatedCatalogCacheExtension.class);
    }

    @Test
    void name() {
        assertThat(extension.name()).isEqualTo("Federated Catalog Cache");
    }

    @Test
    void initialize() {
        extension.initialize(context);

        verify(context, atLeastOnce()).getMonitor();
        verify(context).getSetting("edc.catalog.cache.partition.num.crawlers", 2);
    }

    @Test
    void initialize_withHealthCheck(ObjectFactory factory) {
        var healthCheckServiceMock = mock(HealthCheckService.class);
        context.registerService(HealthCheckService.class, healthCheckServiceMock);
        extension = factory.constructInstance(FederatedCatalogCacheExtension.class); //reconstruct to honor health service

        extension.initialize(context);

        verify(healthCheckServiceMock).addReadinessProvider(any());
    }

    @Test
    void verify_successHandler_persistIsCalled() {
        when(context.getSetting(eq("edc.catalog.cache.partition.num.crawlers"), anyString())).thenReturn("1");
        when(context.getSetting(eq("edc.catalog.cache.execution.delay.seconds"), any())).thenReturn("0");
        when(nodeDirectoryMock.getAll()).thenReturn(List.of(createNode()));
        extension.initialize(context);
        var queryAdapter = mock(NodeQueryAdapter.class);
        when(queryAdapter.sendRequest(any())).thenReturn(CompletableFuture.completedFuture(new UpdateResponse("test-url", createCatalog("test-catalog"))));
        extension.createNodeQueryAdapterRegistry(context).register(TEST_PROTOCOL, queryAdapter);

        extension.start();

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(storeMock, atLeastOnce()).save(any());
                });
    }

    @Test
    void start(ServiceExtensionContext context) {
        extension.initialize(context);
    }

    @Test
    void verifyProvider_cacheNodeAdapterRegistry() {
        var n = extension.createNodeQueryAdapterRegistry(context);
        assertThat(extension.createNodeQueryAdapterRegistry(context)).isSameAs(n);
        assertThat(n.findForProtocol(CatalogConstants.DATASPACE_PROTOCOL)).hasSize(1)
                .allSatisfy(qa -> assertThat(qa).isInstanceOf(DspNodeQueryAdapter.class));
    }

}
