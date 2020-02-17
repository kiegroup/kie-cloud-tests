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
import org.kie.cloud.api.scenario.DeploymentScenarioListener;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.ImageEnvVariables;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorConstants;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorEnvironments;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Auth;
import org.kie.cloud.openshift.operator.model.components.Build;
import org.kie.cloud.openshift.operator.model.components.CommonConfig;
import org.kie.cloud.openshift.operator.model.components.Console;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.GitSource;
import org.kie.cloud.openshift.operator.model.components.ImageRegistry;
import org.kie.cloud.openshift.operator.model.components.Jms;
import org.kie.cloud.openshift.operator.model.components.Ldap;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.SmartRouter;
import org.kie.cloud.openshift.operator.model.components.SsoClient;
import org.kie.cloud.openshift.operator.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioImpl;
import org.kie.cloud.openshift.operator.settings.LdapSettingsMapper;
import org.kie.cloud.openshift.template.ProjectProfile;

public class WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderOperator<WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario>
                                                                                             implements WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder {

    private KieApp kieApp = new KieApp();
    private boolean deploySSO = false;

    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilderImpl() {
        isScenarioAllowed();

        List<Env> authenticationEnvVars = new ArrayList<>();
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_ADMIN_USER, DeploymentConstants.getAppUser()));
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_ADMIN_PWD, DeploymentConstants.getAppPassword()));

        kieApp.getMetadata().setName(OpenShiftConstants.getKieApplicationName());
        kieApp.getSpec().setEnvironment(OpenShiftOperatorEnvironments.PRODUCTION_IMMUTABLE);
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

        // These values are defined in pom.xml where keystore and truststore are generated
        Jms jms = new Jms();
        jms.setEnableIntegration(true);
        jms.setAmqSecretName("amq-app-secret");
        jms.setAmqKeystoreName("broker.ks");
        jms.setAmqKeystorePassword("changeit");
        jms.setAmqTruststoreName("broker.ts");
        jms.setAmqTruststorePassword("changeit");
        jms.setUsername(DeploymentConstants.getAmqUsername());
        jms.setPassword(DeploymentConstants.getAmqPassword());

        Server server = new Server();
        server.addEnvs(authenticationEnvVars);
        server.setJms(jms);
        kieApp.getSpec().getObjects().addServer(server);

        Console console = new Console();
        console.addEnvs(authenticationEnvVars);
        console.setReplicas(1);
        kieApp.getSpec().getObjects().setConsole(console);

        // Instantiate Smart router as it needs to be configured for custom secret
        kieApp.getSpec().getObjects().setSmartRouter(new SmartRouter());
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario getDeploymentScenarioInstance() {
        return new WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioImpl(kieApp, deploySSO);
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder deploySso() {
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
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withKieServerId(String kieServerId) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.KIE_SERVER_ID, kieServerId));
        }
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withHttpKieServerHostname(String hostname) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTP, hostname));
        }
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withHttpsKieServerHostname(String hostname) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTPS, hostname));
        }
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        Ldap ldap = LdapSettingsMapper.toLdapModel(ldapSettings);
        Auth auth = new Auth();
        auth.setLdap(ldap);
        kieApp.getSpec().setAuth(auth);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withContainerDeployment(String kieContainerDeployment) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            if (server.getBuild() == null) {
                server.setBuild(new Build());
            }
            server.getBuild().setKieServerContainerDeployment(kieContainerDeployment);
        }

        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir) {
        GitSource gitSource = new GitSource();
        gitSource.setContextDir(gitContextDir);
        gitSource.setReference(gitReference);
        gitSource.setUri(gitRepoUrl);

        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            if (server.getBuild() == null) {
                server.setBuild(new Build());
            }
            server.getBuild().setGitSource(gitSource);
        }

        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir, String artifactDirs) {
        withSourceLocation(gitRepoUrl, gitReference, gitContextDir);

        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.getBuild().setArtifactDir(artifactDirs);
        }

        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.DROOLS_SERVER_FILTER_CLASSES, Boolean.toString(droolsFilter)));
        }

        return this;
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

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withDeploymentScenarioListener(DeploymentScenarioListener<WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario> deploymentScenarioListener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder enableExternalJmsSignalQueue(String queueJndiName) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.getJms().setEnableSignal(true);
            server.getJms().setQueueSignal(queueJndiName);
        }
        return this;
    }
}
