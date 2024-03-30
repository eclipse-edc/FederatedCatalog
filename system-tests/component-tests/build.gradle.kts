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
    implementation(project(":core:federated-catalog-core"))
    implementation(project(":extensions:api:federated-catalog-api"))
    runtimeOnly(libs.bundles.edc.connector)
    runtimeOnly(libs.edc.core.transform)
    runtimeOnly(libs.edc.core.controlplane)
    runtimeOnly(libs.edc.core.dataPlane.selector)


    testImplementation(libs.edc.junit)
    testImplementation(libs.restAssured)
    testImplementation(libs.awaitility)

    testImplementation(libs.edc.spi.jsonld)
    testImplementation(libs.edc.spi.catalog)
    testImplementation(libs.edc.dsp.all)
    testImplementation(libs.edc.json.ld.lib)
    testRuntimeOnly(libs.edc.iam.mock)
}

edcBuild {
    publish.set(false)
}
