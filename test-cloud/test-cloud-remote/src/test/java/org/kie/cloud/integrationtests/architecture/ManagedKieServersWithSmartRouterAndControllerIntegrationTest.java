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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.SmartRouterAdminClientProvider;
import org.kie.cloud.integrationtests.category.ApbNotSupported;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.cloud.provider.git.Git;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for Architecture 2 from
 * http://mswiderski.blogspot.cz/2017/08/cloud-runtime-architectures-for-jbpm.html
 */
@RunWith(Parameterized.class)
@Category(ApbNotSupported.class)
public class ManagedKieServersWithSmartRouterAndControllerIntegrationTest extends AbstractCloudArchitectureIntegrationTest {

    @Parameter(value = 0)
    public String testScenarioName;
    @Parameter(value = 1)
    public KieServerS2ISettingsBuilder kieServerS2ISettingsBuilder;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        KieServerS2ISettingsBuilder kieServerHttpsS2ISettings = deploymentScenarioFactory.getKieServerHttpsS2ISettingsBuilder();

        return Arrays.asList(new Object[][]{
            {"KIE Server HTTPS S2I", kieServerHttpsS2ISettings}
        });
    }

    private static final Logger logger = LoggerFactory.getLogger(ManagedKieServersWithSmartRouterAndControllerIntegrationTest.class);

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

    private ProcessServicesClient smartProcessServicesClient;
    private UserTaskServicesClient smartTaskServicesClient;
    private QueryServicesClient smartQueryServicesClient;

    private KieServicesClient kieServerClientABC;
    private ProcessServicesClient kieServerABCProcessServicesClient;
    private UserTaskServicesClient kieServerABCTaskServicesClient;
    private QueryServicesClient kieServerABCQueryServicesClient;

    private KieServicesClient kieServerClientDEF;
    private ProcessServicesClient kieServerDEFProcessServicesClient;
    private UserTaskServicesClient kieServerDEFTaskServicesClient;
    private QueryServicesClient kieServerDEFQueryServicesClient;

    private KieServicesClient kieServerClientGHI;
    private ProcessServicesClient kieServerGHIProcessServicesClient;
    private UserTaskServicesClient kieServerGHITaskServicesClient;
    private QueryServicesClient kieServerGHIQueryServicesClient;

    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        repositoryName = Git.getProvider().createGitRepositoryWithPrefix("architectureRepository", ManagedKieServersWithSmartRouterAndControllerIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

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

        kieServerABC = configureKieServerS2I(deploymentScenarioFactory, KIE_SERVER_ABC_NAME, KIE_CONTAINER_DEPLOYMENT_ABC);
        kieServerDEF = configureKieServerS2I(deploymentScenarioFactory, KIE_SERVER_DEF_NAME, KIE_CONTAINER_DEPLOYMENT_DEF);
        kieServerGHI = configureKieServerS2I(deploymentScenarioFactory, KIE_SERVER_GHI_NAME, KIE_CONTAINER_DEPLOYMENT_GHI);

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withWorkbench(controller)
                .withSmartRouter(smartRouter)
                .withKieServer(kieServerABC, kieServerDEF, kieServerGHI)
                .build();
    }

    private DeploymentSettings configureKieServerS2I(DeploymentScenarioBuilderFactory deploymentScenarioFactory, String applicationName, String containerDeploymnet) {
        return kieServerS2ISettingsBuilder
                .withApplicationName(applicationName)
                .withHostame(RANDOM_URL_PREFIX + applicationName + DeploymentConstants.getDefaultDomainSuffix())
                .withControllerUser(DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword())
                .withSmartRouterConnection(RANDOM_URL_PREFIX + SMART_ROUTER_HOSTNAME, PORT)
                .withControllerConnection(RANDOM_URL_PREFIX + CONTROLLER_HOSTNAME, PORT)
                .withContainerDeployment(containerDeploymnet)
                .withSourceLocation(Git.getProvider().getRepositoryUrl(repositoryName), REPO_BRANCH, DEFINITION_PROJECT_NAME)
                .build();
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

    @After
    public void deleteRepo() {
        Git.getProvider().deleteGitRepository(repositoryName);
    }

    @Test
    @Ignore("https://issues.jboss.org/browse/RHBPMS-5055 (Container is deleted when kie server connects to server template)")
    public void testManagedKieServersWithSmartRouterAndControllerArchitecture() {
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
        // containers are deleted when kie server is connected to the controller
        verifyContainerIsDeployed(kieServerClientABC, CONTAINER_ID_ABC);
        verifyContainerIsDeployed(kieServerClientDEF, CONTAINER_ID_DEF);
        verifyContainerIsDeployed(kieServerClientGHI, CONTAINER_ID_GHI);
        verifyServerTemplateContainsContainers(kieControllerClient, SMART_ROUTER_ID, CONTAINER_ID_ABC, CONTAINER_ID_DEF, CONTAINER_ID_GHI);
        verifyServerTemplateContainsContainers(kieControllerClient, kieServerIdABC, CONTAINER_ID_ABC);
        verifyServerTemplateContainsContainers(kieControllerClient, kieServerIdDEF, CONTAINER_ID_DEF);
        verifyServerTemplateContainsContainers(kieControllerClient, kieServerIdGHI, CONTAINER_ID_GHI);

        logger.debug("Check smart router list");
        verifySmartRouterContainsKieServers(smartRouterAdminClient, 1, Arrays.asList(CONTAINER_ID_ABC, CONTAINER_ID_DEF, CONTAINER_ID_GHI), kieServerIdABC, kieServerIdDEF, kieServerIdGHI);
        verifySmartRouterHostPerContainerContainsServers(smartRouterAdminClient, CONTAINER_ID_ABC, kieServerIdABC);
        verifySmartRouterHostPerContainerContainsServers(smartRouterAdminClient, CONTAINER_ID_DEF, kieServerIdDEF);
        verifySmartRouterHostPerContainerContainsServers(smartRouterAdminClient, CONTAINER_ID_GHI, kieServerIdGHI);
        verifySmartRouterHostPerServerContainsContainers(smartRouterAdminClient, kieServerIdABC, CONTAINER_ID_ABC);
        verifySmartRouterHostPerServerContainsContainers(smartRouterAdminClient, kieServerIdDEF, CONTAINER_ID_DEF);
        verifySmartRouterHostPerServerContainsContainers(smartRouterAdminClient, kieServerIdGHI, CONTAINER_ID_GHI);

        logger.debug("Scale up all Kie server deployments"); //eg to 3
        scaleKieServerTo(deploymentScenario.getKieServerDeployments(), 3);

        logger.debug("Check all");
        verifyServerTemplateContainsKieServers(kieControllerClient, SMART_ROUTER_ID, 9);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdABC, 3);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdDEF, 3);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdGHI, 3);
        verifyServerTemplateContainsContainers(kieControllerClient, SMART_ROUTER_ID, CONTAINER_ID_ABC, CONTAINER_ID_DEF, CONTAINER_ID_GHI);
        verifySmartRouterContainsKieServers(smartRouterAdminClient, 3, Arrays.asList(CONTAINER_ID_ABC, CONTAINER_ID_DEF, CONTAINER_ID_GHI), kieServerIdABC, kieServerIdDEF, kieServerIdGHI);

        verifyContainerIsDeployed(kieServerClientABC, CONTAINER_ID_ABC);
        verifyContainerIsDeployed(kieServerClientDEF, CONTAINER_ID_DEF);
        verifyContainerIsDeployed(kieServerClientGHI, CONTAINER_ID_GHI);
        verifyServerTemplateContainsContainers(kieControllerClient, SMART_ROUTER_ID, CONTAINER_ID_ABC, CONTAINER_ID_DEF, CONTAINER_ID_GHI);
        verifyServerTemplateContainsContainers(kieControllerClient, kieServerIdABC, CONTAINER_ID_ABC);
        verifyServerTemplateContainsContainers(kieControllerClient, kieServerIdDEF, CONTAINER_ID_DEF);
        verifyServerTemplateContainsContainers(kieControllerClient, kieServerIdGHI, CONTAINER_ID_GHI);

        logger.debug("scale some server down");
        scaleKieServerTo(0, deploymentScenario.getKieServerDeployments().get(1));

        logger.debug("check all again");
        verifyServerTemplateContainsKieServers(kieControllerClient, SMART_ROUTER_ID, 6);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdABC, 3);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdDEF, 0);
        verifyServerTemplateContainsKieServers(kieControllerClient, kieServerIdGHI, 3);
        verifyServerTemplateContainsContainers(kieControllerClient, SMART_ROUTER_ID, CONTAINER_ID_ABC, CONTAINER_ID_GHI);
        verifySmartRouterContainsKieServers(smartRouterAdminClient, 2, Arrays.asList(CONTAINER_ID_ABC, CONTAINER_ID_GHI), kieServerIdABC, kieServerIdGHI);
    }

    private void workWithSignalsAndTasks() {
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
}
