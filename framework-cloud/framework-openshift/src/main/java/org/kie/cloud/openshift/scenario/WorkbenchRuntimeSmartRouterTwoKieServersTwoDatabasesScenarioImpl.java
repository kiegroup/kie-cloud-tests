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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.kie.cloud.api.deployment.ControllerDeployment;

import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SSODeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.SSODeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl extends OpenShiftScenario implements WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario {

    private WorkbenchDeployment workbenchRuntimeDeployment;
    private SmartRouterDeployment smartRouterDeployment;
    private KieServerDeploymentImpl kieServerOneDeployment;
    private KieServerDeploymentImpl kieServerTwoDeployment;
    private DatabaseDeploymentImpl databaseOneDeployment;
    private DatabaseDeploymentImpl databaseTwoDeployment;
    private SSODeployment ssoDeployment;

    private Map<String, String> envVariables;
    private boolean deploySSO;
    private Map<String, String> ssoEnvVariables;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl.class);

    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl(Map<String, String> envVariables) {
        this(envVariables,false,Collections.EMPTY_MAP);
    }
    
        public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl(Map<String, String> envVariables, boolean deploySSO, Map<String, String> ssoEnvVariables) {
        this.envVariables = envVariables;
        this.deploySSO = deploySSO;
        this.ssoEnvVariables = ssoEnvVariables;
    }

    @Override
    public void deploy() {
        super.deploy();

        if (deploySSO) {
            ssoDeployment = SSODeployer.deploy(project, envVariables, ssoEnvVariables);

            envVariables.put(OpenShiftTemplateConstants.SSO_URL, SSODeployer.createSSOEnvVariable(ssoDeployment.getUrl().toString()));
            envVariables.put(OpenShiftTemplateConstants.SSO_REALM, DeploymentConstants.gettSSORealm());
            envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_SSO_CLIENT, "business-central-client");
            envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_SSO_SECRET, "business-central-secret");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER1_SSO_CLIENT, "kie-server1-client");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER1_SSO_SECRET, "kie-server1-secret");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER2_SSO_CLIENT, "kie-server2-client");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER2_SSO_SECRET, "kie-server2-secret");
        }
        
        logger.info("Processing template and creating resources from " + OpenShiftTemplate.CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(OpenShiftTemplate.CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES.getTemplateUrl(), envVariables);

        workbenchRuntimeDeployment = createWorkbenchRuntimeDeployment(project);
        smartRouterDeployment = createSmartRouterDeployment(project);
        kieServerOneDeployment = createKieServerDeployment(project, "1");
        kieServerTwoDeployment = createKieServerDeployment(project, "2");
        databaseOneDeployment = createDatabaseDeployment(project, "1");
        databaseTwoDeployment = createDatabaseDeployment(project, "2");

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchRuntimeDeployment.waitForScale();

        logger.info("Waiting for Smart router deployment to become ready.");
        smartRouterDeployment.waitForScale();

        logger.info("Waiting for Kie server one deployment to become ready.");
        kieServerOneDeployment.waitForScale();

        logger.info("Waiting for Kie server two deployment to become ready.");
        kieServerTwoDeployment.waitForScale();

        logger.info("Waiting for Database one deployment to become ready.");
        databaseOneDeployment.waitForScale();

        logger.info("Waiting for Database two deployment to become ready.");
        databaseTwoDeployment.waitForScale();

        logger.info("Waiting for Kie servers and Smart router to register itself to the Workbench.");
        KieServerControllerClientProvider.waitForServerTemplateCreation(workbenchRuntimeDeployment, 3);

        logNodeNameOfAllInstances();
    }

    @Override
    public WorkbenchDeployment getWorkbenchRuntimeDeployment() {
        return workbenchRuntimeDeployment;
    }

    @Override
    public SmartRouterDeployment getSmartRouterDeployment() {
        return smartRouterDeployment;
    }

    @Override
    public KieServerDeployment getKieServerOneDeployment() {
        return kieServerOneDeployment;
    }

    @Override
    public KieServerDeployment getKieServerTwoDeployment() {
        return kieServerTwoDeployment;
    }

    @Override
    public DatabaseDeployment getDatabaseOneDeployment() {
        return databaseOneDeployment;
    }

    @Override
    public DatabaseDeployment getDatabaseTwoDeployment() {
        return databaseTwoDeployment;
    }

    @Override
    public SSODeployment getSSODeployment() {
        return ssoDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchRuntimeDeployment, smartRouterDeployment, kieServerOneDeployment, kieServerTwoDeployment, databaseOneDeployment, databaseTwoDeployment, ssoDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    private WorkbenchDeployment createWorkbenchRuntimeDeployment(Project project) {
        WorkbenchRuntimeDeploymentImpl workbenchRuntimeDeployment = new WorkbenchRuntimeDeploymentImpl(project);
        workbenchRuntimeDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchRuntimeDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());

        return workbenchRuntimeDeployment;
    }

    private SmartRouterDeployment createSmartRouterDeployment(Project project) {
        SmartRouterDeploymentImpl smartRouterDeployment = new SmartRouterDeploymentImpl(project);
        smartRouterDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

        return smartRouterDeployment;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project, String kieServerSuffix) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.setServiceSuffix("-" + kieServerSuffix);

        return kieServerDeployment;
    }

    private DatabaseDeploymentImpl createDatabaseDeployment(Project project, String databaseSuffix) {
        DatabaseDeploymentImpl databaseDeployment = new DatabaseDeploymentImpl(project);
        databaseDeployment.setServiceSuffix("-" + databaseSuffix);
        return databaseDeployment;
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Arrays.asList(workbenchRuntimeDeployment);
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Arrays.asList(kieServerOneDeployment, kieServerTwoDeployment);
    }

    @Override
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return Arrays.asList(smartRouterDeployment);
    }

    @Override
    public List<ControllerDeployment> getControllerDeployments() {
        return Collections.emptyList();
    }
}
