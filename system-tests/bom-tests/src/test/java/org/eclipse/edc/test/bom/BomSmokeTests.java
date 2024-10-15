/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.test.bom;


import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerMethodExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;


public class BomSmokeTests {
    abstract static class SmokeTest {
        public static final String DEFAULT_PORT = "8080";
        public static final String DEFAULT_PATH = "/api";

        @Test
        void assertRuntimeReady() {
            await().untilAsserted(() -> given()
                    .baseUri("http://localhost:" + DEFAULT_PORT + DEFAULT_PATH + "/check/startup")
                    .get()
                    .then()
                    .statusCode(200)
                    .log().ifValidationFails()
                    .body("isSystemHealthy", equalTo(true)));

        }
    }

    @Nested
    @EndToEndTest
    class FederatedCatalogDcp extends SmokeTest {

        @RegisterExtension
        protected RuntimeExtension runtime =
                new RuntimePerMethodExtension(new EmbeddedRuntime("fc-dcp-bom",
                        Map.of(
                                "edc.iam.sts.oauth.token.url", "https://sts.com/token",
                                "edc.iam.sts.oauth.client.id", "test-clientid",
                                "edc.iam.sts.oauth.client.secret.alias", "test-alias",
                                "web.http.port", "8080",
                                "web.http.path", "/api",
                                "web.http.catalog.port", "8081",
                                "web.http.catalog.path", "/api/catalog",
                                "edc.catalog.cache.execution.period.seconds", "5",
                                "edc.catalog.cache.execution.delay.seconds", "0"),
                        ":dist:bom:federatedcatalog-dcp-bom"
                ));
    }

}
