/*
 * Copyright 2020 JBoss by Red Hat.
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
import java.util.Collections;
import java.util.Map;

import org.kie.cloud.api.deployment.GogsDeployment;
import org.kie.cloud.openshift.deployment.GogsDeploymentImpl;
import org.kie.cloud.openshift.resource.CloudProperties;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used for deploying Gogs instance to OpenShift project.
 */
public class GogsDeployer {

    private static final String GOGS_TEMPLATE = "/deployments/gogs.yaml";

    private static final Logger logger = LoggerFactory.getLogger(GogsDeployer.class);

    public static GogsDeployment deploy(Project project) {
        logger.info("Creating internal GOGS instance.");
        project.runOcCommandAsAdmin("adm", "policy", "add-scc-to-user", "anyuid", "-z", "default");
        project.processTemplateAndCreateResources(getGogsTemplate(), getGogsProperties(project));
        project.runOcCommandAsAdmin("expose", "service", "gogs");

        logger.info("Waiting for Gogs deployment to become ready.");
        GogsDeployment deployment = new GogsDeploymentImpl(project);
        deployment.waitForScale();

        return deployment;
    }

    private static Map<String, String> getGogsProperties(Project project) {
        return Collections.singletonMap("GOGS_DOCKER_IMAGE", CloudProperties.getInstance().getGogsDockerImage());
    }

    private static URL getGogsTemplate() {
        try {
            return new URL("file://" + GogsDeployer.class.getResource(GOGS_TEMPLATE).getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Wrong GOGS template location", e);
        }
    }
}
