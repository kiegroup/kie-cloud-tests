/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.images.imagestream.ImageStreamProvider;
import org.kie.cloud.openshift.deployment.MavenNexusRepositoryDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used for deploying Docker registry to OpenShift project.
 */
public class MavenRepositoryDeployer {

    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoryDeployer.class);

    private static final String NEXUS_IMAGE_STREAM_NAME = "nexus-mirror";
    private static final String NEXUS_DEPLOYMENT_NAME = "nexus";

    public static MavenRepositoryDeployment deploy(Project project, boolean shouldWait) {
        deployMavenRepository(project);

        logger.info("Waiting for Maven repository deployment to become ready.");
        MavenRepositoryDeployment mavenDeployment = new MavenNexusRepositoryDeploymentImpl(project);

        if (shouldWait) {
            mavenDeployment.waitForScale();
        }

        return mavenDeployment;
    }

    private static void deployMavenRepository(Project project) {
        logger.info("Creating internal Maven Repository.");

        // Login is part of binary retrieval
        OpenShiftBinary masterBinary = OpenShifts.masterBinary(project.getName());

        String nexusMirrorImageStream = OpenShiftConstants.getNexusMirrorImageStream();
        if (nexusMirrorImageStream != null && !nexusMirrorImageStream.isEmpty()) {
            logger.info("Mirrored Nexus docker image is provided.");
            ImageStreamProvider.createImagesFromImageStreamFile(project, nexusMirrorImageStream);
            ImageStreamProvider.waitForImageStreamCreate(project, NEXUS_IMAGE_STREAM_NAME);

            logger.info("Creating new app using template and image from {} image stream.", NEXUS_IMAGE_STREAM_NAME);
            project.processTemplateAndCreateResources(getNexusTemplate(), geNexusProperties());
        } else {
            logger.info("Mirrored Nexus docker image is not provided.");
            logger.info("Creating new app from docker image.");
            masterBinary.execute("new-app", "sonatype/nexus", "-l", "deploymentConfig=maven-nexus");
        }

        masterBinary.execute("expose", "service", NEXUS_DEPLOYMENT_NAME);
    }

    private static final String NEXUS_TEMPLATE = "/deployments/nexus.yaml";

    private static URL getNexusTemplate() {
        try {
            return new URL("file://" + MavenRepositoryDeployer.class.getResource(NEXUS_TEMPLATE).getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Wrong Nexus template location", e);
        }
    }

    private static Map<String, String> geNexusProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("APPLICATION_NAME", NEXUS_DEPLOYMENT_NAME);
        return properties;
    }
}