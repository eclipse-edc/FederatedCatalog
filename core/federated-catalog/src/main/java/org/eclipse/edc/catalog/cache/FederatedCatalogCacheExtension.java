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

package org.eclipse.edc.catalog.cache;

import org.eclipse.edc.catalog.cache.crawler.NodeQueryAdapterRegistryImpl;
import org.eclipse.edc.catalog.cache.query.IdsMultipartNodeQueryAdapter;
import org.eclipse.edc.catalog.spi.CacheConfiguration;
import org.eclipse.edc.catalog.spi.CachedAsset;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.edc.catalog.spi.FederatedCacheNodeFilter;
import org.eclipse.edc.catalog.spi.FederatedCacheStore;
import org.eclipse.edc.catalog.spi.NodeQueryAdapterRegistry;
import org.eclipse.edc.catalog.spi.model.ExecutionPlan;
import org.eclipse.edc.catalog.spi.model.UpdateResponse;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.health.HealthCheckResult;
import org.eclipse.edc.spi.system.health.HealthCheckService;

import static java.util.Optional.ofNullable;

@Extension(value = FederatedCatalogCacheExtension.NAME)
public class FederatedCatalogCacheExtension implements ServiceExtension {
    public static final String NAME = "Federated Catalog Cache";
    private Monitor monitor;
    @Inject
    private FederatedCacheStore store;
    @Inject(required = false)
    private HealthCheckService healthCheckService;
    @Inject
    private RemoteMessageDispatcherRegistry dispatcherRegistry;
    // get all known nodes from node directory - must be supplied by another extension
    @Inject
    private FederatedCacheNodeDirectory directory;
    // optional filter function to select FC nodes eligible for crawling.
    @Inject(required = false)
    private FederatedCacheNodeFilter nodeFilter;

    private ExecutionPlan executionPlan;
    private NodeQueryAdapterRegistryImpl nodeQueryAdapterRegistry;
    private ExecutionManager executionManager;

    @Override
    public String name() {
        return NAME;
    }


    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();


        // CRAWLER SUBSYSTEM
        // contribute to the liveness probe
        if (healthCheckService != null) {
            healthCheckService.addReadinessProvider(() -> HealthCheckResult.Builder.newInstance().component("FCC Crawler Subsystem").build());
        }
        var cacheConfiguration = new CacheConfiguration(context);
        int numCrawlers = cacheConfiguration.getNumCrawlers();
        // and a loader manager

        executionPlan = cacheConfiguration.getExecutionPlan();

        // by default only uses FC nodes that are not "self"
        nodeFilter = ofNullable(nodeFilter).orElse(node -> !node.getName().equals(context.getConnectorId()));

        executionManager = ExecutionManager.Builder.newInstance()
                .monitor(monitor)
                .preExecutionTask(() -> {
                    store.deleteExpired();
                    store.expireAll();
                })
                .numCrawlers(numCrawlers)
                .nodeQueryAdapterRegistry(createNodeQueryAdapterRegistry(context))
                .onSuccess(this::persist)
                .nodeDirectory(directory)
                .nodeFilterFunction(nodeFilter)
                .build();
    }

    @Override
    public void start() {
        executionManager.executePlan(executionPlan);
    }

    @Provider
    public NodeQueryAdapterRegistry createNodeQueryAdapterRegistry(ServiceExtensionContext context) {

        if (nodeQueryAdapterRegistry == null) {
            nodeQueryAdapterRegistry = new NodeQueryAdapterRegistryImpl();
            // catalog queries via IDS multipart are supported by default
            nodeQueryAdapterRegistry.register("ids-multipart", new IdsMultipartNodeQueryAdapter(context.getConnectorId(), dispatcherRegistry, monitor));
        }
        return nodeQueryAdapterRegistry;
    }

    /**
     * inserts a particular {@link Catalog} in the {@link FederatedCacheStore}
     *
     * @param updateResponse The response that contains the catalog
     */
    private void persist(UpdateResponse updateResponse) {
        updateResponse.getCatalog().getContractOffers().forEach(offer -> {
            offer.getAsset().getProperties().put(CachedAsset.PROPERTY_ORIGINATOR, updateResponse.getSource());
            store.save(offer);
        });
    }
}
