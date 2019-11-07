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
import org.kie.cloud.api.scenario.ImmutableKieServerScenario;
import org.kie.cloud.api.scenario.builder.ImmutableKieServerScenarioBuilder;
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
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.GitSource;
import org.kie.cloud.openshift.operator.model.components.ImageRegistry;
import org.kie.cloud.openshift.operator.model.components.Ldap;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.SsoClient;
import org.kie.cloud.openshift.operator.scenario.ImmutableKieServerScenarioImpl;
import org.kie.cloud.openshift.operator.settings.LdapSettingsMapper;
import org.kie.cloud.openshift.template.ProjectProfile;

public class ImmutableKieServerScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderOperator<ImmutableKieServerScenario> implements ImmutableKieServerScenarioBuilder {

    private KieApp kieApp = new KieApp();
    private boolean deploySSO = false;

    public ImmutableKieServerScenarioBuilderImpl() {
        isScenarioAllowed();

        List<Env> authenticationEnvVars = new ArrayList<>();
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_SERVER_USER, DeploymentConstants.getKieServerUser()));
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_SERVER_CONTROLLER_USER, DeploymentConstants.getControllerUser()));
        authenticationEnvVars.add(new Env(ImageEnvVariables.KIE_MAVEN_USER, DeploymentConstants.getWorkbenchUser()));

        kieApp.getMetadata().setName(OpenShiftConstants.getKieApplicationName());
        kieApp.getSpec().setEnvironment(OpenShiftOperatorEnvironments.PRODUCTION_IMMUTABLE);

        OpenShiftOperatorConstants.getKieImageRegistryCustom().ifPresent(registry -> {
            ImageRegistry imageRegistry = new ImageRegistry();
            imageRegistry.setInsecure(true);
            imageRegistry.setRegistry(registry);
            kieApp.getSpec().setImageRegistry(imageRegistry);
        });

        CommonConfig commonConfig = new CommonConfig();
        commonConfig.setAdminUser(DeploymentConstants.getWorkbenchUser());
        commonConfig.setAdminPassword(DeploymentConstants.getWorkbenchPassword());
        commonConfig.setServerPassword(DeploymentConstants.getKieServerPassword());
        commonConfig.setControllerPassword(DeploymentConstants.getControllerPassword());
        commonConfig.setMavenPassword(DeploymentConstants.getWorkbenchPassword());
        kieApp.getSpec().setCommonConfig(commonConfig);

        Server server = new Server();
        server.addEnvs(authenticationEnvVars);
        kieApp.getSpec().getObjects().addServer(server);
    }

    @Override
    public ImmutableKieServerScenario getDeploymentScenarioInstance() {
        return new ImmutableKieServerScenarioImpl(kieApp, deploySSO);
    }

    @Override
    public ImmutableKieServerScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder deploySso() {
        deploySSO = true;
        Server[] servers = kieApp.getSpec().getObjects().getServers();
        for (int i = 0; i < servers.length; i++) {
            SsoClient ssoClient = new SsoClient();
            ssoClient.setName("kie-server-" + i + "-client");
            ssoClient.setSecret("kie-server-" + i + "-secret");
            servers[i].setSsoClient(ssoClient);
        }
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withKieServerId(String kieServerId) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.KIE_SERVER_ID, kieServerId));
        }
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withHttpKieServerHostname(String hostname) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTP, hostname));
        }
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withHttpsKieServerHostname(String hostname) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.HOSTNAME_HTTPS, hostname));
        }
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        Ldap ldap = LdapSettingsMapper.toLdapModel(ldapSettings);
        Auth auth = new Auth();
        auth.setLdap(ldap);
        kieApp.getSpec().setAuth(auth);
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withDeploymentScenarioListener(DeploymentScenarioListener<ImmutableKieServerScenario> deploymentScenarioListener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImmutableKieServerScenarioBuilder withContainerDeployment(String kieContainerDeployment) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            if (server.getBuild() == null) {
                server.setBuild(new Build());
            }
            server.getBuild().setKieServerContainerDeployment(kieContainerDeployment);
        }

        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir) {
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
    public ImmutableKieServerScenarioBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir, String artifactDirs) {
        withSourceLocation(gitRepoUrl, gitReference, gitContextDir);

        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.getBuild().setArtifactDir(artifactDirs);
        }

        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.DROOLS_SERVER_FILTER_CLASSES, Boolean.toString(droolsFilter)));
        }

        return this;
    }

    private static void isScenarioAllowed() {
        ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
        switch (projectProfile) {
            case JBPM:
                throw new UnsupportedOperationException("Not supported");
            case DROOLS:
                return;
            default:
                throw new IllegalStateException("Unrecognized ProjectProfile: " + projectProfile);
        }
    }
}
