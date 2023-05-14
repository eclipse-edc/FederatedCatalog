package org.eclipse.edc.catalog.cache.query;

import org.eclipse.edc.catalog.spi.CatalogRequestMessage;
import org.eclipse.edc.catalog.spi.NodeQueryAdapter;
import org.eclipse.edc.catalog.spi.model.UpdateRequest;
import org.eclipse.edc.catalog.spi.model.UpdateResponse;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.concurrent.CompletableFuture;

public class DspNodeQueryAdapter implements NodeQueryAdapter {
    public static final String DATASPACE_PROTOCOL = "dataspace-protocol-http";
    private static final int INITIAL_OFFSET = 0;
    private static final int BATCH_SIZE = 100;
    private final BatchedRequestFetcher fetcher;

    public DspNodeQueryAdapter(RemoteMessageDispatcherRegistry dispatcherRegistry, Monitor monitor) {

        fetcher = new BatchedRequestFetcher(dispatcherRegistry, monitor);
    }

    @Override
    public CompletableFuture<UpdateResponse> sendRequest(UpdateRequest request) {

        var dspUrl = request.getNodeUrl();
        var catalogRequest = CatalogRequestMessage.Builder.newInstance()
                .protocol(DATASPACE_PROTOCOL)
                .counterPartyAddress(dspUrl)
                .build();
        var catalogFuture = fetcher.fetch(catalogRequest, INITIAL_OFFSET, BATCH_SIZE);

        return catalogFuture.thenApply(catalog -> new UpdateResponse(dspUrl, catalog));
    }
}
