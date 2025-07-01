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
    api(project(":core:federated-catalog-core"))
    implementation(libs.edc.dsp.transform.catalog2025)

    testImplementation(libs.awaitility)
    testImplementation(libs.edc.junit)
    implementation(libs.edc.lib.json.ld)
    testImplementation(testFixtures(project(":spi:federated-catalog-spi")))
    testImplementation(testFixtures(project(":spi:crawler-spi")))
    testImplementation(testFixtures(project(":core:federated-catalog-core")))

    // required for integration test
    testFixturesImplementation(libs.edc.lib.json)
    testFixturesImplementation(libs.edc.dsp.transform.catalog.lib)
    testFixturesImplementation(libs.edc.lib.json.ld)
    testFixturesImplementation(libs.edc.controlplane.transform)
    testFixturesImplementation(libs.edc.dsp.transform.catalog2025)

}
