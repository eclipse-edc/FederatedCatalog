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
    api(libs.edc.spi.contract)
    api(project(":spi:crawler-spi"))

    // required for integration test
    testImplementation(libs.edc.junit)
    testImplementation(libs.edc.dsp.transform.catalog)
    testImplementation(libs.edc.ext.http)
    testImplementation(libs.awaitility)
}
