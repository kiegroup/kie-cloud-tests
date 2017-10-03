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

package org.kie.cloud.openshift;

import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.builder.GenericScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchWithKieServerScenarioBuilder;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchSettingsBuilder;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.scenario.builder.GenericScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.KieServerWithExternalDatabaseScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.WorkbenchWithKieServerScenarioBuilderImpl;
import org.kie.cloud.openshift.settings.builder.KieServerS2ISettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.WorkbenchSettingsBuilderImpl;

public class DeploymentBuilderFactory implements DeploymentScenarioBuilderFactory {

    private static final String CLOUD_API_IMPLEMENTATION_NAME = "openshift";

    OpenShiftController controller;

    public DeploymentBuilderFactory() {
        controller = new OpenShiftController(OpenShiftConstants.getOpenShiftUrl(),
                OpenShiftConstants.getOpenShiftUserName(),
                OpenShiftConstants.getOpenShiftPassword());
    }

    @Override public String getCloudAPIImplementationName() {
        return CLOUD_API_IMPLEMENTATION_NAME;
    }

    @Override
    public WorkbenchWithKieServerScenarioBuilder getWorkbenchWithKieServerScenarioBuilder() {
        return new WorkbenchWithKieServerScenarioBuilderImpl(controller);
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder getWorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder() {
        return new WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilderImpl(controller);
    }

    @Override public KieServerWithExternalDatabaseScenarioBuilder getKieServerWithExternalDatabaseScenarioBuilder() {
        return new KieServerWithExternalDatabaseScenarioBuilderImpl(controller);
    }

    @Override
    public GenericScenarioBuilder getGenericScenarioBuilder() {
        return new GenericScenarioBuilderImpl(controller);
    }

    @Override
    public KieServerS2ISettingsBuilder getKieServerS2ISettingsBuilder() {
        return new KieServerS2ISettingsBuilderImpl();
    }

    @Override
    public WorkbenchSettingsBuilder getWorkbenchSettingsBuilder() {
        return new WorkbenchSettingsBuilderImpl();
    }

    @Override
    public void deleteNamespace(String namespace) {
        Project project = controller.getProject(namespace);
        project.delete();
    }
}
