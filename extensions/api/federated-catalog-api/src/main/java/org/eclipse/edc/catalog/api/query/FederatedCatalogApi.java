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

package org.eclipse.edc.catalog.api.query;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.JsonArray;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;

@OpenAPIDefinition
@Tag(name = "Federated Catalog")
public interface FederatedCatalogApi {
    @Operation(description = "Obtains all contract offers currently held by this cache instance",
            responses = {
                    @ApiResponse(responseCode = "200", description = "A list of contract offers is returned, potentially empty",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContractOffer.class)))),
                    @ApiResponse(responseCode = "50x", description = "A Query could not be completed due to an internal error")
            }

    )
    JsonArray getCachedCatalog(FederatedCatalogCacheQuery federatedCatalogCacheQuery);
}
