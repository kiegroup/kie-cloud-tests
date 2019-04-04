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
import org.kie.cloud.openshift.operator.constants.ProjectSpecificPropertyNames;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Console;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.SmartRouter;
import org.kie.cloud.openshift.operator.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl;

public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl implements ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder {

    private KieApp kieApp = new KieApp();
    private final ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();

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
        kieApp.getSpec().getObjects().getSmartRouter().addEnv(new Env(ImageEnvVariables.KIE_SERVER_ROUTER_ID, smartRouterId));
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withTimerServiceDataStoreRefreshInterval(Duration timerServiceDataStoreRefreshInterval) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.TIMER_SERVICE_DATA_STORE_REFRESH_INTERVAL, Long.toString(timerServiceDataStoreRefreshInterval.toMillis())));
        }
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder deploySso() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withBusinessCentralMavenUser(String user, String password) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.KIE_MAVEN_USER, user));
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.KIE_MAVEN_PWD, password));

        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(propertyNames.workbenchMavenUserName(), user));
            server.addEnv(new Env(propertyNames.workbenchMavenPassword(), password));
        }
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer1Hostname(String hostname) {
        kieApp.getSpec().getObjects().getServers()[0].addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTP, hostname));
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer2Hostname(String hostname) {
        kieApp.getSpec().getObjects().getServers()[1].addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTP, hostname));
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer1Hostname(String hostname) {
        kieApp.getSpec().getObjects().getServers()[0].addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTPS, hostname));
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer2Hostname(String hostname) {
        kieApp.getSpec().getObjects().getServers()[1].addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTPS, hostname));
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTPS, hostname));
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTP, hostname));
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
