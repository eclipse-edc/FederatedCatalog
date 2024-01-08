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

package org.eclipse.edc.catalog.spi;

import org.eclipse.edc.runtime.metamodel.annotation.Setting;

/**
 * Object that provides configuration for the federated catalog.
 * All configuration values that do not allow for default values are resolved instantly, all others are resolved
 * lazily from the context.
 */
public interface CacheSettings {
    int DEFAULT_EXECUTION_PERIOD_SECONDS = 60;
    int LOW_EXECUTION_PERIOD_SECONDS_THRESHOLD = 10;
    int DEFAULT_NUMBER_OF_CRAWLERS = 2;

    @Setting("The time to elapse between two crawl runs")
    String EXECUTION_PLAN_PERIOD_SECONDS = "edc.catalog.cache.execution.period.seconds";
    @Setting("The number of crawlers (execution threads) that should be used. The engine will re-use crawlers when necessary.")
    String NUM_CRAWLER_SETTING = "edc.catalog.cache.partition.num.crawlers";
    @Setting("The initial delay for the cache crawler engine")
    String EXECUTION_PLAN_DELAY_SECONDS = "edc.catalog.cache.execution.delay.seconds";

}
