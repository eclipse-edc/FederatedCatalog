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

package org.eclipse.edc.end2end;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

class ManagementApiClient {
    private static final String MANAGEMENT_BASE_URL = "http://localhost:9192/management";
    private static final String CATALOG_BASE_URL = "http://localhost:8181/api";
    private static final TypeReference<List<Map<String, Object>>> LIST_TYPE_REFERENCE = new TypeReference<>() {
    };
    private static final MediaType JSON = MediaType.parse("application/json");
    private final ObjectMapper mapper;
    private final JsonLd jsonLdService;
    private final TypeTransformerRegistry typeTransformerRegistry;

    ManagementApiClient(ObjectMapper mapper, JsonLd jsonLdService, TypeTransformerRegistry typeTransformerRegistry) {
        this.mapper = mapper;
        this.jsonLdService = jsonLdService;
        this.typeTransformerRegistry = typeTransformerRegistry;
    }

    private static String catalog(String path) {
        return CATALOG_BASE_URL + path;
    }

    Result<String> postAsset(JsonObject entry) {
        return postObjectWithId(createPostRequest(entry, MANAGEMENT_BASE_URL + "/v3/assets"));
    }

    Result<String> postPolicy(String policyJsonLd) {
        return postObjectWithId(createPostRequest(policyJsonLd, MANAGEMENT_BASE_URL + "/v2/policydefinitions"));
    }

    Result<String> postContractDefinition(JsonObject definition) {
        return postObjectWithId(createPostRequest(definition, MANAGEMENT_BASE_URL + "/v2/contractdefinitions"));
    }

    List<Catalog> getContractOffers() {
        var rq = createPostRequest(FederatedCatalogCacheQuery.Builder.newInstance().build(), catalog("/federatedcatalog"));

        try (var response = getClient().newCall(rq).execute()) {
            if (response.isSuccessful()) {
                var rawJsonLd = response.body().string();
                var list = mapper.readValue(rawJsonLd, LIST_TYPE_REFERENCE);

                return list.stream().map(m -> {
                    var jsonObj = jsonLdService.expand(Json.createObjectBuilder(m).build()).getContent();
                    return typeTransformerRegistry.transform(jsonObj, Catalog.class).getContent();
                }).toList();

            }
            throw new RuntimeException(format("Error getting catalog: %s", response));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private Result<String> postObjectWithId(Request policy) {
        try (var response = getClient()
                .newCall(policy)
                .execute()) {
            var stringbody = response.body().string();
            return response.isSuccessful() ?
                    Result.success(fromJson(stringbody, JsonObject.class).getString("@id")) :
                    Result.failure(response.message());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T fromJson(String string, Class<T> clazz) {
        try {
            return mapper.readValue(string, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private Request createPostRequest(Object object, String path) {
        return new Request.Builder().url(path).post(RequestBody.create(asJson(object), JSON)).build();
    }

    private String asJson(Object entry) {
        try {
            return entry instanceof String ? (String) entry : mapper.writeValueAsString(entry);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private OkHttpClient getClient() {
        return new OkHttpClient();
    }
}
