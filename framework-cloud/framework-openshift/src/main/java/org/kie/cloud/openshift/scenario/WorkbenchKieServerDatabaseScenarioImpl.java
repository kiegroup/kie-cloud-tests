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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerDatabaseScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchKieServerDatabaseScenarioImpl extends OpenShiftScenario implements WorkbenchKieServerDatabaseScenario {

    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private DatabaseDeploymentImpl databaseDeployment;

    private Map<String, String> envVariables;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerDatabaseScenarioImpl.class);

    public WorkbenchKieServerDatabaseScenarioImpl(Map<String, String> envVariables) {
        this.envVariables = envVariables;
    }

    @Override
    public void deploy() {
        super.deploy();

        logger.info("Creating secrets from " + OpenShiftConstants.getKieAppSecret());
        project.createResources(OpenShiftConstants.getKieAppSecret());

        logger.info("Creating image streams from " + OpenShiftConstants.getKieImageStreams());
        project.createResources(OpenShiftConstants.getKieImageStreams());

        logger.info("Processing template and creating resources from " + OpenShiftTemplate.WORKBENCH_KIE_SERVER_DATABASE.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(OpenShiftTemplate.WORKBENCH_KIE_SERVER_DATABASE.getTemplateUrl(), envVariables);

        workbenchDeployment = new WorkbenchDeploymentImpl(project);
        workbenchDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());

        kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());

        databaseDeployment = new DatabaseDeploymentImpl(project);

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logger.info("Waiting for Kie server to register itself to the Workbench.");
        KieServerControllerClientProvider.waitForServerTemplateCreation(workbenchDeployment, 1);

        logger.info("Waiting for Database deployment to become ready.");
        databaseDeployment.waitForScale();

        // Used to track persistent volume content due to issues with volume cleanup
        storeProjectInfoToPersistentVolume(workbenchDeployment, "/opt/eap/standalone/data/bpmsuite");
    }

    @Override
    public WorkbenchDeployment getWorkbenchDeployment() {
        return workbenchDeployment;
    }

    @Override
    public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override
    public DatabaseDeployment getDatabaseDeployment() {
        return databaseDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        return Arrays.asList(workbenchDeployment, kieServerDeployment, databaseDeployment);
    }

    private void storeProjectInfoToPersistentVolume(Deployment deployment, String persistentVolumeMountPath) {
        String storeCommand = "echo \"Project " + projectName + ", time " + Instant.now() + "\" > " + persistentVolumeMountPath + "/info.txt";
        workbenchDeployment.getInstances().get(0).runCommand("/bin/bash", "-c", storeCommand);
    }
}
