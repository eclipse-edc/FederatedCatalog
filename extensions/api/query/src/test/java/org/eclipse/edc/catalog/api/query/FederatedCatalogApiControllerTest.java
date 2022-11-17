package org.eclipse.edc.catalog.api.query;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.eclipse.edc.catalog.spi.CacheQueryAdapter;
import org.eclipse.edc.catalog.spi.CacheQueryAdapterRegistry;
import org.eclipse.edc.catalog.spi.FederatedCacheStore;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.catalog.test.TestUtil.createOffer;
import static org.eclipse.edc.junit.testfixtures.TestUtils.getFreePort;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ApiTest
@ExtendWith(EdcExtension.class)
class FederatedCatalogApiControllerTest {
    private static final String BASE_PATH = "/api";
    private static final TypeRef<List<ContractOffer>> CONTRACT_OFFER_LIST_TYPE = new TypeRef<>() {
    };
    private final int port = getFreePort();

    @BeforeEach
    void setUp(EdcExtension extension) {
        extension.setConfiguration(Map.of(
                "web.http.port", String.valueOf(port),
                "web.http.path", BASE_PATH
        ));
    }

    @Test
    void queryApi_whenEmptyResult() {
        var response = baseRequest()
                .contentType(ContentType.JSON)
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post("/federatedcatalog")
                .then()
                .statusCode(200)
                .extract()
                .as(CONTRACT_OFFER_LIST_TYPE);

        assertThat(response).isEmpty();
    }

    @Test
    void queryApi_whenResultsReturned(FederatedCacheStore store) {
        int nbAssets = 3;

        // generate assets and populate the store
        List<ContractOffer> assets = new ArrayList<>();
        for (int i = 0; i < nbAssets; i++) {
            assets.add(createOffer("some-offer-" + i));
        }
        assets.forEach(store::save);


        var offers = baseRequest()
                .contentType(ContentType.JSON)
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post("/federatedcatalog")
                .then()
                .statusCode(200)
                .extract()
                .as(CONTRACT_OFFER_LIST_TYPE);

        // test
        compareAssetsById(offers, assets);
    }

    @Test
    void queryApi_whenQueryUnsuccessful(CacheQueryAdapterRegistry adapterRegistry) {
        var adapter = mock(CacheQueryAdapter.class);
        when(adapter.executeQuery(any())).thenThrow(new RuntimeException("test exception"));
        when(adapter.canExecute(any())).thenReturn(true);
        adapterRegistry.register(adapter);

        baseRequest()
                .contentType(ContentType.JSON)
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post("/federatedcatalog")
                .then()
                .statusCode(500);

    }

    private void compareAssetsById(List<ContractOffer> actual, List<ContractOffer> expected) {
        List<String> actualAssetIds = actual.stream().map(co -> co.getAsset().getId()).sorted().collect(Collectors.toList());
        List<String> expectedAssetIds = expected.stream().map(co -> co.getAsset().getId()).sorted().collect(Collectors.toList());
        assertThat(actualAssetIds).isEqualTo(expectedAssetIds);
    }

    private RequestSpecification baseRequest() {
        return given()
                .baseUri("http://localhost:" + port)
                .basePath(BASE_PATH)
                .when();
    }
}