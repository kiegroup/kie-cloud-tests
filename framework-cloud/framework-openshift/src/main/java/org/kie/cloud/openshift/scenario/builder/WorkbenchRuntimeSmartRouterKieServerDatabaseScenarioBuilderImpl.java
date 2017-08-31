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

package org.kie.cloud.openshift.scenario.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterKieServerDatabaseScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.scenario.WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioImpl;

public class WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilderImpl implements WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder {

    private OpenShiftController openshiftController;
    private Map<String, String> envVariables;

    public WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilderImpl(OpenShiftController openShiftController) {
        this.openshiftController = openShiftController;

        this.envVariables = new HashMap<String, String>();
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());

        // By default use Workbench as maven repo, repo URL is derived from Workbench automatically if not defined
        this.envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, DeploymentConstants.getWorkbenchUser());
        this.envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, DeploymentConstants.getWorkbenchPassword());
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseScenario build() {
        return new WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioImpl(openshiftController, envVariables);
    }
}
