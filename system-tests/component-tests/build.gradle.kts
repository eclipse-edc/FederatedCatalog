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
}

dependencies {
    implementation(project(":core:federated-catalog"))
    implementation(project(":extensions:api:federated-catalog-api"))
    runtimeOnly(edc.bundles.connector)

    testImplementation(edc.junit)
    testImplementation(libs.okhttp)
    testImplementation(libs.restAssured)
    testImplementation(libs.bundles.jupiter)
    testImplementation(libs.awaitility)
}
