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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.SmartRouterAdminClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.cloud.integrationtests.util.SmartRouterUtils;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(JBPMOnly.class)
public class KieServerWithSmartRouterAndControllerSurvivalIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<GenericScenario> {

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithSmartRouterAndControllerSurvivalIntegrationTest.class);

    private static final String SMART_ROUTER_ID = "test-kie-router";

    private static final String CONTROLLER_NAME = "controller";
    private static final String SMART_ROUTER_NAME = "smart-router";
    private static final String KIE_SERVER_NAME = "kie-server";

    private static final String PORT = "80";

    private DeploymentSettings controllerSettings;
    private DeploymentSettings smartRouterSettings;
    private DeploymentSettings kieServerSettings;

    private KieServerControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;
    private KieServicesClient smartRouterClient;
    private KieServerRouterClient smartRouterAdminClient;

    private ProcessServicesClient kieServerProcessClient;
    private ProcessServicesClient smartRouterProcessClient;

    private String repositoryName;

    private final String RANDOM_URL_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";

    private final String CONTROLLER_HOSTNAME = RANDOM_URL_PREFIX + CONTROLLER_NAME + DeploymentConstants.getDefaultDomainSuffix();
    private final String SMART_ROUTER_HOSTNAME = RANDOM_URL_PREFIX + SMART_ROUTER_NAME + DeploymentConstants.getDefaultDomainSuffix();
    private final String KIE_SERVER_HOSTNAME = RANDOM_URL_PREFIX + KIE_SERVER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    private static final String SORT_BY_DATE = "start_date";

    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        controllerSettings = deploymentScenarioFactory.getWorkbenchSettingsBuilder()
                .withApplicationName(CONTROLLER_NAME)
                .withHostame(CONTROLLER_HOSTNAME)
                .build();

        smartRouterSettings = deploymentScenarioFactory.getSmartRouterSettingsBuilder()
                .withApplicationName(SMART_ROUTER_NAME)
                .withSmartRouterID(SMART_ROUTER_ID)
                .withControllerConnection(CONTROLLER_NAME + "-rhpamcentr")
                .withControllerUser(DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword())
                .withHostame(SMART_ROUTER_HOSTNAME)
                .withSmartRouterExternalUrl("http://" + SMART_ROUTER_HOSTNAME + ":" + PORT)
                .build();

        kieServerSettings = deploymentScenarioFactory.getKieServerMySqlSettingsBuilder()
                .withApplicationName(KIE_SERVER_NAME)
                .withHostame(KIE_SERVER_HOSTNAME)
                .withAdminUser(DeploymentConstants.getWorkbenchUser(), DeploymentConstants.getWorkbenchPassword())
                .withControllerUser(DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword())
                .withControllerConnection(CONTROLLER_NAME + "-rhpamcentr")
                .withSmartRouterConnection(SMART_ROUTER_NAME + "-smartrouter")
                .withMavenRepoService(CONTROLLER_NAME + "-rhpamcentr")
                .withMavenRepoServiceUser(DeploymentConstants.getWorkbenchMavenUser(), DeploymentConstants.getWorkbenchMavenPassword())
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withWorkbench(controllerSettings)
                .withSmartRouter(smartRouterSettings)
                .withKieServer(kieServerSettings)
                .build();
    }

    @Before
    public void setUp() {
        repositoryName = getGitProvider().createGitRepositoryWithPrefix(controllerDeployment().getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEFINITION_PROJECT_NAME).getFile());

        WorkbenchUtils.deployProjectToWorkbench(getGitProvider().getRepositoryUrl(repositoryName), controllerDeployment(), DEFINITION_PROJECT_NAME);

        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(controllerDeployment());
        kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment());
        smartRouterClient = KieServerClientProvider.getSmartRouterClient(smartRouterDeployment(), kieServerDeployment().getUsername(), kieServerDeployment().getPassword());
        smartRouterAdminClient = SmartRouterAdminClientProvider.getSmartRouterClient(smartRouterDeployment());

        kieServerProcessClient = kieServerClient.getServicesClient(ProcessServicesClient.class);
        smartRouterProcessClient = smartRouterClient.getServicesClient(ProcessServicesClient.class);
    }

    @After
    public void tearDown() {
        getGitProvider().deleteGitRepository(repositoryName);
    }

    @Test
    public void testScaleAllDeploymentsToZeroAndBackToOne() {
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();

        logger.debug("Register Kie Container to Kie Server");
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(kieServerDeployment(), CONTAINER_ID);
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

    private void verifyServerTemplateContainsKieServers(String serverTemplate, int numberOfKieServers) {
        Collection<ServerInstanceKey> kieServers = kieControllerClient.getServerTemplate(serverTemplate).getServerInstanceKeys();
        assertThat(kieServers).hasSize(numberOfKieServers);
    }

    private void scaleKieServerTo(int count) {
        kieServerDeployment().scale(count);
        kieServerDeployment().waitForScale();
    }

    private void scaleControllerTo(int count) {
        controllerDeployment().scale(count);
        controllerDeployment().waitForScale();
    }

    private void scaleSmartRouterTo(int count) {
        smartRouterDeployment().scale(count);
        smartRouterDeployment().waitForScale();
    }

    private WorkbenchDeployment controllerDeployment() {
        return deploymentScenario.getWorkbenchDeployments().get(0);
    }

    private KieServerDeployment kieServerDeployment() {
        return deploymentScenario.getKieServerDeployments().get(0);
    }

    private SmartRouterDeployment smartRouterDeployment() {
        return deploymentScenario.getSmartRouterDeployments().get(0);
    }
}
