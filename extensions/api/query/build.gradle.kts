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
 *       Microsoft Corporation - initial API and implementation
 *
 */

plugins {
    `java-library`
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

dependencies {
    api(edc.spi.core)
    api(edc.spi.web)
    api(project(":spi:federated-catalog-spi"))

    implementation(edc.util)
    implementation(edc.core.connector)
    implementation(libs.okhttp)
    implementation(libs.jakarta.rsApi)
    implementation(libs.failsafe.core)

    // required for integration test
    testImplementation(project(":core:federated-catalog")) // provides the QueryEngine impl
    testImplementation(testFixtures(project(":core:federated-catalog"))) // provides the TestUtil
    testImplementation(edc.core.junit)
    testImplementation(edc.ext.http)
    testImplementation(edc.spi.ids)
    testImplementation(libs.awaitility)
    testImplementation(libs.restAssured)
}

edcBuild {
    swagger {
        apiGroup.set("management-api")
    }
}

publishing {
    publications {
        create<MavenPublication>("federated-catalog-api") {
            artifactId = "federated-catalog-api"
            from(components["java"])
        }
    }
}
