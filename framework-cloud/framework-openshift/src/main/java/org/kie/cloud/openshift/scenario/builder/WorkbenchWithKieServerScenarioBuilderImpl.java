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

import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchWithKieServerScenarioBuilder;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.scenario.WorkbenchWithKieServerScenarioImpl;

public class WorkbenchWithKieServerScenarioBuilderImpl implements WorkbenchWithKieServerScenarioBuilder {

    private OpenShiftController openshiftController;
    private Map<String, String> envVariables;

    public WorkbenchWithKieServerScenarioBuilderImpl(OpenShiftController openShiftController) {
        this.openshiftController = openShiftController;

        this.envVariables = new HashMap<String, String>();
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, OpenShiftConstants.getKieServerUser());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, OpenShiftConstants.getKieServerPassword());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, OpenShiftConstants.getWorkbenchUser());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, OpenShiftConstants.getWorkbenchPassword());

        // By default use Workbench as maven repo, repo URL is derived from Workbench automatically if not defined
        this.envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, OpenShiftConstants.getWorkbenchUser());
        this.envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, OpenShiftConstants.getWorkbenchPassword());
    }

    @Override
    public WorkbenchWithKieServerScenario build() {
        return new WorkbenchWithKieServerScenarioImpl(openshiftController, envVariables);
    }

    @Override
    public WorkbenchWithKieServerScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, repoUrl);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, repoUserName);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, repoPassword);
        return this;
    }
}
