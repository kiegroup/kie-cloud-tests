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

import static org.kie.cloud.api.deployment.constants.DeploymentConstants.getDatabaseDriver;
import static org.kie.cloud.api.deployment.constants.DeploymentConstants.getDatabaseHost;
import static org.kie.cloud.api.deployment.constants.DeploymentConstants.getDatabasePassword;
import static org.kie.cloud.api.deployment.constants.DeploymentConstants.getDatabasePort;
import static org.kie.cloud.api.deployment.constants.DeploymentConstants.getDatabaseUsername;
import static org.kie.cloud.api.deployment.constants.DeploymentConstants.getExternalDatabaseName;
import static org.kie.cloud.api.deployment.constants.DeploymentConstants.getHibernatePersistenceDialect;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.scenario.KieServerWithExternalDatabaseScenarioImpl;

public class KieServerWithExternalDatabaseScenarioBuilderImpl implements KieServerWithExternalDatabaseScenarioBuilder {

    private Map<String, String> envVariables;

    public KieServerWithExternalDatabaseScenarioBuilderImpl() {
        this.envVariables = new HashMap<String, String>();
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOST, "-"); // Parameter is required by template

        this.envVariables.put(OpenShiftTemplateConstants.DBE_SERVICE_HOST, getDatabaseHost());
        this.envVariables.put(OpenShiftTemplateConstants.DBE_DRIVER, getDatabaseDriver());
        this.envVariables.put(OpenShiftTemplateConstants.DBE_SERVICE_PORT, getDatabasePort());
        this.envVariables.put(OpenShiftTemplateConstants.DBE_DATABASE, getExternalDatabaseName());
        this.envVariables.put(OpenShiftTemplateConstants.DBE_USERNAME, getDatabaseUsername());
        this.envVariables.put(OpenShiftTemplateConstants.DBE_PASSWORD, getDatabasePassword());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PERSISTENCE_DIALECT, getHibernatePersistenceDialect());
    }

    @Override public KieServerWithExternalDatabaseScenario build() {
        return new KieServerWithExternalDatabaseScenarioImpl(envVariables);
    }

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        this.envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, repoUrl);
        this.envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, repoUserName);
        this.envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, repoPassword);
        return this;
    }
}
