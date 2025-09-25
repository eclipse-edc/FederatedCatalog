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

import org.eclipse.edc.boot.system.injection.ObjectFactory;
import org.eclipse.edc.catalog.cache.query.DspCatalogRequestAction;
import org.eclipse.edc.catalog.crawler.RecurringExecutionPlan;
import org.eclipse.edc.catalog.spi.CatalogConstants;
import org.eclipse.edc.catalog.spi.FederatedCatalogCache;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.crawler.spi.TargetNodeFilter;
import org.eclipse.edc.crawler.spi.model.ExecutionPlan;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(DependencyInjectionExtension.class)
class FederatedCatalogCacheExtensionTest {
    private final FederatedCatalogCache storeMock = mock();
    private final TargetNodeDirectory nodeDirectoryMock = mock();
    private FederatedCatalogCacheExtension extension;

    @BeforeEach
    void setUp(ServiceExtensionContext context, ObjectFactory factory) {
        var monitorWithPrefix = mock(Monitor.class);
        var monitor = mock(Monitor.class);
        when(monitor.withPrefix(anyString())).thenReturn(monitorWithPrefix);

        context.registerService(TargetNodeDirectory.class, nodeDirectoryMock);
        context.registerService(FederatedCatalogCache.class, storeMock);
        context.registerService(TargetNodeFilter.class, null);
        context.registerService(ExecutionPlan.class, new RecurringExecutionPlan(Duration.ofSeconds(1), Duration.ofSeconds(0), mock()));
        context.registerService(TypeManager.class, mock());
        context.registerService(Monitor.class, monitor);

        extension = factory.constructInstance(FederatedCatalogCacheExtension.class);
    }

    @Test
    void start(ServiceExtensionContext context) {
        extension.initialize(context);
    }

    @Test
    void verifyProvider_cacheNodeAdapterRegistry(ServiceExtensionContext context) {
        var n = extension.createNodeQueryAdapterRegistry(context);
        assertThat(extension.createNodeQueryAdapterRegistry(context)).isSameAs(n);
        assertThat(n.findForProtocol(CatalogConstants.DATASPACE_PROTOCOL)).hasSize(1)
                .allSatisfy(qa -> assertThat(qa).isInstanceOf(DspCatalogRequestAction.class));
    }

}
