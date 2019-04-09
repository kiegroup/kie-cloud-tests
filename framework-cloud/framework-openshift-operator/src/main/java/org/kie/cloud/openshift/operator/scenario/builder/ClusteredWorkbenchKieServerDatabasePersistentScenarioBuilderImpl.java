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

package org.kie.cloud.openshift.operator.scenario.builder;

import java.util.ArrayList;
import java.util.List;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.ImageEnvVariables;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorEnvironments;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.CommonConfig;
import org.kie.cloud.openshift.operator.model.components.Console;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenarioImpl;
import org.kie.cloud.openshift.template.ProjectProfile;

public class ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl implements ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder {

    private KieApp kieApp = new KieApp();

    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl() {
        isScenarioAllowed();

        List<Env> authenticationEnvVars = new ArrayList<>();
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_SERVER_USER, DeploymentConstants.getKieServerUser()));
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()));

        kieApp.getMetadata().setName(OpenShiftConstants.getKieApplicationName());
        kieApp.getSpec().setEnvironment(OpenShiftOperatorEnvironments.AUTHORING_HA);

        CommonConfig commonConfig = new CommonConfig();
        commonConfig.setAdminPassword(DeploymentConstants.getWorkbenchPassword());
        commonConfig.setServerPassword(DeploymentConstants.getKieServerPassword());
        kieApp.getSpec().setCommonConfig(commonConfig);

        Server server = new Server();
        server.addEnvs(authenticationEnvVars);
        // TODO: Workaround until Maven repo with released artifacts is implemented
        server.addEnv(new Env(ImageEnvVariables.KIE_SERVER_MODE, "DEVELOPMENT"));
        server.setReplicas(1);
        kieApp.getSpec().getObjects().addServer(server);

        Console console = new Console();
        console.addEnvs(authenticationEnvVars);
        kieApp.getSpec().getObjects().setConsole(console);
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenario build() {
        return new ClusteredWorkbenchKieServerDatabasePersistentScenarioImpl(kieApp);
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_URL, repoUrl));
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_USERNAME, repoUserName));
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_PASSWORD, repoPassword));
        }
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withGitHooksDir(String dir) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.GIT_HOOKS_DIR, dir));
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder deploySso() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTP, hostname));
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTPS, hostname));
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpKieServerHostname(String hostname) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTP, hostname));
        }
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpsKieServerHostname(String hostname) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTPS, hostname));
        }
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static void isScenarioAllowed() {
        ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
        switch (projectProfile) {
            case JBPM:
                return;
            case DROOLS:
                throw new UnsupportedOperationException("Not supported");
            default:
                throw new IllegalStateException("Unrecognized ProjectProfile: " + projectProfile);
        }
    }
}
