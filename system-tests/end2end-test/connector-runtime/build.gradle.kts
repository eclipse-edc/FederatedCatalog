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
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {

    runtimeOnly(libs.edc.core.controlplane)
    runtimeOnly(libs.edc.core.dataPlane.selector)
    runtimeOnly(libs.edc.api.observability)
    runtimeOnly(libs.edc.api.management)
    runtimeOnly(libs.edc.config.filesystem)
    runtimeOnly(libs.edc.ext.http)

    // IDS
    runtimeOnly(libs.edc.ids)
    runtimeOnly(libs.edc.iam.mock)

    // Embedded DPF
    runtimeOnly(libs.bundles.edc.dpf)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("app.jar")
}

edcBuild {
    publish.set(false)
}
