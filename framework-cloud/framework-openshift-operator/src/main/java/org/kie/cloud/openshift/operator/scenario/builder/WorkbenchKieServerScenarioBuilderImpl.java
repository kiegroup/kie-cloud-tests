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

package org.kie.cloud.openshift.operator.scenario.builder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.openshift.constants.ImageEnvVariables;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorEnvironments;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.CommonConfig;
import org.kie.cloud.openshift.operator.model.components.Console;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.scenario.WorkbenchKieServerScenarioImpl;

public class WorkbenchKieServerScenarioBuilderImpl implements WorkbenchKieServerScenarioBuilder {

    private KieApp kieApp = new KieApp();

    public WorkbenchKieServerScenarioBuilderImpl() {
        List<Env> authenticationEnvVars = new ArrayList<>();
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_SERVER_USER, DeploymentConstants.getKieServerUser()));
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()));

        kieApp.getMetadata().setName(OpenShiftConstants.getKieApplicationName());
        kieApp.getSpec().setEnvironment(OpenShiftOperatorEnvironments.TRIAL);

        CommonConfig commonConfig = new CommonConfig();
        commonConfig.setAdminPassword(DeploymentConstants.getWorkbenchPassword());
        commonConfig.setServerPassword(DeploymentConstants.getKieServerPassword());
        kieApp.getSpec().setCommonConfig(commonConfig);

        Server server = new Server();
        server.addEnvs(authenticationEnvVars);
        kieApp.getSpec().getObjects().addServer(server);

        Console console = new Console();
        console.addEnvs(authenticationEnvVars);
        kieApp.getSpec().getObjects().setConsole(console);
    }

    @Override
    public WorkbenchKieServerScenario build() {
        return new WorkbenchKieServerScenarioImpl(kieApp);
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_URL, repoUrl));
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_USERNAME, repoUserName));
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_PASSWORD, repoPassword));
        }
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withKieServerId(String kieServerId) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.KIE_SERVER_ID, kieServerId));
        }
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowCredentials(boolean allowCredentials) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowHeaders(String... allowedHeaders) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowMethods(String... allowedMethods) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowOrigin(String url) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlMaxAge(Duration maxAge) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
