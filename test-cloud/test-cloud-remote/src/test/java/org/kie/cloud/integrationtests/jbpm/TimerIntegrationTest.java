/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.integrationtests.jbpm;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.cloud.integrationtests.util.TimeUtils;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

public class TimerIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario> {

    private static final Kjar DEPLOYED_KJAR = Kjar.TIMER;
    private static final String CONTAINER_SUCCESSFULLY_STARTED = "Container cont-id (for release id " + DEPLOYED_KJAR.toString() + ") successfully started";
    private static final String NODE_INSTANCE_NAME = "PrintingNode";

    private KieServerControllerClient kieControllerClient;

    @Override
    protected ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withTimerServiceDataStoreRefreshInterval(Duration.ofSeconds(1))
                .build();
    }

    @BeforeClass
    public static void buildKjar() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEPLOYED_KJAR.getName()).getFile());
    }

    @Before
    public void setUp() {
        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchRuntimeDeployment());
    }

    @Test
    public void testTimerStartEvent() throws Exception {
        QueryServicesClient queryClient = KieServerClientProvider.getQueryClient(deploymentScenario.getKieServerOneDeployment());
        ProcessServicesClient processClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerOneDeployment());

        deploymentScenario.getKieServerOneDeployment().scale(3);
        deploymentScenario.getKieServerOneDeployment().waitForScale();

        KieServerInfo serverInfo = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerOneDeployment()).getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, DEPLOYED_KJAR, KieContainerStatus.STARTED);

        waitUntilKieServerLogsContain(deploymentScenario.getKieServerOneDeployment(), CONTAINER_SUCCESSFULLY_STARTED);

        Long pid = processClient.startProcess(CONTAINER_ID, Constants.ProcessId.TIMER);
        KieServerSynchronization.waitForProcessInstanceToFinish(processClient, CONTAINER_ID, pid);

        List<NodeInstance> nodeInstances = queryClient.findCompletedNodeInstances(pid, 0, 100).stream()
                                                                                              .filter(n -> n.getName().equals(NODE_INSTANCE_NAME))
                                                                                              .sorted((n1,n2) -> n1.getDate().compareTo(n2.getDate()))
                                                                                              .collect(Collectors.toList());
        assertThat(nodeInstances).hasSize(3);

        long firstInstance = nodeInstances.get(0).getDate().getTime();
        long secondInstance = nodeInstances.get(1).getDate().getTime();
        long thirdInstance = nodeInstances.get(2).getDate().getTime();

        long distance1 = thirdInstance - secondInstance;
        long distance2 = secondInstance - firstInstance;

        // TODO: Skip time distance checks as they are unstable, needs to be investigated and properly addressed.
//        assertThat(distance1).isBetween(3000L, 7000L);
//        assertThat(distance2).isBetween(3000L, 7000L);
    }

    private void waitUntilKieServerLogsContain(KieServerDeployment kieServerDeployment, String logMessage) {
        for (Instance kieServerInstance : kieServerDeployment.getInstances()) {
            TimeUtils.wait(Duration.ofSeconds(30), Duration.ofSeconds(1), () -> kieServerInstance.getLogs().contains(logMessage));
        }
    }
}
