/*
 * Copyright 2017 JBoss by Red Hat.
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
package org.kie.cloud.openshift.scenario.builder;

import java.util.HashMap;
import java.util.Map;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.scenario.builder.GenericScenarioBuilder;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ScenarioConstants;
import org.kie.cloud.openshift.scenario.GenericScenarioImpl;

public class GenericScenarioBuilderImpl implements GenericScenarioBuilder {

    private OpenShiftController openshiftController;
    private Map<String, String> envVariables;

    private Map<ScenarioConstants, String> kieAppTemplates;

    public GenericScenarioBuilderImpl(OpenShiftController openShiftController) {
        this.openshiftController = openShiftController;
        this.envVariables = new HashMap<>();
        this.kieAppTemplates = new HashMap<>();
    }

    @Override
    public GenericScenario build() {
        return new GenericScenarioImpl(openshiftController, envVariables, kieAppTemplates);
    }

    @Override
    public GenericScenarioBuilder withKieServerS2I(String kieContainerDeployment, String gitRepoUrl, String gitReference, String gitContextDir) {
        return withKieServerS2I(false, false, kieContainerDeployment, gitRepoUrl, gitReference, gitContextDir);
    }

    @Override
    public GenericScenarioBuilder withKieServerS2I(boolean managedMode, boolean connectToSmartRouter, String kieContainerDeployment, String gitRepoUrl, String gitReference, String gitContextDir) {
        if (kieAppTemplates.containsKey(ScenarioConstants.KIE_SERVER_TEMPLATE_KEY)) {
            throw new RuntimeException("Kie server is already in scenario.");
        }
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());

        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment);
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_URL, gitRepoUrl);
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_REF, gitReference);
        envVariables.put(OpenShiftTemplateConstants.CONTEXT_DIR, gitContextDir);

        if (managedMode) {
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, DeploymentConstants.getControllerUser());
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, DeploymentConstants.getControllerPassword());
        } else {
            //set empty values for controller env. variables
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_SERVICE, "");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_HOST, "");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PORT, "");
        }

        if (!connectToSmartRouter) {
            //set empty values for smart router env. variables
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_SERVICE ,"");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HOST ,"");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_PORT ,"");
        }

        kieAppTemplates.put(ScenarioConstants.KIE_SERVER_TEMPLATE_KEY, OpenShiftConstants.getKieAppTemplateKieServerS2I());
        return this;
    }

    @Override
    public GenericScenarioBuilder withExternalDatabaseForKieServer() {
        if (kieAppTemplates.containsKey(ScenarioConstants.DATABASE_TEMPLATE_KEY)) {
            throw new RuntimeException("Database is already in scenario.");
        }
        envVariables.put(OpenShiftTemplateConstants.DB_HOST, DeploymentConstants.getDatabaseHost());
        envVariables.put(OpenShiftTemplateConstants.DB_DATABASE, DeploymentConstants.getExternalDatabaseName());
        envVariables.put(OpenShiftTemplateConstants.DB_USERNAME, DeploymentConstants.getDatabaseUsername());
        envVariables.put(OpenShiftTemplateConstants.DB_PASSWORD, DeploymentConstants.getDatabasePassword());

        kieAppTemplates.put(ScenarioConstants.DATABASE_TEMPLATE_KEY, null);
        return this;
    }

    @Override
    public GenericScenarioBuilder withWorkbench() {
        if (kieAppTemplates.containsKey(ScenarioConstants.WORKBENCH_TEMPLATE_KEY)) {
            throw new RuntimeException("Workbench is already in scenario.");
        }
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());

        // By default use Workbench as maven repo, repo URL is derived from Workbench automatically if not defined
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, DeploymentConstants.getWorkbenchUser());
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, DeploymentConstants.getWorkbenchPassword());

        kieAppTemplates.put(ScenarioConstants.WORKBENCH_TEMPLATE_KEY, OpenShiftConstants.getKieAppTemplateWorkbench());
        return this;
    }

}
