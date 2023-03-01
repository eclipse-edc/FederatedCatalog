package org.eclipse.edc.end2end;

import org.eclipse.edc.connector.api.management.asset.model.AssetCreationRequestDto;
import org.eclipse.edc.connector.api.management.asset.model.AssetEntryDto;
import org.eclipse.edc.connector.api.management.asset.model.DataAddressDto;
import org.eclipse.edc.connector.api.management.policy.model.PolicyDefinitionRequestDto;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.asset.Asset;

import java.util.Map;

public class TestFunctions {
    public static AssetEntryDto createAsset(String id) {
        return AssetEntryDto.Builder.newInstance()
                .asset(AssetCreationRequestDto.Builder.newInstance()
                        .id(id)
                        .properties(Map.of(
                                Asset.PROPERTY_CONTENT_TYPE, "application/octet-stream",
                                Asset.PROPERTY_VERSION, "1.0",
                                Asset.PROPERTY_NAME, id
                        ))
                        .build())
                .dataAddress(DataAddressDto.Builder.newInstance()
                        .properties(Map.of(
                                "type", "test-type"
                        ))
                        .build())
                .build();
    }

    public static PolicyDefinitionRequestDto createPolicy(String policyId, String assetId) {
        return PolicyDefinitionRequestDto.Builder.newInstance()
                .id(policyId)
                .policy(Policy.Builder.newInstance()
                        .target(assetId)
                        .build()
                )
                .build();
    }
}
