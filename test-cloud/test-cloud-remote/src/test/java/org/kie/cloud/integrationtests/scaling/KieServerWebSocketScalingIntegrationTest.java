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
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.protocol.Protocol;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.scenario.MissingResourceException;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.util.TimeUtils;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.client.KieServerControllerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerWebSocketScalingIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(KieServerWebSocketScalingIntegrationTest.class);

    private GenericScenario workbenchMonitoringScenario;
    private GenericScenario kieServerScenario;

    private WorkbenchDeployment workbenchDeployment;
    private KieServerDeployment kieServerDeployment;

    private KieServerControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;

    private static final String CONTAINER_ID = "cont-id";
    private static final String CONTAINER_ALIAS = "cont-alias";

    private static final String WEBSOCKET_CONNECTION = "Connection to Kie Controller over Web Socket is now open";
    private static final String STARTED_CONTAINER = "Container cont-id (for release id org.kie.server.testing:definition-project-snapshot:1.0.0-SNAPSHOT) successfully started";

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void buildKjar() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());
    }

    @Before
    public void setUp() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            DeploymentSettings workbenchMonitoringSettings = deploymentScenarioFactory.getWorkbenchMonitoringSettingsBuilder()
                    .withControllerUser(DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword())
                    .build();
            workbenchMonitoringScenario = deploymentScenarioFactory.getGenericScenarioBuilder()
                    .withMonitoring(workbenchMonitoringSettings)
                    .build();
            workbenchMonitoringScenario.setLogFolderName(testName.getMethodName());
            workbenchMonitoringScenario.deploy();
            workbenchDeployment = workbenchMonitoringScenario.getWorkbenchDeployments().get(0);

            DeploymentSettings kieServerSettings = deploymentScenarioFactory.getKieServerMySqlSettingsBuilder()
                    .withControllerUser(DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword())
                    .withControllerConnection(Protocol.ws.name(), workbenchDeployment.getWebSocketUri().getHost(), String.valueOf(workbenchDeployment.getWebSocketUri().getPort()))
                    .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                    .withKieServerSyncDeploy(true)
                    .build();
            kieServerScenario = deploymentScenarioFactory.getGenericScenarioBuilder()
                    .withKieServer(kieServerSettings)
                    .build();
            kieServerScenario.setLogFolderName(testName.getMethodName());
            kieServerScenario.deploy();
            kieServerDeployment = kieServerScenario.getKieServerDeployments().get(0);
        } catch (MissingResourceException e) {
            logger.warn("Skipping test because of missing resource.", e);
            Assume.assumeNoException(e);
        } catch (UnsupportedOperationException e) {
            logger.warn("Skipping test", e);
            Assume.assumeNoException(e);
        }

        kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);
        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(workbenchDeployment);
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
    @Category(JBPMOnly.class)
    public void testConnectionBetweenDeployables() {
        scaleKieServerTo(3);
        verifyServerDeploymentContainInstances(3);

        String kieServerId = getKieServerId(kieServerClient);
        waitUntilKieServerLogsContain(WEBSOCKET_CONNECTION);
        verifyKieServerLogsContain(WEBSOCKET_CONNECTION);
        // In case of WebSockets all Kie servers with same URL are registered under one server template instance
        verifyServerTemplateContainsKieServers(kieServerId, 1);

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
            TimeUtils.wait(Duration.ofMinutes(1), Duration.ofSeconds(1), () -> kieServerInstance.getLogs().contains(logMessage));
        }
    }

    private void verifyServerTemplateContainsContainer(String serverTemplate, String containerId) {
        Collection<ContainerSpec> containersSpec = kieControllerClient.getServerTemplate(serverTemplate).getContainersSpec();
        assertThat(containersSpec).hasSize(1);
        assertThat(containersSpec.iterator().next().getId()).isEqualTo(containerId);
    }

    private void verifyServerDeploymentContainInstances(int numberOfKieServerInstances) {
        assertThat(kieServerDeployment.getInstances()).hasSize(numberOfKieServerInstances);
    }

    private void verifyServerTemplateContainsKieServers(String serverTemplate, int numberOfKieServers) {
        Collection<ServerInstanceKey> kieServers = kieControllerClient.getServerTemplate(serverTemplate).getServerInstanceKeys();
        assertThat(kieServers).hasSize(numberOfKieServers);
    }

    private void deployAndStartContainer() {
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION_SNAPSHOT, KieContainerStatus.STARTED);
    }

    private void scaleKieServerTo(int count) {
        kieServerDeployment.scale(count);
        kieServerDeployment.waitForScale();
    }
}
