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

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.common.client.util.WorkbenchUtils;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.controller.management.client.KieServerMgmtControllerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DbSurvivalIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchWithKieServerScenario> {

    private KieServerMgmtControllerClient kieServerMgmtControllerClient;
    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected QueryServicesClient queryServicesClient;
    private static final Logger logger = LoggerFactory.getLogger(DbSurvivalIntegrationTest.class);

    @Override
    protected WorkbenchWithKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchWithKieServerScenarioBuilder().build();
    }

    @Before
    public void setUp() {
        WorkbenchDeployment workbenchDeployment = deploymentScenario.getWorkbenchDeployment();
        gitProvider.createGitRepository(workbenchDeployment.getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER).getFile());
        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(workbenchDeployment.getNamespace()), workbenchDeployment, DEFINITION_PROJECT_NAME);

        kieServerMgmtControllerClient = KieServerControllerClientProvider.getKieServerMgmtControllerClient(deploymentScenario.getWorkbenchDeployment());

        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
        queryServicesClient = KieServerClientProvider.getQueryClient(deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void reconnectionDbTest() {
        logger.debug("Register Kie Container to Kie Server");
        KieServerInfo serverInfo = kieServicesClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieServerMgmtControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, DEFINITION_PROJECT_NAME, DEFINITION_PROJECT_VERSION, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        logger.debug("Start process instance");
        Long signalPid = processServicesClient.startProcess(CONTAINER_ID, SIGNALTASK_PROCESS_ID);
        assertThat(signalPid).isNotNull().isGreaterThan(0L);
        assertThat(queryServicesClient.findProcessInstances(0, 10)).isNotNull().hasSize(1);

        scaleDatabaseTo(0);

        logger.debug("Try to get process instances");
        assertThatThrownBy(() -> {
            processServicesClient.startProcess(CONTAINER_ID, SIGNALTASK_PROCESS_ID);
        }).isInstanceOf(KieServicesException.class);

        scaleDatabaseTo(1);

        waitForKieServerResposne();

        logger.debug("Check started processes");
        assertThat(queryServicesClient.findProcessInstances(0, 10)).isNotNull().hasSize(1);

        logger.debug("Send signal and complete process");
        processServicesClient.signal(CONTAINER_ID, SIGNAL_NAME, null);

        logger.debug("Check that prcoess is completed");
        assertThat(queryServicesClient.findProcessInstanceById(signalPid).getState()).isEqualTo(ProcessInstance.STATE_COMPLETED);

    }

    private void scaleDatabaseTo(int count) {
        logger.debug("Scale Database to " + count);
        deploymentScenario.getDatabaseDeployment().scale(count);
        deploymentScenario.getDatabaseDeployment().waitForScale();
    }

    private void waitForKieServerResposne() {
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(30);

        while (LocalDateTime.now().isBefore(endTime)) {
            try {
                assertThat(queryServicesClient.findProcessInstances(0, 10)).isNotNull();
                return;
            } catch (KieServicesException e) {
                //ok
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Kie Server response.", e);
            }
        }
        throw new RuntimeException("Timeout while waiting for Kie Server.");
    }
}
