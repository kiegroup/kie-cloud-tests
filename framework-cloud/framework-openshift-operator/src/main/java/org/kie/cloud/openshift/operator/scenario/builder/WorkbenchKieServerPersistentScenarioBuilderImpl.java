/*
 * Copyright 2019 JBoss by Red Hat.
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
package org.kie.cloud.openshift.operator.scenario.builder;

import java.util.ArrayList;
import java.util.List;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerPersistentScenarioBuilder;
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
import org.kie.cloud.openshift.operator.scenario.WorkbenchKieServerPersistentScenarioImpl;
import org.kie.cloud.openshift.operator.settings.LdapSettingsMapper;

public class WorkbenchKieServerPersistentScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderOperator<WorkbenchKieServerPersistentScenario> implements WorkbenchKieServerPersistentScenarioBuilder {

    private KieApp kieApp = new KieApp();
    private boolean deploySSO = false;

    public WorkbenchKieServerPersistentScenarioBuilderImpl() {
        List<Env> authenticationEnvVars = new ArrayList<>();
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_ADMIN_USER, DeploymentConstants.getAppUser()));
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_ADMIN_PWD, DeploymentConstants.getAppPassword()));

        kieApp.getMetadata().setName(OpenShiftConstants.getKieApplicationName());
        kieApp.getSpec().setEnvironment(OpenShiftOperatorEnvironments.AUTHORING);
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
        kieApp.getSpec().setCommonConfig(commonConfig);

        Server server = new Server();
        server.addEnvs(authenticationEnvVars);
        kieApp.getSpec().getObjects().addServer(server);

        Console console = new Console();
        console.addEnvs(authenticationEnvVars);
        kieApp.getSpec().getObjects().setConsole(console);
    }

    @Override
    public WorkbenchKieServerPersistentScenario getDeploymentScenarioInstance() {
        return new WorkbenchKieServerPersistentScenarioImpl(kieApp, deploySSO);
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder deploySso() {
        deploySSO = true;
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
    public WorkbenchKieServerPersistentScenarioBuilder withKieServerId(String kieServerId) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.KIE_SERVER_ID, kieServerId));
        }
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTP, hostname));
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTPS, hostname));
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpKieServerHostname(String hostname) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTP, hostname));
        }
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpsKieServerHostname(String hostname) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTPS, hostname));
        }
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        Ldap ldap = LdapSettingsMapper.toLdapModel(ldapSettings);
        Auth auth = new Auth();
        auth.setLdap(ldap);
        kieApp.getSpec().setAuth(auth);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withGitHooksDir(String dir) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.GIT_HOOKS_DIR, dir));
        return this;
    }
}
