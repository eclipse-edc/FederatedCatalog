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

package org.eclipse.edc.crawler.spi.model;

import org.eclipse.edc.spi.monitor.Monitor;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An ExecutionPlan that executes periodically according to a given schedule.
 */
public class RecurringExecutionPlan implements ExecutionPlan {

    protected static final String ERROR_DURING_PLAN_EXECUTION = "Unexpected error during plan execution";
    protected static final String ERROR_DURING_PLAN_SHUTDOWN = "Unexpected error during execution plan shutdown";
    private static final Integer EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 60;

    private final Duration schedule;
    private final Duration withInitialDelay;
    private final Monitor monitor;
    private ScheduledExecutorService ses;

    /**
     * Instantiates the {@code RecurringExecutionPlan}.
     *
     * @param schedule     A time span used for initial delay and for the period.
     * @param initialDelay Specifies whether the execution plan should run right away or after an initial delay passes.
     */
    public RecurringExecutionPlan(Duration schedule, Duration initialDelay, Monitor monitor) {
        this.schedule = schedule;
        withInitialDelay = initialDelay;
        this.monitor = monitor;
    }

    @Override
    public void run(Runnable task) {
        this.ses = Executors.newSingleThreadScheduledExecutor();
        this.ses.scheduleAtFixedRate(catchExceptions(task), withInitialDelay.toMillis(), schedule.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (ses != null && !ses.isShutdown()) {
            ses.shutdown();
            try {
                if (!ses.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    ses.shutdownNow();

                    if (!ses.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                        monitor.warning("The execution plan did not shutdown");
                }
            } catch (InterruptedException ie) {
                monitor.severe(ERROR_DURING_PLAN_SHUTDOWN, ie);
                ses.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private Runnable catchExceptions(Runnable original) {
        return () -> {
            try {
                original.run();
            } catch (Throwable thr) {
                monitor.severe(ERROR_DURING_PLAN_EXECUTION, thr);
            }
        };
    }
}
