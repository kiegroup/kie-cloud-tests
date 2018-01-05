/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.scenario;

import java.util.UUID;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpenShiftScenario implements DeploymentScenario {

    protected String projectName;
    protected Project project;

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftScenario.class);

    @Override
    public String getNamespace() {
        return projectName;
    }

    @Override
    public void deploy() {
        // OpenShift restriction: Hostname must be shorter than 63 characters
        projectName = UUID.randomUUID().toString().substring(0, 4);
        OpenShiftConstants.getNamespacePrefix().ifPresent(p -> projectName = p + "-" + projectName);

        logger.info("Generated project name is " + projectName);

        logger.info("Creating project " + projectName);
        project = OpenShiftController.createProject(projectName);
    }

    @Override
    public void undeploy() {
        try {
            InstanceLogUtil.writeDeploymentLogs(this);

            for (Deployment deployment : getDeployments()) {
                if (deployment != null && deployment.isReady()) {
                    deployment.scale(0);
                    deployment.waitForScale();
                }
            }

            project.delete();
            project.close();
        } catch (Exception e) {
            throw new RuntimeException("Error while undeploying scenario.", e);
        }
    }
}
