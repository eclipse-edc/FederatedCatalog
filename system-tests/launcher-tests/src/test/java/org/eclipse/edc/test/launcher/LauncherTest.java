/*
 *  Copyright (c) 2025 Think-it GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Think-it GmbH - initial API and implementation
 *
 */

package org.eclipse.edc.test.launcher;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.testfixtures.TestUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.io.IOException;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.eclipse.edc.junit.testfixtures.TestUtils.findBuildRoot;
import static org.testcontainers.containers.wait.strategy.Wait.forHealthcheck;

@EndToEndTest
public class LauncherTest {

    @Test
    void shouldBuildAndRun() throws IOException, InterruptedException {
        var launcherName = "catalog-mocked";

        var file = new File(TestUtils.findBuildRoot(), TestUtils.GRADLE_WRAPPER);
        var command = new String[] { file.getCanonicalPath(), ":launchers:" + launcherName + ":shadowJar"};

        var exec = Runtime.getRuntime().exec(command).waitFor(1, MINUTES);
        assertThat(exec).isTrue();

        var runtime = new GenericContainer<>(new ImageFromDockerfile()
                        .withDockerfile(findBuildRoot().toPath().resolve("launchers").resolve(launcherName).resolve("Dockerfile")))
                .waitingFor(forHealthcheck())
                .withLogConsumer(f -> System.out.println(f.getUtf8StringWithoutLineEnding()));

        assertThatNoException().isThrownBy(() -> {
            runtime.start();

            runtime.stop();
        });

    }
}
