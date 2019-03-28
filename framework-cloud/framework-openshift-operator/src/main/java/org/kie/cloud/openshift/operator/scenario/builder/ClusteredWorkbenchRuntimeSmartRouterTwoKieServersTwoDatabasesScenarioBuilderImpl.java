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
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.ImageEnvVariables;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorEnvironments;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Console;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.SmartRouter;
import org.kie.cloud.openshift.operator.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl;

public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl implements ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder {

    private KieApp kieApp = new KieApp();

    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl() {
        List<Env> authenticationEnvVars = new ArrayList<>();
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_SERVER_USER, DeploymentConstants.getKieServerUser()));
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword()));
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()));
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword()));

        kieApp.getMetadata().setName(OpenShiftConstants.getKieApplicationName());
        kieApp.getSpec().setEnvironment(OpenShiftOperatorEnvironments.PRODUCTION);

        Server server = new Server();
        server.addEnvs(authenticationEnvVars);
        // TODO: Workaround until Maven repo with released artifacts is implemented
        server.addEnv(new Env(ImageEnvVariables.KIE_SERVER_MODE, "DEVELOPMENT"));
        server.setDeployments(2);
        server.setReplicas(1);
        kieApp.getSpec().getObjects().addServer(server);

        Console console = new Console();
        console.addEnvs(authenticationEnvVars);
        console.setReplicas(1);
        kieApp.getSpec().getObjects().setConsole(console);

        // Instantiate Smart router as it needs to be configured for custom secret
        kieApp.getSpec().getObjects().setSmartRouter(new SmartRouter());
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario build() {
        return new ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl(kieApp);
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_URL, repoUrl));
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_USERNAME, repoUserName));
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_PASSWORD, repoPassword));
        }
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withSmartRouterId(String smartRouterId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withTimerServiceDataStoreRefreshInterval(Duration timerServiceDataStoreRefreshInterval) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder deploySso() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withBusinessCentralMavenUser(String user, String password) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer1Hostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer2Hostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer1Hostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer2Hostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
