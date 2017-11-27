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

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.common.client.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.controller.management.client.KieServerMgmtControllerClient;

public class TimerIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchWithKieServerScenario> {

    private KieServerMgmtControllerClient kieControllerClient;

    @Override
    protected WorkbenchWithKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchWithKieServerScenarioBuilder().build();
    }

    @Before
    public void setUp() {
        WorkbenchDeployment workbenchDeployment = deploymentScenario.getWorkbenchDeployment();
        gitProvider.createGitRepository(workbenchDeployment.getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER).getFile());
        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(workbenchDeployment.getNamespace()), workbenchDeployment, TIMER_PROJECT_NAME);

        kieControllerClient = KieServerControllerClientProvider.getKieServerMgmtControllerClient(deploymentScenario.getWorkbenchDeployment());
    }

    @Test
    @Ignore("Activate when GUVNOR-3361 is fixed.")
    public void testTimerStartEvent() throws Exception {
        QueryServicesClient queryClient = KieServerClientProvider.getQueryClient(deploymentScenario.getKieServerDeployment());

        deploymentScenario.getKieServerDeployment().scale(3);
        deploymentScenario.getKieServerDeployment().waitForScale();

        KieServerInfo serverInfo = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment()).getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, TIMER_PROJECT_NAME, TIMER_PROJECT_VERSION, KieContainerStatus.STARTED);

        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);
        List<Integer> completedOnly = Arrays.asList(org.jbpm.process.instance.ProcessInstance.STATE_COMPLETED);

        // Wait for 15 seconds. The processes should be started in 10 seconds, 5 seconds are a reserve.
        Thread.sleep(15_000L);

        List<ProcessInstance> startedInstances = queryClient.findProcessInstancesByContainerId(CONTAINER_ID, completedOnly, 0, 10, "Id", false);
        assertThat(startedInstances).hasSize(3);

        long thirdInstance = startedInstances.get(0).getDate().getTime();
        long secondInstance = startedInstances.get(1).getDate().getTime();
        long firstInstance = startedInstances.get(2).getDate().getTime();

        long distance1 = thirdInstance - secondInstance;
        long distance2 = secondInstance - firstInstance;

        assertThat(distance1).isBetween(4000L, 6000L);
        assertThat(distance2).isBetween(4000L, 6000L);
    }
}
