package org.eclipse.edc.end2end;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.edc.api.model.CriterionDto;
import org.eclipse.edc.connector.api.management.contractdefinition.model.ContractDefinitionRequestDto;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.List;
import java.util.UUID;

import static java.time.Duration.ofSeconds;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.end2end.TestFunctions.createAsset;
import static org.eclipse.edc.end2end.TestFunctions.createPolicy;

@EndToEndTest
class FederatedCatalogTest {
    private final ManagementApiClient apiClient = createManagementClient();

    @NotNull
    private static ManagementApiClient createManagementClient() {
        var mapper = new ObjectMapper();
        //needed for ZonedDateTime
        mapper.registerModule(new JavaTimeModule());
        return new ManagementApiClient(mapper);
    }

    @Test
    void crawl_whenOfferAvailable_shouldContainOffer(TestInfo testInfo) {
        // setup
        var id = String.format("%s-%s", testInfo.getDisplayName(), UUID.randomUUID());
        var asset = createAsset(id);
        var r = apiClient.postAsset(asset);
        assertThat(r.succeeded()).withFailMessage(getError(r)).isTrue();

        var policyId = "policy-" + id;
        var policy = createPolicy(policyId, id);
        var pr = apiClient.postPolicy(policy);
        assertThat(r.succeeded()).withFailMessage(getError(pr)).isTrue();

        var def = ContractDefinitionRequestDto.Builder.newInstance().id("def-" + id)
                .accessPolicyId(policyId)
                .contractPolicyId(policyId)
                .criteria(List.of(CriterionDto.from(Asset.PROPERTY_ID, "=", id)))
                .build();
        var dr = apiClient.postContractDefinition(def);
        assertThat(dr.succeeded()).withFailMessage(getError(dr)).isTrue();

        // act-assert
        await().pollDelay(ofSeconds(1))
                .pollInterval(ofSeconds(1))
                .atMost(ofSeconds(20))
                .untilAsserted(() -> {
                    var contractOffers = apiClient.getContractOffers();
                    assertThat(contractOffers).isNotEmpty();
                    assertThat(contractOffers).extracting(ContractOffer::getAsset).extracting(Asset::getId).contains(id);
                });
    }

    private String getError(Result<String> r) {
        return ofNullable(r.getFailureDetail()).orElse("No error");
    }

}
