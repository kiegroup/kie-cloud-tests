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
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.UpgradeSettings;
import org.kie.cloud.openshift.constants.ImageEnvVariables;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorConstants;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorEnvironments;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.CommonConfig;
import org.kie.cloud.openshift.operator.model.components.Console;
import org.kie.cloud.openshift.operator.model.components.DataGridAuth;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.ImageRegistry;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.SsoClient;
import org.kie.cloud.openshift.operator.model.components.Upgrades;
import org.kie.cloud.openshift.operator.scenario.ClusteredWorkbenchKieServerPersistentScenarioImpl;
import org.kie.cloud.openshift.scenario.ScenarioRequest;

import static org.kie.cloud.openshift.util.ScenarioValidations.verifyDroolsScenarioOnly;

public class ClusteredWorkbenchKieServerPersistentScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderOperator<ClusteredWorkbenchKieServerPersistentScenario> implements
                                                                      ClusteredWorkbenchKieServerPersistentScenarioBuilder {

    private KieApp kieApp = new KieApp();
    private final ScenarioRequest request = new ScenarioRequest();

    public ClusteredWorkbenchKieServerPersistentScenarioBuilderImpl() {
        verifyDroolsScenarioOnly();

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
        DataGridAuth dataGridAuth = new DataGridAuth();
        dataGridAuth.setUsername("datagridUser");
        dataGridAuth.setPassword("datagridPassword");
        console.setDataGridAuth(dataGridAuth);
        kieApp.getSpec().getObjects().setConsole(console);
    }

    @Override
    public ClusteredWorkbenchKieServerPersistentScenario getDeploymentScenarioInstance() {
        return new ClusteredWorkbenchKieServerPersistentScenarioImpl(kieApp, request);
    }

    @Override
    public ClusteredWorkbenchKieServerPersistentScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerPersistentScenarioBuilder withGitHooksDir(String dir) {
        kieApp.getSpec().getObjects().getConsole().addEnv(new Env(ImageEnvVariables.GIT_HOOKS_DIR, dir));
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerPersistentScenarioBuilder deploySso() {
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
    public ClusteredWorkbenchKieServerPersistentScenarioBuilder withGitSettings(GitSettings gitSettings) {
        request.setGitSettings(gitSettings);
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerPersistentScenarioBuilder withWorkbenchMemoryLimit(String limit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ClusteredWorkbenchKieServerPersistentScenarioBuilder withUpgrades(UpgradeSettings upgradeSettings) {
        if (upgradeSettings != null) {
            Upgrades upgrades = new Upgrades();
            upgrades.setEnabled(true);
            upgrades.setMinor(upgradeSettings.isMinor());
            kieApp.getSpec().setUpgrades(upgrades);
            request.enableUpgrade();
        }

        return this;
    }
}
