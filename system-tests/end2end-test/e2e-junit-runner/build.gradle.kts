/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
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
    testImplementation(project(":spi:federated-catalog-spi"))
    testImplementation(project(":core:federated-catalog-core"))
    testImplementation(libs.edc.core.connector)
    testImplementation(libs.edc.dsp.transform.catalog)
    testImplementation(libs.edc.lib.transform)
    testImplementation(libs.edc.controlplane.transform)
    testImplementation(libs.awaitility)
    testImplementation(libs.edc.api.management)
    testImplementation(libs.edc.dsp.transform.catalog)
    testImplementation(libs.edc.dsp.transform.catalog.lib)
    testImplementation(libs.edc.junit)
    testImplementation(libs.edc.lib.json.ld)
    testImplementation(libs.jackson.jsr310)
}

edcBuild {
    publish.set(false)
}
