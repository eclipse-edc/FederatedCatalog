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

package org.eclipse.edc.catalog.cache.query;

import org.eclipse.edc.catalog.spi.CatalogRequestMessage;
import org.eclipse.edc.catalog.spi.NodeQueryAdapter;
import org.eclipse.edc.catalog.spi.model.UpdateRequest;
import org.eclipse.edc.catalog.spi.model.UpdateResponse;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.concurrent.CompletableFuture;

@Deprecated(forRemoval = true, since = "milestone9")
public class IdsMultipartNodeQueryAdapter implements NodeQueryAdapter {
    public static final String IDS_MULTIPART_PROTOCOL = "ids-multipart";
    private final String connectorId;
    private final BatchedRequestFetcher requestFetcher;

    public IdsMultipartNodeQueryAdapter(String connectorId, RemoteMessageDispatcherRegistry dispatcherRegistry, Monitor monitor) {
        this.connectorId = connectorId;
        requestFetcher = new BatchedRequestFetcher(dispatcherRegistry, monitor);
    }

    @Override
    public CompletableFuture<UpdateResponse> sendRequest(UpdateRequest updateRequest) {

        var catalogRequest = CatalogRequestMessage.Builder.newInstance()
                .protocol(IDS_MULTIPART_PROTOCOL)
                .counterPartyAddress(getNodeUrl(updateRequest))
                .connectorId(connectorId)
                .build();
        var catalogFuture = requestFetcher.fetch(catalogRequest, 0, 100);

        return catalogFuture.thenApply(catalog -> new UpdateResponse(getNodeUrl(updateRequest), catalog));
    }

    // adds /api/ids/data if not already there
    private String getNodeUrl(UpdateRequest updateRequest) {
        var url = updateRequest.getNodeUrl();
        if (!url.endsWith("/ids/data")) {
            url += "/api/v1/ids/data";
        }

        return url;
    }
}
