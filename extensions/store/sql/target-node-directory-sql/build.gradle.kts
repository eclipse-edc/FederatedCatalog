/*
 *  Copyright (c) 2024 Amadeus IT Group
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus IT Group - initial API and implementation
 *
 */

plugins {
    `java-library`
}

dependencies {
    api(project(":spi:crawler-spi"))
    implementation(libs.edc.lib.sql)
    implementation(libs.edc.sql.bootstrapper)
    implementation(libs.edc.spi.transaction.datasource)
    implementation(libs.edc.lib.util)

    testImplementation(libs.edc.junit)
    testImplementation(testFixtures(libs.edc.sql.test.fixtures))
    testImplementation(testFixtures(project(":spi:crawler-spi")))
}
