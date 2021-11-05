/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.DeploymentScenarioListener;
import org.kie.cloud.api.scenario.KieServerScenario;
import org.kie.cloud.api.scenario.builder.KieServerScenarioBuilder;
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
import org.kie.cloud.openshift.operator.model.components.Database;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.ImageRegistry;
import org.kie.cloud.openshift.operator.model.components.Ldap;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.SsoClient;
import org.kie.cloud.openshift.operator.scenario.KieServerScenarioImpl;
import org.kie.cloud.openshift.operator.settings.LdapSettingsMapper;
import org.kie.cloud.openshift.scenario.ScenarioRequest;

public class KieServerScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderOperator<KieServerScenario> implements KieServerScenarioBuilder {

    private KieApp kieApp = new KieApp();
    private ScenarioRequest request = new ScenarioRequest();

    public KieServerScenarioBuilderImpl() {
        kieApp.getMetadata().setName(OpenShiftConstants.getKieApplicationName());
        kieApp.getSpec().setEnvironment(OpenShiftOperatorEnvironments.TRIAL);
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

        Database database = new Database();
        database.setType("h2");

        server.setDatabase(database);

        kieApp.getSpec().getObjects().addServer(server);

        Console console = new Console();
        kieApp.getSpec().getObjects().setConsole(console);
    }

    @Override
    public KieServerScenario getDeploymentScenarioInstance() {
        return new KieServerScenarioImpl(kieApp, request);
    }

    @Override
    public KieServerScenarioBuilder deploySso() {
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
    public KieServerScenarioBuilder withKieServerId(String kieServerId) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.KIE_SERVER_ID, kieServerId));
        }
        return this;
    }

    @Override
    public KieServerScenarioBuilder withHttpKieServerHostname(String hostname) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTP, hostname));
        }
        return this;
    }

    @Override
    public KieServerScenarioBuilder withHttpsKieServerHostname(String hostname) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTPS, hostname));
        }
        return this;
    }

    @Override
    public KieServerScenarioBuilder withLdap(LdapSettings ldapSettings) {
        setAsyncExternalDeployment(ExternalDeploymentID.LDAP);
        Ldap ldap = LdapSettingsMapper.toLdapModel(ldapSettings);
        Auth auth = new Auth();
        auth.setLdap(ldap);
        kieApp.getSpec().setAuth(auth);
        return this;
    }

    @Override
    public KieServerScenarioBuilder withDeploymentScenarioListener(DeploymentScenarioListener<KieServerScenario> deploymentScenarioListener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KieServerScenarioBuilder withInternalMavenRepo(boolean waitForRunning) {
        if(waitForRunning) {
            setSyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        } else {
            setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        }
        return this;
    }

    @Override
    public KieServerScenarioBuilder withContainerDeployment(String kieContainerDeployment) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment));
        }
        return this;
    }

}