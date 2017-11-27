/*
 * Copyright 2017 JBoss by Red Hat.
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
package org.kie.cloud.integrationtests.scaling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.protocol.Protocol;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.time.TimeUtils;
import org.kie.cloud.common.client.util.WorkbenchUtils;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.management.client.KieServerMgmtControllerClient;

public class KieServerWebSocketScalingIntegrationTest {

    private GenericScenario workbenchMonitoringScenario;
    private GenericScenario kieServerScenario;

    private WorkbenchDeployment workbenchDeployment;
    private KieServerDeployment kieServerDeployment;

    private KieServerMgmtControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;

    private static final String CONTAINER_ID = "cont-id";
    private static final String CONTAINER_ALIAS = "cont-alias";

    private static final String PROJECT_GROUP_ID = "org.kie.server.testing";
    private static final String DEFINITION_PROJECT_SNAPSHOT_NAME = "definition-project-snapshot";
    private static final String DEFINITION_PROJECT_SNAPSHOT_VERSION = "1.0.0-SNAPSHOT";

    private static final String WEBSOCKET_CONNECTION = "Connection to Kie Controller over websocket is now open";
    private static final String STARTED_CONTAINER = "Container cont-id (for release id org.kie.server.testing:definition-project-snapshot:1.0.0-SNAPSHOT) successfully started";

    @Before
    public void setUp() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());

        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        DeploymentSettings workbenchMonitoringSettings = deploymentScenarioFactory.getWorkbenchMonitoringSettingsBuilder()
                .withControllerUser(DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword())
                .build();
        workbenchMonitoringScenario = deploymentScenarioFactory.getGenericScenarioBuilder()
                .withMonitoring(workbenchMonitoringSettings)
                .build();
        workbenchMonitoringScenario.deploy();
        workbenchDeployment = workbenchMonitoringScenario.getWorkbenchDeployments().get(0);

        DeploymentSettings kieServerSettings = deploymentScenarioFactory.getKieServerS2ISettingsBuilder()
                .withControllerUser(DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword())
                .withControllerProtocol(Protocol.ws)
                .withControllerConnection(workbenchDeployment.getWebSocketUri().getHost(), String.valueOf(workbenchDeployment.getWebSocketUri().getPort()))
                .withMavenRepoUrl(MavenConstants.getMavenRepoUrl())
                .withMavenRepoUser(MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withKieServerSyncDeploy(true)
                .build();
        kieServerScenario = deploymentScenarioFactory.getGenericScenarioBuilder()
                .withKieServer(kieServerSettings)
                .build();
        kieServerScenario.deploy();
        kieServerDeployment = kieServerScenario.getKieServerDeployments().get(0);

        kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);
        kieControllerClient = KieServerControllerClientProvider.getKieServerMgmtControllerClient(workbenchDeployment);
    }

    @After
    public void cleanEnvironment() {
        if (workbenchMonitoringScenario != null) {
            workbenchMonitoringScenario.undeploy();
        }
        if (kieServerScenario != null) {
            kieServerScenario.undeploy();
        }
    }

    @Test
    public void testConnectionBetweenDeployables() {
        scaleKieServerTo(3);

        String kieServerId = getKieServerId(kieServerClient);
        waitUntilKieServerLogsContain(WEBSOCKET_CONNECTION);
        verifyKieServerLogsContain(WEBSOCKET_CONNECTION);
        verifyServerTemplateContainsKieServers(kieServerId, 3);

        deployAndStartContainer();

        waitUntilKieServerLogsContain(STARTED_CONTAINER);
        verifyKieServerLogsContain(STARTED_CONTAINER);
        verifyServerTemplateContainsContainer(kieServerId, CONTAINER_ID);

        scaleKieServerTo(1);

        verifyServerTemplateContainsKieServers(kieServerId, 1);
    }

    private String getKieServerId(KieServicesClient kieServerClient) {
        ServiceResponse<KieServerInfo> serverInfo = kieServerClient.getServerInfo();
        assertThat(serverInfo.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        return serverInfo.getResult().getServerId();
    }

    private void verifyKieServerLogsContain(String logMessage) {
        for (Instance kieServerInstance : kieServerDeployment.getInstances()) {
            assertThat(kieServerInstance.getLogs()).contains(logMessage);
        }
    }

    private void waitUntilKieServerLogsContain(String logMessage) {
        for (Instance kieServerInstance : kieServerDeployment.getInstances()) {
            TimeUtils.wait(Duration.ofSeconds(15), Duration.ofSeconds(1), () -> kieServerInstance.getLogs().contains(logMessage));
        }
    }

    private void verifyServerTemplateContainsContainer(String serverTemplate, String containerId) {
        Collection<ContainerSpec> containersSpec = kieControllerClient.getServerTemplate(serverTemplate).getContainersSpec();
        assertThat(containersSpec).hasSize(1);
        assertThat(containersSpec.iterator().next().getId()).isEqualTo(containerId);
    }

    private void verifyServerTemplateContainsKieServers(String serverTemplate, int numberOfKieServers) {
        Collection<ServerInstanceKey> kieServers = kieControllerClient.getServerTemplate(serverTemplate).getServerInstanceKeys();
        assertThat(kieServers).hasSize(numberOfKieServers);
    }

    private void deployAndStartContainer() {
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, DEFINITION_PROJECT_SNAPSHOT_NAME, DEFINITION_PROJECT_SNAPSHOT_VERSION, KieContainerStatus.STARTED);
    }

    private void scaleKieServerTo(int count) {
        kieServerDeployment.scale(count);
        kieServerDeployment.waitForScale();
    }
}
