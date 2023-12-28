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

import org.eclipse.edc.catalog.cache.TestUpdateResponse;
import org.eclipse.edc.crawler.spi.CrawlerAction;
import org.eclipse.edc.crawler.spi.CrawlerErrorHandler;
import org.eclipse.edc.crawler.spi.CrawlerSuccessHandler;
import org.eclipse.edc.crawler.spi.WorkItem;
import org.eclipse.edc.crawler.spi.model.UpdateRequest;
import org.eclipse.edc.crawler.spi.model.UpdateResponse;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CrawlerTest {

    private Crawler crawler;
    private CrawlerErrorHandler errorHandlerMock;
    private CrawlerSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        errorHandlerMock = mock(CrawlerErrorHandler.class);
        successHandler = mock(CrawlerSuccessHandler.class);
        crawler = new Crawler(mock(Monitor.class), errorHandlerMock, successHandler);
    }

    @Test
    void run() {
        WorkItem target = createWorkItem();
        var adapter = new CrawlerAction() {
            @Override
            public CompletableFuture<UpdateResponse> apply(UpdateRequest request) {
                return CompletableFuture.completedFuture(new TestUpdateResponse(target.getUrl()));
            }
        };
        assertThat(crawler.run(target, adapter)).isCompleted();
        verify(successHandler).accept(argThat(argument -> argument.getSource().equals(target.getUrl())));
        verifyNoInteractions(errorHandlerMock);
    }

    @Test
    void run_withError() {
        WorkItem target = createWorkItem();
        var adapter = new CrawlerAction() {
            @Override
            public CompletableFuture<UpdateResponse> apply(UpdateRequest request) {
                return CompletableFuture.failedFuture(new EdcException("foobar"));
            }
        };
        assertThat(crawler.run(target, adapter)).isCompletedExceptionally();
        verify(errorHandlerMock).accept(eq(target));
        verifyNoInteractions(successHandler);
    }

    @Test
    void run_throwsException() {
        WorkItem target = createWorkItem();
        var adapter = mock(CrawlerAction.class);
        when(adapter.apply(any())).thenThrow(new RuntimeException("test exception"));
        assertThat(crawler.run(target, adapter)).isCompletedExceptionally();
        verify(errorHandlerMock).accept(eq(target));
        verifyNoInteractions(successHandler);
    }

    @Test
    void getId() {
        assertThat(crawler.getId()).isNotNull();
    }


    @NotNull
    private WorkItem createWorkItem() {
        return new WorkItem("test-url", "test-protocol");
    }
}