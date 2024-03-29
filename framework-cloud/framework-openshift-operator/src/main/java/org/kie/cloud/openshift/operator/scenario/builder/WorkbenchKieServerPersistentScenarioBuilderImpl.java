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

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerPersistentScenarioBuilder;
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
import org.kie.cloud.openshift.operator.model.components.RoleMapper;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.SsoClient;
import org.kie.cloud.openshift.operator.scenario.WorkbenchKieServerPersistentScenarioImpl;
import org.kie.cloud.openshift.operator.settings.LdapSettingsMapper;
import org.kie.cloud.openshift.scenario.ScenarioRequest;

public class WorkbenchKieServerPersistentScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderOperator<WorkbenchKieServerPersistentScenario> implements WorkbenchKieServerPersistentScenarioBuilder {

    private KieApp kieApp = new KieApp();
    private final ScenarioRequest request = new ScenarioRequest();

    public WorkbenchKieServerPersistentScenarioBuilderImpl() {
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
        kieApp.getSpec().getObjects().addServer(server);

        Console console = new Console();
        kieApp.getSpec().getObjects().setConsole(console);
    }

    @Override
    public WorkbenchKieServerPersistentScenario getDeploymentScenarioInstance() {
        return new WorkbenchKieServerPersistentScenarioImpl(kieApp, request);
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder deploySso() {
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
    public WorkbenchKieServerPersistentScenarioBuilder withGitSettings(GitSettings gitSettings) {
        request.setGitSettings(gitSettings);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withKieServerId(String kieServerId) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.KIE_SERVER_ID, kieServerId));
        }
        return this;
    }

    private boolean workbenchHostnameSet = false;

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        checkHttpWorkbenchRouteConfig(workbenchHostnameSet, kieApp);
        
        kieApp.getSpec().getObjects().getConsole().setRouteHostname(hostname);

        workbenchHostnameSet = true;
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        checkHttpsWorkbenchRouteConfig(workbenchHostnameSet, kieApp);
        
        kieApp.getSpec().getObjects().getConsole().setRouteHostname(hostname);

        workbenchHostnameSet = true;
        return this;
    }

    private boolean kieServerHostnameSet = false;

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpKieServerHostname(String hostname) {
        checkHttpKieServerRouteConfig(kieServerHostnameSet, kieApp);

        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.setRouteHostname(hostname);
        }

        kieServerHostnameSet = true;
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpsKieServerHostname(String hostname) {
        checkHttpsKieServerRouteConfig(kieServerHostnameSet, kieApp);

        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.setRouteHostname(hostname);
        }

        kieServerHostnameSet = true;
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withLdap(LdapSettings ldapSettings) {
        setAsyncExternalDeployment(ExternalDeploymentID.LDAP);
        Ldap ldap = LdapSettingsMapper.toLdapModel(ldapSettings);

        if (kieApp.getSpec().getAuth() != null) {
            kieApp.getSpec().getAuth().setLdap(ldap);
        } else {
            Auth auth = new Auth();
            auth.setLdap(ldap);
            kieApp.getSpec().setAuth(auth);
        }
        
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withRoleMapper(String rolesProperties, Boolean rolesKeepMapped,  Boolean rolesKeepNonMapped) {
        RoleMapper roleMapper = new RoleMapper();
        roleMapper.setRolesProperties(rolesProperties);
        roleMapper.setRolesKeepMapped(rolesKeepMapped);
        roleMapper.setRolesKeepNonMapped(rolesKeepNonMapped);
        if (kieApp.getSpec().getAuth() != null) {
            kieApp.getSpec().getAuth().setRoleMapper(roleMapper);
        } else {
            Auth auth = new Auth();
            auth.setRoleMapper(roleMapper);
            kieApp.getSpec().setAuth(auth);
        }

        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withGitHooksDir(String dir) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.GIT_HOOKS_DIR, dir));
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder usePublicIpAddress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override 
    public WorkbenchKieServerPersistentScenarioBuilder withReposPersistence() {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.setPersistRepos(true);
            server.setServersKiePvSize("1Gi");
            server.setServersM2PvSize("1Gi");
        }
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withSecretAdminCredentials() {
        request.enableDeploySecretAdminCredentials();
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withEnabledEdgeTermination() {
        request.enableEdgeTermination();
        return this;
    }
}
