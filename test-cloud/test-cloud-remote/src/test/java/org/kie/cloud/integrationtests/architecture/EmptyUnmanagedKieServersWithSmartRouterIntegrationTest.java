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
package org.kie.cloud.integrationtests.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.SmartRouterAdminClientProvider;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.cloud.integrationtests.util.SmartRouterUtils;
import org.kie.cloud.integrationtests.util.TimeUtils;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.kie.server.router.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for Architecture 5 from
 * http://mswiderski.blogspot.cz/2017/08/cloud-runtime-architectures-for-jbpm.html
 */
@Category(JBPMOnly.class)
public class EmptyUnmanagedKieServersWithSmartRouterIntegrationTest extends AbstractCloudArchitectureIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(EmptyUnmanagedKieServersWithSmartRouterIntegrationTest.class);

    private final String RANDOM_URL_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";

    private static final String SMART_ROUTER_HOSTNAME = SMART_ROUTER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    private DeploymentSettings smartRouter;

    private DeploymentSettings
            kieServerABC,
            kieServerDEF,
            kieServerGHI;

    private KieServerRouterClient smartRouterAdminClient;
    private KieServicesClient smartRouterClient;

    private KieServicesClient kieServerClientABC;
    private KieServicesClient kieServerClientDEF;
    private KieServicesClient kieServerClientGHI;

    private ProcessServicesClient smartProcessServicesClient;
    private UserTaskServicesClient smartTaskServicesClient;
    private QueryServicesClient smartQueryServicesClient;

    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        smartRouter = deploymentScenarioFactory.getSmartRouterSettingsBuilder()
                .withApplicationName(SMART_ROUTER_NAME)
                .withSmartRouterID(SMART_ROUTER_ID)
                .withHostame(RANDOM_URL_PREFIX + SMART_ROUTER_HOSTNAME)
                .withSmartRouterExternalUrl("http://" + RANDOM_URL_PREFIX + SMART_ROUTER_HOSTNAME + ":" + PORT)
                .build();

        kieServerABC = configureKieServer(deploymentScenarioFactory, KIE_SERVER_ABC_NAME);
        kieServerDEF = configureKieServer(deploymentScenarioFactory, KIE_SERVER_DEF_NAME);
        kieServerGHI = configureKieServer(deploymentScenarioFactory, KIE_SERVER_GHI_NAME);

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withSmartRouter(smartRouter)
                .withKieServer(kieServerABC, kieServerDEF, kieServerGHI)
                .build();
    }

    private DeploymentSettings configureKieServer(DeploymentScenarioBuilderFactory deploymentScenarioFactory, String applicationName) {
        return deploymentScenarioFactory.getKieServerSettingsBuilder()
                .withApplicationName(applicationName)
                .withHostame(RANDOM_URL_PREFIX + applicationName + DeploymentConstants.getDefaultDomainSuffix())
                .withControllerUser(DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword())
                .withSmartRouterConnection(RANDOM_URL_PREFIX + SMART_ROUTER_HOSTNAME, PORT)
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
    }

    @Before
    public void setUp() {
        smartRouterAdminClient = SmartRouterAdminClientProvider.getSmartRouterClient(deploymentScenario.getSmartRouterDeployments().get(0));

        kieServerClientABC = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(0));
        kieServerClientDEF = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(1));
        kieServerClientGHI = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(2));
        smartRouterClient = KieServerClientProvider.getSmartRouterClient(deploymentScenario.getSmartRouterDeployments().get(0), DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword());

        smartProcessServicesClient = smartRouterClient.getServicesClient(ProcessServicesClient.class);
        smartTaskServicesClient = smartRouterClient.getServicesClient(UserTaskServicesClient.class);
        smartQueryServicesClient = smartRouterClient.getServicesClient(QueryServicesClient.class);
    }

    @Test
    public void testEmptyUnmanagedKieServersWithSmartRouterArchitecture() {
        connectionBetweenDeployments();
        scaleKieServerTo(deploymentScenario.getKieServerDeployments(), 3);

        // Ignore due https://issues.jboss.org/browse/RHBPMS-4899 and https://issues.jboss.org/browse/JBPM-6623
        //workWithSignalsAndTasks();
    }

    private void connectionBetweenDeployments() {
        logger.debug("Check architecture after start");
        assertClientHasConnectedKieServerCount(kieServerClientABC, 1);
        assertClientHasConnectedKieServerCount(kieServerClientDEF, 1);
        assertClientHasConnectedKieServerCount(kieServerClientGHI, 1);
        assertSmartRouterIsEmpty(smartRouterAdminClient);

        logger.debug("Scale up all Kie server deployments");
        scaleKieServerTo(deploymentScenario.getKieServerDeployments(), 3);
        assertClientHasConnectedKieServerCount(kieServerClientABC, 3);
        assertClientHasConnectedKieServerCount(kieServerClientDEF, 3);
        assertClientHasConnectedKieServerCount(kieServerClientGHI, 3);
        assertSmartRouterIsEmpty(smartRouterAdminClient);

        logger.debug("scale some server down");
        scaleKieServerTo(0, deploymentScenario.getKieServerDeployments().get(1));

        logger.debug("check all again");
        assertClientHasConnectedKieServerCount(kieServerClientABC, 3);
        assertClientHasNotConnectedKieServer(kieServerClientDEF);
        assertClientHasConnectedKieServerCount(kieServerClientGHI, 3);
        assertSmartRouterIsEmpty(smartRouterAdminClient);
    }

    private void workWithSignalsAndTasks() {
        logger.debug("Deploy and start containers on Kie servers");
        deployAndStartContainer(kieServerClientABC, CONTAINER_ID_ABC, deploymentScenario.getKieServerDeployments().get(0));
        deployAndStartContainer(kieServerClientDEF, CONTAINER_ID_DEF, deploymentScenario.getKieServerDeployments().get(1));
        deployAndStartContainer(kieServerClientGHI, CONTAINER_ID_GHI, deploymentScenario.getKieServerDeployments().get(2));

        waitUntilKieServerLogsContain(deploymentScenario.getKieServerDeployments().get(0), "Container cont-id-abc (for release id org.kie.server.testing:definition-project-snapshot:1.0.0-SNAPSHOT) successfully started");
        waitUntilKieServerLogsContain(deploymentScenario.getKieServerDeployments().get(1), "Container cont-id-def (for release id org.kie.server.testing:definition-project-snapshot:1.0.0-SNAPSHOT) successfully started");
        waitUntilKieServerLogsContain(deploymentScenario.getKieServerDeployments().get(2), "Container cont-id-ghi (for release id org.kie.server.testing:definition-project-snapshot:1.0.0-SNAPSHOT) successfully started");

        logger.debug("Start process instance");
        List<Long> signalProcessInstances = createSignalProcesses();

        assertThat(smartQueryServicesClient.findProcessInstances(0, 10)).hasSize(6);

        smartProcessServicesClient.signalProcessInstances(CONTAINER_ID_ABC, signalProcessInstances, Constants.Signal.SIGNAL_NAME, null);
        smartProcessServicesClient.signalProcessInstance(CONTAINER_ID_DEF, signalProcessInstances.get(2), Constants.Signal.SIGNAL_NAME, null);
        smartProcessServicesClient.signalProcessInstance(CONTAINER_ID_DEF, signalProcessInstances.get(3), Constants.Signal.SIGNAL_2_NAME, null);

        assertThat(smartTaskServicesClient.findTasks(Constants.User.YODA, 0, 10)).hasSize(3);

        smartProcessServicesClient.signal(CONTAINER_ID_ABC, Constants.Signal.SIGNAL_2_NAME, null);
        assertThat(smartQueryServicesClient.findProcessInstancesByStatus(Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED), 0, 10)).hasSize(2);

        assertThat(smartQueryServicesClient.findProcessInstancesByStatus(Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE), 0, 10)).hasSize(4);
        List<TaskSummary> activeTasks = smartTaskServicesClient.findTasks(Constants.User.YODA, 0, 10);
        assertThat(activeTasks).hasSize(1);
        assertThat(activeTasks.get(0).getContainerId()).isEqualTo(CONTAINER_ID_DEF);
        assertThat(activeTasks.get(0).getProcessInstanceId()).isEqualTo(signalProcessInstances.get(2));

        smartTaskServicesClient.startTask(CONTAINER_ID_DEF, activeTasks.get(0).getId(), Constants.User.YODA);
        smartTaskServicesClient.completeTask(CONTAINER_ID_DEF, activeTasks.get(0).getId(), Constants.User.YODA, Collections.emptyMap());

        assertThat(smartQueryServicesClient.findProcessesByContainerId(CONTAINER_ID_GHI, 0, 10)).hasSize(2);
        smartProcessServicesClient.abortProcessInstance(CONTAINER_ID_GHI, signalProcessInstances.get(5));

        assertThat(smartQueryServicesClient.findProcessInstancesByStatus(Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED), 0, 10)).hasSize(4);
        assertThat(smartQueryServicesClient.findProcessInstancesByStatus(Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE), 0, 10)).hasSize(2);
        assertThat(smartTaskServicesClient.findTasks(Constants.User.YODA, 0, 10)).hasSize(0);
    }

    private List<Long> createSignalProcesses() {
        // Start 2 instance of SignalUserTask each process on each container
        return Stream.of(CONTAINER_ID_ABC, CONTAINER_ID_ABC, CONTAINER_ID_DEF, CONTAINER_ID_DEF, CONTAINER_ID_GHI, CONTAINER_ID_GHI)
                .map(containerId -> smartProcessServicesClient.startProcess(containerId, Constants.ProcessId.SIGNALUSERTASK))
                .collect(Collectors.toList());
    }

    private void waitUntilKieServerLogsContain(KieServerDeployment kieServerDeployment, String logMessage) {
        for (Instance kieServerInstance : kieServerDeployment.getInstances()) {
            TimeUtils.wait(Duration.ofSeconds(15), Duration.ofSeconds(1), () -> kieServerInstance.getLogs().contains(logMessage));
        }
    }

    private void deployAndStartContainer(KieServicesClient kieServerClient, String containerId, KieServerDeployment kieServerDeployment) {
        KieContainerResource ksr = new KieContainerResource(containerId, new ReleaseId(PROJECT_GROUP_ID, DEFINITION_PROJECT_SNAPSHOT_NAME, DEFINITION_PROJECT_SNAPSHOT_VERSION), KieContainerStatus.STARTED);
        kieServerClient.createContainer(containerId, ksr);
        // Wait until container is started in Kie server
        KieServerClientProvider.waitForContainerStart(kieServerDeployment, containerId);
        // Wait until container is registered in Smart router
        SmartRouterUtils.waitForContainerStart(smartRouterAdminClient, containerId);
        // Wait until container is registered in Workbench under Smart router server template
        // (containers are registered asynchronously in a batch).
    }

    private void assertSmartRouterIsEmpty(KieServerRouterClient client) {
        Configuration smartRouterConfig = client.getRouterConfig();
        assertThat(smartRouterConfig.getHostsPerContainer()).isEmpty();
        assertThat(smartRouterConfig.getHostsPerServer()).isEmpty();
        assertThat(smartRouterConfig.getContainerInfosPerContainer()).isEmpty();
    }
}
