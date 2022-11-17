package org.eclipse.edc.catalog.api.query;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;

import java.util.Collection;

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
    Collection<ContractOffer> getCachedCatalog(FederatedCatalogCacheQuery federatedCatalogCacheQuery);
}
