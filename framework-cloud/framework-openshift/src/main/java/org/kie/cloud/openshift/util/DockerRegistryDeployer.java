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

import java.io.IOException;

import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import org.kie.cloud.api.deployment.DockerDeployment;
import org.kie.cloud.openshift.deployment.DockerDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used for deploying Docker registry to OpenShift project.
 */
public class DockerRegistryDeployer {

    private static final Logger logger = LoggerFactory.getLogger(DockerRegistryDeployer.class);

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
        masterBinary.execute("new-app", "registry:2", "-l", "deploymentConfig=registry");
        masterBinary.execute("expose", "service", "registry");
    }
}
