/*
 * Copyright 2018 JBoss by Red Hat.
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
import org.kie.cloud.api.deployment.DockerDeployment;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.images.imagestream.ImageStreamProvider;
import org.kie.cloud.openshift.deployment.DockerDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used for deploying Docker registry to OpenShift project.
 */
public class DockerRegistryDeployer {

    private static final Logger logger = LoggerFactory.getLogger(DockerRegistryDeployer.class);

    private static final String REGISTRY_IMAGE_STREAM_NAME = "registry-mirror";
    private static final String REGISTRY_DEPLOYMENT_NAME = "registry";

    public static DockerDeployment deploy(Project project) {
        deployDockerRegistry(project);

        logger.info("Waiting for Docker registry deployment to become ready.");
        DockerDeployment dockerDeployment = new DockerDeploymentImpl(project);
        dockerDeployment.waitForScale();

        return dockerDeployment;
    }

    private static void deployDockerRegistry(Project project) {
        logger.info("Creating internal Docker registry.");

        // Login is part of binary retrieval
        OpenShiftBinary masterBinary = OpenShifts.masterBinary();
        masterBinary.project(project.getName());

        String registryMirrorImageStream = OpenShiftConstants.getRegistryMirrorImageStream();
        if (registryMirrorImageStream != null && !registryMirrorImageStream.isEmpty()) {
            logger.info("Mirrored Registry docker image is provided.");
            ImageStreamProvider.createImagesFromImageStreamFile(project, registryMirrorImageStream);
            ImageStreamProvider.waitForImageStreamCreate(project, REGISTRY_IMAGE_STREAM_NAME);


            logger.info("Creating new app using template and image from {} image stream.", REGISTRY_IMAGE_STREAM_NAME);
            
            logger.info("FOR DEBUG: {}", masterBinary.execute("new-app", "--image-stream="+REGISTRY_IMAGE_STREAM_NAME, "--name=registry", "--allow-missing-imagestream-tags", "-l", "deploymentConfig=registry", "-o", "yaml"));
            project.processTemplateAndCreateResources(getRegistryTemplate(), geRegistryProperties());
        } else {
            logger.info("Mirrored Registry docker image is not provided.");
            logger.info("Creating new app from docker image.");
            masterBinary.execute("new-app", "registry:2", "-l", "deploymentConfig=registry");
        }

        masterBinary.execute("expose", "service", REGISTRY_DEPLOYMENT_NAME);
    }

    private static final String REGISTRY_TEMPLATE = "/deployments/registry.yaml";

    private static URL getRegistryTemplate() {
        try {
            return new URL("file://" + MavenRepositoryDeployer.class.getResource(REGISTRY_TEMPLATE).getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Wrong Registry template location", e);
        }
    }

    private static Map<String, String> geRegistryProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("APPLICATION_NAME", REGISTRY_DEPLOYMENT_NAME);
        return properties;
    }
}
