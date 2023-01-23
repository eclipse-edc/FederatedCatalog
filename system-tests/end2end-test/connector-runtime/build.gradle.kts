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
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {

    runtimeOnly(edc.core.controlplane)
    runtimeOnly(edc.api.observability)
    runtimeOnly(edc.api.management)
    runtimeOnly(edc.config.filesystem)
    runtimeOnly(edc.ext.http)

    // IDS
    runtimeOnly(edc.ids)
    runtimeOnly(edc.iam.mock)

    // Embedded DPF
    runtimeOnly(edc.bundles.dpf)
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
