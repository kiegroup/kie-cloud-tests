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
import org.kie.cloud.api.scenario.builder.WorkbenchWithKieServerScenarioBuilder;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.scenario.builder.WorkbenchWithKieServerScenarioBuilderImpl;

public class DeploymentBuilderFactory implements DeploymentScenarioBuilderFactory {

    private static final String CLOUD_API_IMPLEMENTATION_NAME = "openshift";

    OpenShiftController controller;

    public DeploymentBuilderFactory() {
        controller = new OpenShiftController(OpenShiftConstants.getOpenShiftUrl(),
                OpenShiftConstants.getOpenShiftUserName(),
                OpenShiftConstants.getOpenShiftPassword());
    }

    @Override public String getCloudEnvironmentName() {
        return CLOUD_API_IMPLEMENTATION_NAME;
    }

    @Override
    public WorkbenchWithKieServerScenarioBuilder getWorkbenchWithKieServerScenarioBuilder() {
        return new WorkbenchWithKieServerScenarioBuilderImpl(controller);
    }

    @Override
    public void deleteNamespace(String namespace) {
        Project project = controller.getProject(namespace);
        project.delete();
    }
}
