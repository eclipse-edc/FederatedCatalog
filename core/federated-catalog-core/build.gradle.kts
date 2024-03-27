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
    `java-test-fixtures`
}

dependencies {
    api(libs.edc.spi.core)
    api(libs.edc.spi.web)
    api(libs.edc.spi.dsp)
    api(project(":core:crawler-core"))
    api(project(":spi:federated-catalog-spi"))

    implementation(libs.edc.util)
    implementation(libs.edc.spi.jsonld)
    implementation(libs.edc.jsonld)
    implementation(libs.edc.query.lib)

    // required for integration test
    testImplementation(libs.edc.dsp.transform.catalog)
    testImplementation(libs.edc.junit)
    testImplementation(libs.edc.core.transform)
    testImplementation(libs.edc.ext.http)
    testImplementation(libs.awaitility)
}

edcBuild {
    swagger {
        apiGroup.set("management-api")
    }
}
