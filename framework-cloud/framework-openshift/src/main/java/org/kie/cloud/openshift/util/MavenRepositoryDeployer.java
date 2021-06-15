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
import java.util.concurrent.TimeUnit;

import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.ImageStream;
import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
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
            logger.info("Creating image streams from {}", nexusMirrorImageStream);
            project.createResourcesFromYamlAsAdmin(nexusMirrorImageStream);
            new SimpleWaiter(() -> isImageStreamCreated(project, NEXUS_IMAGE_STREAM_NAME))
                        .timeout(TimeUnit.SECONDS, 30)
                        .reason("Waiting for Nexus mirrored image stream to be created")
                        .waitFor();


            logger.info("Creating new app from image stream.");
            
            logger.info("DEBUG oc version: {}", masterBinary.execute("version"));
            logger.info("FOR DEBUG: {}", masterBinary.execute("new-app", "--image-stream="+NEXUS_IMAGE_STREAM_NAME, "--name="+NEXUS_DEPLOYMENT_NAME, "--allow-missing-imagestream-tags", "-l", "deploymentConfig=maven-nexus", "-o", "yaml"));
            /*masterBinary.execute("new-app", "nexus", "--name=nexus", "-l", "deploymentConfig=maven-nexus", "-o", "yaml", "|", "oc", "apply", "-f", "-");
            */

            masterBinary.execute("new-app", "--image-stream="+NEXUS_IMAGE_STREAM_NAME, "--name="+NEXUS_DEPLOYMENT_NAME, "--allow-missing-imagestream-tags", "-l", "deploymentConfig=maven-nexus");

            logger.info("Wait and check if {} service is created, if not create one.", NEXUS_DEPLOYMENT_NAME);
            new SimpleWaiter(()->isNexusServiceCreated(project, NEXUS_DEPLOYMENT_NAME))
                        .timeout(TimeUnit.SECONDS, 30)
                        .onTimeout(()->createNexusService(project))
                        .waitFor();
        } else {
            logger.info("Mirrored Nexus docker image is not provided.");
            logger.info("Creating new app from docker image.");
            masterBinary.execute("new-app", "sonatype/nexus", "-l", "deploymentConfig=maven-nexus");
        }

        masterBinary.execute("expose", "service", NEXUS_DEPLOYMENT_NAME);
    }

    private static boolean isImageStreamCreated(Project project, String name) {
        return project.getOpenShiftAdmin()
                  .getImageStreams()
                  .stream()
                  .map(ImageStream::getMetadata)
                  .map(ObjectMeta::getName)
                  .anyMatch(name::equals);
    }

    private static boolean isNexusServiceCreated(Project project, String name) {
        return project.getOpenShiftAdmin()
                    .getServices()
                    .stream()
                    .map(Service::getMetadata)
                    .map(ObjectMeta::getName)
                    .anyMatch(name::equals);
    }

    private static void createNexusService(Project project) {
        boolean isNexusPodAvailable = project.getOpenShiftAdmin()
                                             .getPods()
                                             .stream()
                                             .map(Pod::getMetadata)
                                             .map(ObjectMeta::getName)
                                             .filter(name->!name.endsWith("deploy"))
                                             .anyMatch(NEXUS_DEPLOYMENT_NAME::contains);
        if (isNexusPodAvailable) {
            project.createResourcesFromYaml(getNexusService().toString());
        } else {
            throw new RuntimeException("Nexus pods is not available in project "+ project.getName());
        }


    }

    private static final String NEXUS_SERVICE = "/deployments/nexusService.yaml";
    private static URL getNexusService() {
        try {
            return new URL("file://" + MavenRepositoryDeployer.class.getResource(NEXUS_SERVICE).getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Wrong Nexus service file location", e);
        }
    }
}