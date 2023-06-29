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

package org.eclipse.edc.catalog.api.query;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.catalog.spi.QueryEngine;
import org.eclipse.edc.catalog.spi.QueryResponse;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.spi.result.AbstractResult;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import static jakarta.json.stream.JsonCollectors.toJsonArray;

@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/federatedcatalog")
public class FederatedCatalogApiController implements FederatedCatalogApi {

    private final QueryEngine queryEngine;
    private final TypeTransformerRegistry transformerRegistry;

    public FederatedCatalogApiController(QueryEngine queryEngine, TypeTransformerRegistry transformerRegistry) {
        this.queryEngine = queryEngine;
        this.transformerRegistry = transformerRegistry;
    }

    @Override
    @POST
    public JsonArray getCachedCatalog(FederatedCatalogCacheQuery federatedCatalogCacheQuery) {
        var queryResponse = queryEngine.getCatalog(federatedCatalogCacheQuery);
        // query not possible
        if (queryResponse.getStatus() == QueryResponse.Status.NO_ADAPTER_FOUND) {
            throw new QueryNotAcceptedException();
        }
        if (!queryResponse.getErrors().isEmpty()) {
            throw new QueryException(queryResponse.getErrors());
        }

        var catalogs = queryResponse.getCatalogs();
        return catalogs.stream().map(c -> transformerRegistry.transform(c, JsonObject.class))
                .filter(Result::succeeded)
                .map(AbstractResult::getContent)
                .collect(toJsonArray());
    }
}
