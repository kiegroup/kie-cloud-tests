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

import java.util.Collection;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.KjarDeployer;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.api.scenario.MissingResourceException;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.category.Baseline;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.OperatorNotSupported;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.client.KieServerControllerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.cloud.tests.common.client.util.KieServerUtils.waitUntilKieServerContainerIsStarted;
import static org.kie.cloud.tests.common.client.util.KieServerUtils.waitUntilKieServerIsConnected;

@Category({Baseline.class, OperatorNotSupported.class}) // Operator doesn't contain any scenario with controller based strategy by default
public class KieServerWebSocketScalingIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(KieServerWebSocketScalingIntegrationTest.class);

    private ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario deploymentScenario;

    private KieServerControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;

    private static final String CONTAINER_ID = "cont-id";
    private static final String CONTAINER_ALIAS = "cont-alias";

    @Before
    public void setUp() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            // Only RHPAM prod template is using WebSockets now.
            deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                    .withInternalMavenRepo()
                    .build();
            deploymentScenario.setLogFolderName(KieServerWebSocketScalingIntegrationTest.class.getSimpleName());
            ScenarioDeployer.deployScenario(deploymentScenario);
        } catch (MissingResourceException e) {
            logger.warn("Skipping test because of missing resource.", e);
            Assume.assumeNoException(e);
        } catch (UnsupportedOperationException e) {
            logger.warn("Skipping test", e);
            Assume.assumeNoException(e);
        }

        KjarDeployer.create(Kjar.DEFINITION_SNAPSHOT).deploy(deploymentScenario.getMavenRepositoryDeployment());

        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerOneDeployment());
        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchRuntimeDeployment());
    }

    @After
    public void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testConnectionBetweenDeployables() {
        scaleKieServerTo(3);
        verifyServerDeploymentContainInstances(3);

        String kieServerId = getKieServerId(kieServerClient);
        KieServerDeployment kieServerDeployment = deploymentScenario.getKieServerOneDeployment();

        waitUntilKieServerIsConnected(kieServerDeployment);
        // In case of WebSockets all Kie servers with same URL are registered under one server template instance
        verifyServerTemplateContainsKieServers(kieServerId, 1);

        deployAndStartContainer();

        waitUntilKieServerContainerIsStarted(kieServerDeployment, CONTAINER_ID, Kjar.DEFINITION_SNAPSHOT);
        verifyServerTemplateContainsContainer(kieServerId, CONTAINER_ID);

        scaleKieServerTo(1);

        verifyServerTemplateContainsKieServers(kieServerId, 1);
    }

    private String getKieServerId(KieServicesClient kieServerClient) {
        ServiceResponse<KieServerInfo> serverInfo = kieServerClient.getServerInfo();
        assertThat(serverInfo.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        return serverInfo.getResult().getServerId();
    }

    private void verifyServerTemplateContainsContainer(String serverTemplate, String containerId) {
        Collection<ContainerSpec> containersSpec = kieControllerClient.getServerTemplate(serverTemplate).getContainersSpec();
        assertThat(containersSpec).hasSize(1);
        assertThat(containersSpec.iterator().next().getId()).isEqualTo(containerId);
    }

    private void verifyServerDeploymentContainInstances(int numberOfKieServerInstances) {
        assertThat(deploymentScenario.getKieServerOneDeployment().getInstances()).hasSize(numberOfKieServerInstances);
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
        deploymentScenario.getKieServerOneDeployment().scale(count);
        deploymentScenario.getKieServerOneDeployment().waitForScale();
    }
}
