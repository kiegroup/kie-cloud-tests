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
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.ImageEnvVariables;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorConstants;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorEnvironments;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Auth;
import org.kie.cloud.openshift.operator.model.components.CommonConfig;
import org.kie.cloud.openshift.operator.model.components.Console;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.ImageRegistry;
import org.kie.cloud.openshift.operator.model.components.Ldap;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.SsoClient;
import org.kie.cloud.openshift.operator.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenarioImpl;
import org.kie.cloud.openshift.operator.settings.LdapSettingsMapper;
import org.kie.cloud.openshift.scenario.ScenarioRequest;

import static org.kie.cloud.openshift.util.ScenarioValidations.verifyJbpmScenarioOnly;

public class ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderOperator<ClusteredWorkbenchKieServerDatabasePersistentScenario> implements
                                                                              ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder {

    private KieApp kieApp = new KieApp();
    private final ScenarioRequest request = new ScenarioRequest();

    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl() {
        verifyJbpmScenarioOnly();

        List<Env> authenticationEnvVars = new ArrayList<>();
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_ADMIN_USER, DeploymentConstants.getAppUser()));
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_ADMIN_PWD, DeploymentConstants.getAppPassword()));

        kieApp.getMetadata().setName(OpenShiftConstants.getKieApplicationName());
        kieApp.getSpec().setEnvironment(OpenShiftOperatorEnvironments.AUTHORING_HA);
        kieApp.getSpec().setUseImageTags(true);

        OpenShiftOperatorConstants.getKieImageRegistryCustom().ifPresent(registry -> {
            ImageRegistry imageRegistry = new ImageRegistry();
            imageRegistry.setInsecure(true);
            imageRegistry.setRegistry(registry);
            kieApp.getSpec().setImageRegistry(imageRegistry);
        });

        CommonConfig commonConfig = new CommonConfig();
        commonConfig.setAdminUser(DeploymentConstants.getAppUser());
        commonConfig.setAdminPassword(DeploymentConstants.getAppPassword());
        commonConfig.setAmqClusterPassword("amqClusterPassword");
        commonConfig.setAmqPassword("amqPassword");

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
    public ClusteredWorkbenchKieServerDatabasePersistentScenario getDeploymentScenarioInstance() {
        return new ClusteredWorkbenchKieServerDatabasePersistentScenarioImpl(kieApp, request);
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withGitHooksDir(String dir) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.GIT_HOOKS_DIR, dir));
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder deploySso() {
        request.enableDeploySso();
        SsoClient ssoClient = new SsoClient();
        ssoClient.setName("workbench-client");
        ssoClient.setSecret("workbench-secret");
        kieApp.getSpec().getObjects().getConsole().setSsoClient(ssoClient);

        Server[] servers = kieApp.getSpec().getObjects().getServers();
        for (int i = 0; i < servers.length; i++) {
            ssoClient = new SsoClient();
            ssoClient.setName("kie-server-" + i + "-client");
            ssoClient.setSecret("kie-server-" + i + "-secret");
            servers[i].setSsoClient(ssoClient);
        }
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withGitSettings(GitSettings gitSettings) {
        request.setGitSettings(gitSettings);
        return this;
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
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withLdap(LdapSettings ldapSettings) {
        setAsyncExternalDeployment(ExternalDeploymentID.LDAP);
        Ldap ldap = LdapSettingsMapper.toLdapModel(ldapSettings);
        Auth auth = new Auth();
        auth.setLdap(ldap);
        kieApp.getSpec().setAuth(auth);
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withWorkbenchMemoryLimit(String limit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
