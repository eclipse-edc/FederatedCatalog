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

package org.eclipse.edc.catalog.cache.crawler;

import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.crawler.spi.CrawlerAction;
import org.eclipse.edc.crawler.spi.CrawlerErrorHandler;
import org.eclipse.edc.crawler.spi.CrawlerSuccessHandler;
import org.eclipse.edc.crawler.spi.WorkItem;
import org.eclipse.edc.crawler.spi.model.UpdateRequest;
import org.eclipse.edc.crawler.spi.model.UpdateResponse;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

/**
 * Receives a target (i.e. a {@link WorkItem}) that it queries for its {@link Catalog}.
 * The resulting {@link UpdateResponse} is returned if successful.
 */
public class Crawler {
    private final Monitor monitor;
    private final CrawlerErrorHandler errorHandler;
    private final CrawlerSuccessHandler successHandler;
    private final String crawlerId;

    /**
     * Constructs a new {@link Crawler}.
     *
     * @param monitor        A monitor
     * @param errorHandler   A {@link CrawlerErrorHandler} that is invoked if any errors occur during querying.
     * @param successHandler A {@link CrawlerSuccessHandler} that is invoked when the query is completed successfully.
     */
    public Crawler(Monitor monitor, @NotNull CrawlerErrorHandler errorHandler, CrawlerSuccessHandler successHandler) {
        this.monitor = monitor;
        this.errorHandler = errorHandler;
        this.successHandler = successHandler;
        crawlerId = format("Crawler-%s", UUID.randomUUID());
    }

    /**
     * Executes a query by sending a request to {@code target} asking for that target's catalo.
     *
     * @param target  The query target
     * @param adapter The protocol handler which is used to send the query
     * @return A completable future containing the response, or an exception if an error occurred.
     */
    public CompletableFuture<? extends UpdateResponse> run(WorkItem target, @NotNull CrawlerAction adapter) {
        try {
            monitor.debug(format("%s: WorkItem acquired", crawlerId));
            var updateFuture = adapter.apply(new UpdateRequest(target.getId(), target.getUrl()));
            return updateFuture.whenComplete((response, throwable) -> {
                if (throwable != null) {
                    handleError(target, throwable.getMessage());
                } else {
                    successHandler.accept(response);
                }
            });
        } catch (Throwable thr) {
            handleError(target, thr.getMessage());
            return CompletableFuture.failedFuture(new EdcException(thr));
        }
    }

    public String getId() {
        return crawlerId;
    }

    private void handleError(@Nullable WorkItem errorWorkItem, String message) {
        monitor.severe(message);
        if (errorWorkItem != null) {
            errorWorkItem.error(message);
            errorHandler.accept(errorWorkItem);
        }
    }

}
