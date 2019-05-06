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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.SmartRouterAdminClientProvider;
import org.kie.cloud.integrationtests.category.ApbNotSupported;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.cloud.tests.common.client.util.SmartRouterUtils;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.cloud.tests.common.time.Constants;
import org.kie.cloud.tests.common.time.TimeUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for Architecture 3 from
 * http://mswiderski.blogspot.cz/2017/08/cloud-runtime-architectures-for-jbpm.html
 */
@Category(ApbNotSupported.class)
public class EmptyManagedKieServersWithSmartRouterAndControllerIntegrationTest extends AbstractCloudArchitectureIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(EmptyManagedKieServersWithSmartRouterAndControllerIntegrationTest.class);

    private final String RANDOM_URL_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";

    private static final String CONTROLLER_HOSTNAME = CONTROLLER_NAME + DeploymentConstants.getDefaultDomainSuffix();
    private static final String SMART_ROUTER_HOSTNAME = SMART_ROUTER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    private DeploymentSettings controller;
    private DeploymentSettings smartRouter;

    private DeploymentSettings
            kieServerABC,
            kieServerDEF,
            kieServerGHI;

    private KieServerControllerClient kieControllerClient;
    private KieServerRouterClient smartRouterAdminClient;
    private KieServicesClient smartRouterClient;

    private KieServicesClient kieServerClientABC;
    private KieServicesClient kieServerClientDEF;
    private KieServicesClient kieServerClientGHI;

    private ProcessServicesClient smartProcessServicesClient;
    private UserTaskServicesClient smartTaskServicesClient;
    private QueryServicesClient smartQueryServicesClient;

    private ProcessServicesClient kieServerABCProcessServicesClient;
    private UserTaskServicesClient kieServerABCTaskServicesClient;
    private QueryServicesClient kieServerABCQueryServicesClient;

    private ProcessServicesClient kieServerDEFProcessServicesClient;
    private UserTaskServicesClient kieServerDEFTaskServicesClient;
    private QueryServicesClient kieServerDEFQueryServicesClient;

    private ProcessServicesClient kieServerGHIProcessServicesClient;
    private UserTaskServicesClient kieServerGHITaskServicesClient;
    private QueryServicesClient kieServerGHIQueryServicesClient;

    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        controller = deploymentScenarioFactory.getWorkbenchSettingsBuilder()
                .withApplicationName(CONTROLLER_NAME)
                .withHostame(RANDOM_URL_PREFIX + CONTROLLER_HOSTNAME)
                .build();

        smartRouter = deploymentScenarioFactory.getSmartRouterSettingsBuilder()
                .withApplicationName(SMART_ROUTER_NAME)
                .withSmartRouterID(SMART_ROUTER_ID)
                .withControllerConnection(RANDOM_URL_PREFIX + CONTROLLER_HOSTNAME, PORT)
                .withHostame(RANDOM_URL_PREFIX + SMART_ROUTER_HOSTNAME)
                .withSmartRouterExternalUrl("http://" + RANDOM_URL_PREFIX + SMART_ROUTER_HOSTNAME + ":" + PORT)
                .build();

        kieServerABC = configureKieServer(deploymentScenarioFactory, KIE_SERVER_ABC_NAME);
        kieServerDEF = configureKieServer(deploymentScenarioFactory, KIE_SERVER_DEF_NAME);
        kieServerGHI = configureKieServer(deploymentScenarioFactory, KIE_SERVER_GHI_NAME);

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withWorkbench(controller)
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
                .withControllerConnection(RANDOM_URL_PREFIX + CONTROLLER_HOSTNAME, PORT)
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
    }

    @BeforeClass
    public static void buildKjar() {
        MavenDeployer.buildAndDeployMavenProject(EmptyManagedKieServersWithSmartRouterAndControllerIntegrationTest.class.getResource("/kjars-sources/definition-project-snapshot").getFile());
    }

    @Before
    public void setUp() {
        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployments().get(0));
        smartRouterAdminClient = SmartRouterAdminClientProvider.getSmartRouterClient(deploymentScenario.getSmartRouterDeployments().get(0));

        kieServerClientABC = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(0));
        kieServerClientDEF = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(1));
        kieServerClientGHI = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(2));
        smartRouterClient = KieServerClientProvider.getSmartRouterClient(deploymentScenario.getSmartRouterDeployments().get(0), DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword());

        smartProcessServicesClient = smartRouterClient.getServicesClient(ProcessServicesClient.class);
        smartTaskServicesClient = smartRouterClient.getServicesClient(UserTaskServicesClient.class);
        smartQueryServicesClient = smartRouterClient.getServicesClient(QueryServicesClient.class);

        kieServerABCProcessServicesClient = kieServerClientABC.getServicesClient(ProcessServicesClient.class);
        kieServerABCTaskServicesClient = kieServerClientABC.getServicesClient(UserTaskServicesClient.class);
        kieServerABCQueryServicesClient = kieServerClientABC.getServicesClient(QueryServicesClient.class);

        kieServerDEFProcessServicesClient = kieServerClientDEF.getServicesClient(ProcessServicesClient.class);
        kieServerDEFTaskServicesClient = kieServerClientDEF.getServicesClient(UserTaskServicesClient.class);
        kieServerDEFQueryServicesClient = kieServerClientDEF.getServicesClient(QueryServicesClient.class);

        kieServerGHIProcessServicesClient = kieServerClientGHI.getServicesClient(ProcessServicesClient.class);
        kieServerGHITaskServicesClient = kieServerClientGHI.getServicesClient(UserTaskServicesClient.class);
        kieServerGHIQueryServicesClient = kieServerClientGHI.getServicesClient(QueryServicesClient.class);
    }

    @Test
    @Ignore("https://issues.jboss.org/browse/RHBPMS-4899 && https://issues.jboss.org/browse/JBPM-6623")
    public void testEmptyManagedKieServersWithSmartRouterAndControllerArchitecture() {
        connectionBetweenDeployments();
        scaleKieServerTo(deploymentScenario.getKieServerDeployments(), 3);
        workWithSignalsAndTasks();
    }

    private void connectionBetweenDeployments() {
        String kieServerIdABC = getKieServerId(kieServerClientABC);
        String kieServerIdDEF = getKieServerId(kieServerClientDEF);
        String kieServerIdGHI = getKieServerId(kieServerClientGHI);

        logger.debug("Check architecture after start");
        verifyServerTemplateContainsKieServers(kieControllerClient, SMART_ROUTER_ID, 1);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdABC, 1);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdDEF, 1);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdGHI, 1);

        logger.debug("Deploy a container to kie server");
        deployAndStartContainer(kieServerClientABC, CONTAINER_ID_ABC, deploymentScenario.getKieServerDeployments().get(0));
        waitUntilKieServerLogsContain(deploymentScenario.getKieServerDeployments().get(0), "Container cont-id-abc (for release id org.kie.server.testing:definition-project-snapshot:1.0.0-SNAPSHOT) successfully started");
        verifySmartRouterContainsKieServers(smartRouterAdminClient, 1, Arrays.asList(CONTAINER_ID_ABC), kieServerIdABC);
        verifySmartRouterHostPerContainerContainsServers(smartRouterAdminClient, CONTAINER_ID_ABC, kieServerIdABC);
        verifySmartRouterHostPerServerContainsContainers(smartRouterAdminClient, kieServerIdABC, CONTAINER_ID_ABC);

        logger.debug("Scale up all Kie server deployments to 3");
        scaleKieServerTo(deploymentScenario.getKieServerDeployments(), 3);

        logger.debug("Check all");
        verifyServerTemplateContainsKieServers(kieControllerClient, SMART_ROUTER_ID, 1);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdABC, 3);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdDEF, 3);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdGHI, 3);
        verifyServerTemplateContainsContainers(kieControllerClient, SMART_ROUTER_ID, CONTAINER_ID_ABC);
        verifySmartRouterContainsKieServers(smartRouterAdminClient, 3, Arrays.asList(CONTAINER_ID_ABC), kieServerIdABC);
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
        Stream.of(kieServerABCQueryServicesClient, kieServerDEFQueryServicesClient, kieServerGHIQueryServicesClient)
                .forEach(client -> assertThat(client.findProcessInstances(0, 10)).hasSize(2));

        smartProcessServicesClient.signalProcessInstances(CONTAINER_ID_ABC, signalProcessInstances, Constants.Signal.SIGNAL_NAME, null);
        smartProcessServicesClient.signalProcessInstance(CONTAINER_ID_DEF, signalProcessInstances.get(2), Constants.Signal.SIGNAL_NAME, null);
        smartProcessServicesClient.signalProcessInstance(CONTAINER_ID_DEF, signalProcessInstances.get(3), Constants.Signal.SIGNAL_2_NAME, null);
        kieServerGHIProcessServicesClient.signalProcessInstances(CONTAINER_ID_ABC, signalProcessInstances, Constants.Signal.SIGNAL_NAME, null);

        assertThat(smartTaskServicesClient.findTasks(Constants.User.YODA, 0, 10)).hasSize(3);
        assertThat(kieServerABCTaskServicesClient.findTasks(Constants.User.YODA, 0, 10)).hasSize(2);
        assertThat(kieServerDEFTaskServicesClient.findTasks(Constants.User.YODA, 0, 10)).hasSize(1);
        assertThat(kieServerGHITaskServicesClient.findTasks(Constants.User.YODA, 0, 10)).hasSize(2);

        smartProcessServicesClient.signal(CONTAINER_ID_ABC, Constants.Signal.SIGNAL_2_NAME, null);
        assertThat(smartQueryServicesClient.findProcessInstancesByStatus(Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED), 0, 10)).hasSize(2);
        assertThat(smartQueryServicesClient.findProcessInstancesByStatus(Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE), 0, 10)).hasSize(4);

        kieServerDEFProcessServicesClient.signal(CONTAINER_ID_DEF, Constants.Signal.SIGNAL_2_NAME, null);
        assertThat(smartQueryServicesClient.findProcessInstancesByStatus(Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED), 0, 10)).hasSize(3);
        assertThat(smartQueryServicesClient.findProcessInstancesByStatus(Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE), 0, 10)).hasSize(3);

        List<TaskSummary> activeTasks = smartTaskServicesClient.findTasks(Constants.User.YODA, 0, 10);
        assertThat(activeTasks).hasSize(2);
        assertThat(activeTasks.get(0).getContainerId()).isEqualTo(CONTAINER_ID_GHI);
        assertThat(activeTasks.get(0).getProcessInstanceId()).isEqualTo(signalProcessInstances.get(4));
        assertThat(activeTasks.get(1).getContainerId()).isEqualTo(CONTAINER_ID_GHI);
        assertThat(activeTasks.get(1).getProcessInstanceId()).isEqualTo(signalProcessInstances.get(5));

        smartTaskServicesClient.startTask(CONTAINER_ID_GHI, activeTasks.get(0).getId(), Constants.User.YODA);
        kieServerGHITaskServicesClient.startTask(CONTAINER_ID_GHI, activeTasks.get(1).getId(), Constants.User.YODA);
        kieServerGHITaskServicesClient.completeTask(CONTAINER_ID_GHI, activeTasks.get(0).getId(), Constants.User.YODA, Collections.emptyMap());
        smartTaskServicesClient.completeTask(CONTAINER_ID_GHI, activeTasks.get(1).getId(), Constants.User.YODA, Collections.emptyMap());

        assertThat(smartQueryServicesClient.findProcessesByContainerId(CONTAINER_ID_GHI, 0, 10)).hasSize(2);
        smartProcessServicesClient.abortProcessInstance(CONTAINER_ID_DEF, signalProcessInstances.get(3));

        assertThat(smartQueryServicesClient.findProcessInstancesByStatus(Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED), 0, 10)).hasSize(6);
        assertThat(smartQueryServicesClient.findProcessInstancesByStatus(Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE), 0, 10)).hasSize(0);
        assertThat(smartTaskServicesClient.findTasks(Constants.User.YODA, 0, 10)).hasSize(0);
    }

    private void waitUntilKieServerLogsContain(KieServerDeployment kieServerDeployment, String logMessage) {
        for (Instance kieServerInstance : kieServerDeployment.getInstances()) {
            TimeUtils.wait(Duration.ofSeconds(15), Duration.ofSeconds(1), () -> kieServerInstance.getLogs().contains(logMessage));
        }
    }

    private List<Long> createSignalProcesses() {
        List<Long> signalProcessInstances = new ArrayList<>();

        signalProcessInstances.add(smartProcessServicesClient.startProcess(CONTAINER_ID_ABC, Constants.ProcessId.SIGNALUSERTASK));
        signalProcessInstances.add(kieServerABCProcessServicesClient.startProcess(CONTAINER_ID_ABC, Constants.ProcessId.SIGNALUSERTASK));

        signalProcessInstances.add(smartProcessServicesClient.startProcess(CONTAINER_ID_DEF, Constants.ProcessId.SIGNALUSERTASK));
        signalProcessInstances.add(kieServerDEFProcessServicesClient.startProcess(CONTAINER_ID_DEF, Constants.ProcessId.SIGNALUSERTASK));

        signalProcessInstances.add(smartProcessServicesClient.startProcess(CONTAINER_ID_GHI, Constants.ProcessId.SIGNALUSERTASK));
        signalProcessInstances.add(kieServerGHIProcessServicesClient.startProcess(CONTAINER_ID_GHI, Constants.ProcessId.SIGNALUSERTASK));

        return signalProcessInstances;
    }
    private void deployAndStartContainer(KieServicesClient kieServerClient, String containerId, KieServerDeployment kieServerDeployment) {
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        ServerTemplate serverTemplate = kieControllerClient.getServerTemplate(serverInfo.getServerId());
        ReleaseId definitionSnapshotId = new ReleaseId(PROJECT_GROUP_ID, DEFINITION_PROJECT_SNAPSHOT_NAME, DEFINITION_PROJECT_SNAPSHOT_VERSION);
        ContainerSpec containerSpec = new ContainerSpec(serverInfo.getName(), containerId, serverTemplate, definitionSnapshotId, KieContainerStatus.STARTED, Collections.emptyMap());
        kieControllerClient.saveContainerSpec(serverTemplate.getId(), containerSpec);
        // Wait until container is started in Kie server
        KieServerClientProvider.waitForContainerStart(kieServerDeployment, containerId);
        // Wait until container is registered in Smart router
        SmartRouterUtils.waitForContainerStart(smartRouterAdminClient, containerId);
        // Wait until container is registered in Workbench under Smart router server template
        // (containers are registered asynchronously in a batch).
        WorkbenchUtils.waitForContainerRegistration(kieControllerClient, SMART_ROUTER_ID, containerId);
    }
}
