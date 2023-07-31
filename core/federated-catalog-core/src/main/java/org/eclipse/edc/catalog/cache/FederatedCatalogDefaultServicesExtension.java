/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.catalog.cache;

import org.eclipse.edc.catalog.cache.query.CacheQueryAdapterImpl;
import org.eclipse.edc.catalog.cache.query.CacheQueryAdapterRegistryImpl;
import org.eclipse.edc.catalog.cache.query.QueryEngineImpl;
import org.eclipse.edc.catalog.directory.InMemoryNodeDirectory;
import org.eclipse.edc.catalog.spi.CacheQueryAdapterRegistry;
import org.eclipse.edc.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.edc.catalog.spi.FederatedCacheStore;
import org.eclipse.edc.catalog.spi.QueryEngine;
import org.eclipse.edc.catalog.store.InMemoryFederatedCacheStore;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.util.concurrency.LockManager;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provides default service implementations for fallback
 * Omitted {@link org.eclipse.edc.runtime.metamodel.annotation.Extension since there this module already contains {@link FederatedCatalogCacheExtension} }
 */
public class FederatedCatalogDefaultServicesExtension implements ServiceExtension {

    public static final String NAME = "Federated Catalog Default Services";

    @Inject
    private FederatedCacheStore store;
    private CacheQueryAdapterRegistry registry;

    @Override
    public String name() {
        return NAME;
    }

    @Provider(isDefault = true)
    public FederatedCacheStore defaultCacheStore() {
        //todo: converts every criterion into a predicate that is always true. must be changed later!
        return new InMemoryFederatedCacheStore(new LockManager(new ReentrantReadWriteLock()));
    }

    @Provider(isDefault = true)
    public FederatedCacheNodeDirectory defaultNodeDirectory() {
        return new InMemoryNodeDirectory();
    }

    @Provider
    public QueryEngine defaultQueryEngine() {
        return new QueryEngineImpl(getCacheQueryAdapterRegistry());
    }

    @Provider
    public CacheQueryAdapterRegistry getCacheQueryAdapterRegistry() {
        if (registry == null) {
            registry = new CacheQueryAdapterRegistryImpl();
            registry.register(new CacheQueryAdapterImpl(store));
        }
        return registry;
    }

}
