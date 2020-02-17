/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.scenario.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ProjectSpecificPropertyNames;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioImpl;

public class ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderTemplates<ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario> implements
                                                                                    ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder {

    private final Map<String, String> envVariables = new HashMap<>();

    public ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilderImpl() {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, DeploymentConstants.getControllerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, DeploymentConstants.getControllerPassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());

        ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();
        envVariables.put(propertyNames.workbenchHttpsSecret(), OpenShiftConstants.getKieApplicationSecretName());

        // TODO: Workaround until Maven repo with released artifacts is implemented
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_MODE, "DEVELOPMENT");
    }

    @Override
    public ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario getDeploymentScenarioInstance() {
        return new ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioImpl(envVariables);
    }

    @Override
    public ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }
}
