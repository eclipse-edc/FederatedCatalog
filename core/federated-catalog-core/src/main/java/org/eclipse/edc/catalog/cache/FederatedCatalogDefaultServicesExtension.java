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

import org.eclipse.edc.catalog.cache.query.QueryServiceImpl;
import org.eclipse.edc.catalog.crawler.RecurringExecutionPlan;
import org.eclipse.edc.catalog.directory.InMemoryNodeDirectory;
import org.eclipse.edc.catalog.spi.FederatedCatalogCache;
import org.eclipse.edc.catalog.spi.QueryService;
import org.eclipse.edc.catalog.store.InMemoryFederatedCatalogCache;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.crawler.spi.model.ExecutionPlan;
import org.eclipse.edc.query.CriterionOperatorRegistryImpl;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.util.concurrency.LockManager;

import java.time.Duration;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;
import static org.eclipse.edc.catalog.spi.CacheSettings.DEFAULT_EXECUTION_PERIOD_SECONDS;
import static org.eclipse.edc.catalog.spi.CacheSettings.DEFAULT_NUMBER_OF_CRAWLERS;
import static org.eclipse.edc.catalog.spi.CacheSettings.LOW_EXECUTION_PERIOD_SECONDS_THRESHOLD;

/**
 * Provides default service implementations for fallback
 * Omitted {@link org.eclipse.edc.runtime.metamodel.annotation.Extension since there this module already contains {@code FederatedCatalogCacheExtension} }
 */
public class FederatedCatalogDefaultServicesExtension implements ServiceExtension {

    public static final String NAME = "Federated Catalog Default Services";
    private static final int DEFAULT_INITIAL_DELAY = 0;

    @Setting(
            description = "The time to elapse between two crawl runs",
            key = "edc.catalog.cache.execution.period.seconds",
            defaultValue = DEFAULT_EXECUTION_PERIOD_SECONDS + "")
    private long periodSeconds;

    @Setting(
            description = "The number of crawlers (execution threads) that should be used. The engine will re-use crawlers when necessary.",
            key = "edc.catalog.cache.partition.num.crawlers",
            defaultValue = DEFAULT_NUMBER_OF_CRAWLERS + "")
    private int numCrawlers;

    @Setting(
            description = "The initial delay for the cache crawler engine",
            key = "edc.catalog.cache.execution.delay.seconds",
            required = false,
            defaultValue = DEFAULT_INITIAL_DELAY + "")
    private Integer delaySeconds;

    @Inject
    private FederatedCatalogCache store;

    @Override
    public String name() {
        return NAME;
    }

    @Provider(isDefault = true)
    public FederatedCatalogCache defaultCacheStore() {
        return new InMemoryFederatedCatalogCache(new LockManager(new ReentrantReadWriteLock()), CriterionOperatorRegistryImpl.ofDefaults());
    }

    @Provider(isDefault = true)
    public TargetNodeDirectory defaultNodeDirectory() {
        return new InMemoryNodeDirectory();
    }

    @Provider
    public QueryService defaultQueryEngine() {
        return new QueryServiceImpl(store);
    }

    @Provider(isDefault = true)
    public ExecutionPlan createRecurringExecutionPlan(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        if (periodSeconds < LOW_EXECUTION_PERIOD_SECONDS_THRESHOLD) {
            monitor.warning(format("An execution period of %d seconds is very low (threshold = %d). This might result in the work queue to be ever growing." +
                    " A longer execution period or more crawler threads (currently using %d) should be considered.", periodSeconds, LOW_EXECUTION_PERIOD_SECONDS_THRESHOLD, numCrawlers));
        }
        return new RecurringExecutionPlan(Duration.ofSeconds(periodSeconds), Duration.ofSeconds(delaySeconds), monitor);
    }
}
