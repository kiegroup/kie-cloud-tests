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
package org.kie.cloud.integrationtests.survival;

import java.util.Collection;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.deployment.KjarDeployer;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.SmartRouterAdminClientProvider;
import org.kie.cloud.common.util.AwaitilityUtils;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.MonitoringK8sFs;
import org.kie.cloud.integrationtests.category.OperatorNotSupported;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.SmartRouterUtils;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.cloud.tests.common.time.Constants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@Category({JBPMOnly.class, OperatorNotSupported.class, MonitoringK8sFs.class})
public class KieServerWithSmartRouterAndControllerSurvivalIntegrationTest extends AbstractCloudIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithSmartRouterAndControllerSurvivalIntegrationTest.class);

    private static ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario deploymentScenario;

    private static final String SMART_ROUTER_ID = "kie-server-router";

    private KieServerControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;
    private KieServicesClient smartRouterClient;
    private KieServerRouterClient smartRouterAdminClient;

    private ProcessServicesClient kieServerProcessClient;
    private ProcessServicesClient smartRouterProcessClient;

    private static final String SORT_BY_DATE = "start_date";

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                .withInternalMavenRepo()
                .build();
        deploymentScenario.setLogFolderName(KieServerWithSmartRouterAndControllerSurvivalIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        KjarDeployer.create(Kjar.DEFINITION_SNAPSHOT).deploy(deploymentScenario.getMavenRepositoryDeployment());
    }

    @Before
    public void setUp() {
        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchRuntimeDeployment());
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerOneDeployment());
        smartRouterClient = KieServerClientProvider.getSmartRouterClient(deploymentScenario.getSmartRouterDeployment(), deploymentScenario.getKieServerOneDeployment().getUsername(), deploymentScenario.getKieServerOneDeployment().getPassword());
        smartRouterAdminClient = SmartRouterAdminClientProvider.getSmartRouterClient(deploymentScenario.getSmartRouterDeployment());

        kieServerProcessClient = kieServerClient.getServicesClient(ProcessServicesClient.class);
        smartRouterProcessClient = smartRouterClient.getServicesClient(ProcessServicesClient.class);
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testScaleAllDeploymentsToZeroAndBackToOne() {
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();

        logger.debug("Register Kie Container to Kie Server");
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION_SNAPSHOT, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerOneDeployment(), CONTAINER_ID);
        SmartRouterUtils.waitForContainerStart(smartRouterAdminClient, CONTAINER_ID);
        WorkbenchUtils.waitForContainerRegistration(kieControllerClient, SMART_ROUTER_ID, CONTAINER_ID);

        verifyContainerIsDeployed(kieServerClient, CONTAINER_ID);
        verifyContainerIsDeployed(smartRouterClient, CONTAINER_ID);

        verifyServerTemplateContainsKieServers(serverInfo.getServerId(), 1);
        verifyServerTemplateContainsKieServers(SMART_ROUTER_ID, 1);

        verifyServerTemplateContainsContainer(serverInfo.getServerId(), CONTAINER_ID);
        verifyServerTemplateContainsContainer(SMART_ROUTER_ID, CONTAINER_ID);

        logger.debug("Start process instance");
        Long kieServerSignalPid = kieServerProcessClient.startProcess(CONTAINER_ID, Constants.ProcessId.SIGNALTASK);
        assertThat(kieServerProcessClient.findProcessInstances(CONTAINER_ID, 0, 10)).isNotNull().hasSize(1);

        logger.debug("Start process instance");
        Long smartRouterSignalPid = smartRouterProcessClient.startProcess(CONTAINER_ID, Constants.ProcessId.SIGNALTASK);
        assertThat(smartRouterProcessClient.findProcessInstances(CONTAINER_ID, 0, 10)).isNotNull().hasSize(2);

        logger.debug("Scale Kie server and Smart router to 0");
        scaleSmartRouterTo(0);
        scaleKieServerTo(0);
        logger.debug("Verifiy that Server templates not contains any servers");
        verifyServerTemplateContainsKieServers(serverInfo.getServerId(), 0);
        verifyServerTemplateContainsKieServers(SMART_ROUTER_ID, 0);
        logger.debug("Scale Controller to 0");
        scaleControllerTo(0);

        logger.debug("Scale all back to 1");
        scaleControllerTo(1);
        scaleSmartRouterTo(1);
        scaleKieServerTo(1);

        verifyServerTemplateContainsKieServers(serverInfo.getServerId(), 1);
        verifyServerTemplateContainsKieServers(SMART_ROUTER_ID, 1);

        verifyServerTemplateContainsContainer(serverInfo.getServerId(), CONTAINER_ID);
        verifyServerTemplateContainsContainer(SMART_ROUTER_ID, CONTAINER_ID);

        verifyContainerIsDeployed(kieServerClient, CONTAINER_ID);
        verifyContainerIsDeployed(smartRouterClient, CONTAINER_ID);

        logger.debug("Check started processes");
        List<ProcessInstance> processInstances = smartRouterProcessClient.findProcessInstances(CONTAINER_ID, 0, 10, SORT_BY_DATE, true);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(processInstances).isNotNull().hasSize(2);
            softly.assertThat(processInstances.get(0).getId()).isEqualTo(kieServerSignalPid);
            softly.assertThat(processInstances.get(0).getProcessId()).isEqualTo(Constants.ProcessId.SIGNALTASK);
            softly.assertThat(processInstances.get(0).getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
            softly.assertThat(processInstances.get(1).getId()).isEqualTo(smartRouterSignalPid);
            softly.assertThat(processInstances.get(1).getProcessId()).isEqualTo(Constants.ProcessId.SIGNALTASK);
            softly.assertThat(processInstances.get(1).getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        });

        kieServerProcessClient.signal(CONTAINER_ID, Constants.Signal.SIGNAL_NAME, null);
        assertThat(kieServerProcessClient.getProcessInstance(CONTAINER_ID, kieServerSignalPid).getState())
                .isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
        assertThat(kieServerProcessClient.getProcessInstance(CONTAINER_ID, smartRouterSignalPid).getState())
                .isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }

    private void verifyContainerIsDeployed(KieServicesClient kieServerClient, String containerId) {
        ServiceResponse<KieContainerResourceList> containers = kieServerClient.listContainers();
        assertThat(containers.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        List<KieContainerResource> containerList = containers.getResult().getContainers();
        assertThat(containerList).hasSize(1);
        assertThat(containerList.get(0).getContainerId()).isEqualTo(containerId);
        assertThat(containerList.get(0).getStatus()).isEqualTo(KieContainerStatus.STARTED);
    }

    private void verifyServerTemplateContainsContainer(String serverTemplate, String containerId) {
        Collection<ContainerSpec> containersSpec = kieControllerClient.getServerTemplate(serverTemplate).getContainersSpec();
        assertThat(containersSpec).hasSize(1);
        assertThat(containersSpec.iterator().next().getId()).isEqualTo(containerId);
    }

    private void verifyServerTemplateContainsKieServers(String serverTemplateName, int numberOfKieServers) {
        AwaitilityUtils.untilAsserted(() -> kieControllerClient.getServerTemplate(serverTemplateName),
                                      serverTemplate -> assertThat(serverTemplate.getServerInstanceKeys()).hasSize(numberOfKieServers));
    }

    private void scaleKieServerTo(int count) {
        deploymentScenario.getKieServerOneDeployment().scale(count);
        deploymentScenario.getKieServerOneDeployment().waitForScale();
    }

    private void scaleControllerTo(int count) {
        deploymentScenario.getWorkbenchRuntimeDeployment().scale(count);
        deploymentScenario.getWorkbenchRuntimeDeployment().waitForScale();
    }

    private void scaleSmartRouterTo(int count) {
        deploymentScenario.getSmartRouterDeployment().scale(count);
        deploymentScenario.getSmartRouterDeployment().waitForScale();
    }
}
