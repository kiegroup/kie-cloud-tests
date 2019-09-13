/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ProjectSpecificPropertyNames;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.template.ProjectProfile;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredWorkbenchKieServerPersistentScenarioImpl extends KieCommonScenario<ClusteredWorkbenchKieServerPersistentScenario> implements ClusteredWorkbenchKieServerPersistentScenario {

    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private SsoDeployment ssoDeployment;

    private boolean deploySso;
    private final ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithExternalDatabaseScenario.class);

    public ClusteredWorkbenchKieServerPersistentScenarioImpl(Map<String, String> envVariables, boolean deploySso) {
        super(envVariables);
        this.deploySso = deploySso;
    }

    public ClusteredWorkbenchKieServerPersistentScenarioImpl(Map<String, String> envVariables) {
        this(envVariables, false);
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
    protected void deployKieDeployments() {
        if (deploySso) {
            ssoDeployment = SsoDeployer.deploy(project);

            envVariables.put(OpenShiftTemplateConstants.SSO_URL, SsoDeployer.createSsoEnvVariable(ssoDeployment.getUrl().toString()));
            envVariables.put(OpenShiftTemplateConstants.SSO_REALM, DeploymentConstants.getSsoRealm());

            ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
            envVariables.put(propertyNames.workbenchSsoClient(), projectProfile.getWorkbenchName() + "-client");
            envVariables.put(propertyNames.workbenchSsoSecret(), projectProfile.getWorkbenchName() + "-secret");

            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_CLIENT, "kie-server-client");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_SECRET, "kie-server-secret");
        }

        logger.info("Processing template and creating resources from " + OpenShiftTemplate.CLUSTERED_WORKBENCH_KIE_SERVER_PERSISTENT.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, project.getName());
        envVariables.put(OpenShiftTemplateConstants.AMQ_IMAGE_STREAM_NAMESPACE, project.getName());
        envVariables.put("AMQ_REPLICAS","1");
        project.processTemplateAndCreateResources(OpenShiftTemplate.CLUSTERED_WORKBENCH_KIE_SERVER_PERSISTENT.getTemplateUrl(), envVariables);

        workbenchDeployment = new WorkbenchDeploymentImpl(project);
        workbenchDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());
        workbenchDeployment.scale(1); //- TODO remove this to tes HA scenarios

        kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.scale(1);

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logNodeNameOfAllInstances();
    }

    @Override public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchDeployment, kieServerDeployment, ssoDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Arrays.asList(workbenchDeployment);
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Arrays.asList(kieServerDeployment);
    }

    @Override
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return Collections.emptyList();
    }

    @Override
    public List<ControllerDeployment> getControllerDeployments() {
        return Collections.emptyList();
    }

    @Override
    public SsoDeployment getSsoDeployment() {
        return ssoDeployment;
    }
}
