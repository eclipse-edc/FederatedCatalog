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


import org.eclipse.edc.crawler.spi.CrawlerAction;

/**
 * {@link CrawlerAction}s accept {@code UpdateRequests} to execute
 */
public record UpdateRequest(String nodeId, String nodeUrl) {
}
