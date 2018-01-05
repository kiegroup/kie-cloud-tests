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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerWithExternalDatabaseScenarioImpl extends OpenShiftScenario implements KieServerWithExternalDatabaseScenario {

    private KieServerDeploymentImpl kieServerDeployment;
    private Map<String, String> envVariables;

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithExternalDatabaseScenario.class);

    public KieServerWithExternalDatabaseScenarioImpl(Map<String, String> envVariables) {
        this.envVariables = envVariables;
    }

    @Override public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override public void deploy() {
        super.deploy();

        logger.info("Creating secrets from " + OpenShiftConstants.getKieAppSecret());
        project.createResources(OpenShiftConstants.getKieAppSecret());

        logger.info("Creating image streams from " + OpenShiftConstants.getKieImageStreams());
        project.createResources(OpenShiftConstants.getKieImageStreams());

        logger.info("Processing template and creating resources from " + OpenShiftTemplate.KIE_SERVER_DATABASE_EXTERNAL.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, project.getName());
        project.processTemplateAndCreateResources(OpenShiftTemplate.KIE_SERVER_DATABASE_EXTERNAL.getTemplateUrl(), envVariables);

        kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();
    }

    @Override public List<Deployment> getDeployments() {
        return Arrays.asList(kieServerDeployment);
    }
}
