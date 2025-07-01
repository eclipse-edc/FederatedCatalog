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
    testImplementation(project(":core:federated-catalog-core-2025"))
    testImplementation(project(":extensions:api:federated-catalog-api"))

    testImplementation(libs.edc.spi.jsonld)
    testImplementation(libs.edc.spi.catalog)
    testImplementation(libs.edc.dsp.all)
    testImplementation(libs.edc.lib.json.ld)
    testImplementation(libs.edc.core.jetty)
    testImplementation(libs.edc.junit)
    testImplementation(libs.edc.spi.dataplane.selector)

    testImplementation(libs.restAssured)
    testImplementation(libs.awaitility)

    testRuntimeOnly(libs.edc.iam.mock)
}

edcBuild {
    publish.set(false)
}
