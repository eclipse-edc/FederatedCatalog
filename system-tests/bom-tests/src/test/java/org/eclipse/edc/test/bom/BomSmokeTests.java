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
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.util.io.Ports.getFreePort;
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
                new RuntimePerMethodExtension(new EmbeddedRuntime("fc-dcp-bom", ":dist:bom:federatedcatalog-dcp-bom")
                        .configurationProvider(() -> ConfigFactory.fromMap(new HashMap<>() {
                            {
                                put("edc.iam.sts.oauth.token.url", "https://sts.com/token");
                                put("edc.iam.sts.oauth.client.id", "test-clientid");
                                put("edc.iam.sts.oauth.client.secret.alias", "test-alias");
                                put("web.http.port", "8080");
                                put("web.http.path", "/api");
                                put("web.http.catalog.port", "8081");
                                put("web.http.catalog.path", "/api/catalog");
                                put("web.http.version.port", String.valueOf(getFreePort()));
                                put("edc.catalog.cache.execution.period.seconds", "5");
                                put("edc.iam.issuer.id", "did:web:testparticipant");
                                put("edc.iam.sts.privatekey.alias", "private-alias");
                                put("edc.iam.sts.publickey.id", "public-key-id");
                                put("edc.catalog.cache.execution.delay.seconds", "0");
                            }
                        }))
                );
    }

}
