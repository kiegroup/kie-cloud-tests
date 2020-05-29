/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.commons.io.FilenameUtils;
import org.kie.cloud.api.deployment.DockerDeployment;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.database.driver.ExternalDriver;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use cekit to create a custom database image.
 */
public class CustomDatabaseImageBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CustomDatabaseImageBuilder.class);

    private CustomDatabaseImageBuilder() {

    }

    /**
     * Create image stream from external image with driver and reference it for custom resource.
     * @param project where to create the image stream.
     * @param dockerDeployment registry to use.
     * @param externalDriver database driver to use.
     * @return the created image tag.
     */
    public static final String build(Project project, DockerDeployment dockerDeployment, ExternalDriver externalDriver) {
        downloadJdbcDriver(externalDriver);
        installDriverImageToRegistry(dockerDeployment, externalDriver);
        createDriverImageStreams(project, dockerDeployment, externalDriver);
        return externalDriver.getImageName() + ":" + externalDriver.getImageVersion();
    }

    private static void downloadJdbcDriver(ExternalDriver externalDriver) {
        externalDriver.getJdbcDriverUrl().ifPresent(jdbcDriverUrl -> {
            File jdbcDriverFile = getJdbcDriverFile(jdbcDriverUrl);
            if (jdbcDriverFile.exists()) {
                return;
            }

            logger.info("Downloading JDBC driver from {} to {}", jdbcDriverUrl, jdbcDriverFile.getAbsolutePath());
            try (ReadableByteChannel rbc = Channels.newChannel(new URL(jdbcDriverUrl).openStream());
                    FileOutputStream fos = new FileOutputStream(jdbcDriverFile)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                throw new RuntimeException("Error while downloading driver binary.", e);
            }
        });

    }

    private static File getJdbcDriverFile(String jdbcDriverUrl) {
        String filename = FilenameUtils.getName(jdbcDriverUrl);
        return new File(OpenShiftConstants.getKieJdbcDriverScriptsFolder(), filename);
    }

    private static String getCekitCommand(ExternalDriver externalDriver) {
        String buildCommand = externalDriver.getCekitImageBuildCommand();
        if (externalDriver.getJdbcDriverUrl().isPresent()) {
            String artifact = getJdbcDriverFile(externalDriver.getJdbcDriverUrl().get()).getAbsolutePath();
            String version = externalDriver.getImageVersion();
            buildCommand += String.format(" artifact=%s version=%s", artifact, version);
        }

        return buildCommand;
    }

    private static void installDriverImageToRegistry(DockerDeployment dockerDeployment, ExternalDriver externalDriver) {
        File kieJdbcDriverScriptsFolder = OpenShiftConstants.getKieJdbcDriverScriptsFolder();
        String buildCommand = getCekitCommand(externalDriver);
        String sourceDockerTag = externalDriver.getSourceDockerTag();
        String targetDockerTag = externalDriver.getTargetDockerTag(dockerDeployment.getUrl());

        try (ProcessExecutor processExecutor = new ProcessExecutor()) {
            logger.info("Building JDBC driver image");
            processExecutor.executeProcessCommand(buildCommand, kieJdbcDriverScriptsFolder.toPath());

            logger.info("Pushing JDBC driver image to Docker registry.");
            processExecutor.executeProcessCommand("docker tag " + sourceDockerTag + " " + targetDockerTag);
            processExecutor.executeProcessCommand("docker push " + targetDockerTag);
        }
    }

    private static void createDriverImageStreams(Project project, DockerDeployment dockerDeployment, ExternalDriver externalDriver) {
        String imageStreamName = externalDriver.getImageName();
        String dockerTag = externalDriver.getTargetDockerTag(dockerDeployment.getUrl());

        project.createImageStreamFromInsecureRegistry(imageStreamName, dockerTag);
    }
}
