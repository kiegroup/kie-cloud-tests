/*
 * Copyright 2018 JBoss by Red Hat.
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
import org.kie.cloud.api.scenario.WorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerDatabasePersistentScenarioBuilder;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.scenario.WorkbenchKieServerDatabasePersistentScenarioImpl;


public class WorkbenchKieServerDatabasePersistentScenarioBuilderImpl implements WorkbenchKieServerDatabasePersistentScenarioBuilder {

    private Map<String, String> envVariables = new HashMap<>();

    public WorkbenchKieServerDatabasePersistentScenarioBuilderImpl() {
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());
    }

    @Override
    public WorkbenchKieServerDatabasePersistentScenario build() {
        return new WorkbenchKieServerDatabasePersistentScenarioImpl(envVariables);
    }

    @Override
    public WorkbenchKieServerDatabasePersistentScenarioBuilder withExternalMavenRepo(String repoUrl) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, repoUrl);
        // Maven username and password is currently hardcoded as KIE_ADMIN_USER and KIE_ADMIN_PWD until RHDM-319 is fixed.
        return this;
    }
}
