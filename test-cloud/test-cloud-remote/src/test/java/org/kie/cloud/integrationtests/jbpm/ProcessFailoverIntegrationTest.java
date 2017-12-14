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
package org.kie.cloud.integrationtests.jbpm;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.common.client.util.WorkbenchUtils;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.controller.management.client.KieServerMgmtControllerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessFailoverIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchWithKieServerScenario> {

    protected KieServerMgmtControllerClient kieServerMgmtControllerClient;
    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected QueryServicesClient queryServicesClient;
    private static final Logger logger = LoggerFactory.getLogger(ProcessFailoverIntegrationTest.class);

    private static final String variableKey = "name";
    private static final String variableValueOne = "ONE";
    private static final String variableValueTwo = "TWO";

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
    public void processFailoverTest() {
        logger.debug("Register Kie Container to Kie Server");
        KieServerInfo serverInfo = kieServicesClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieServerMgmtControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, DEFINITION_PROJECT_NAME, DEFINITION_PROJECT_VERSION, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        logger.debug("Get Kie Server Instance");
        Instance kieServerInstance = deploymentScenario.getKieServerDeployment().getInstances().iterator().next();

        logger.debug("Start process instance");
        Long longScriptPid = processServicesClient.startProcess(CONTAINER_ID, LONG_SCRIPT_PROCESS_ID, Collections.emptyMap());
        assertThat(longScriptPid).isNotNull().isGreaterThan(0L);
        assertThat(queryServicesClient.findProcessInstances(0, 10)).isNotNull().hasSize(1);

        assertProcessInstanceState(longScriptPid, org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        assertProcessVariable(longScriptPid, variableKey, variableValueOne);

        logger.debug("Send signal to continue with process.");
        signalStartLongScript(longScriptPid);

        logger.debug("Force delete (Kill) Kie server instance.");
        deploymentScenario.getKieServerDeployment().deleteInstances(kieServerInstance);
        logger.debug("Wait for scale");
        deploymentScenario.getKieServerDeployment().waitForScale();

        logger.debug("Send signal to try complete process. Not able yet.");
        processServicesClient.signalProcessInstance(CONTAINER_ID, longScriptPid, SIGNAL_2_NAME, null);

        assertProcessInstanceState(longScriptPid, org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        assertProcessVariable(longScriptPid, variableKey, variableValueOne);

        logger.debug("Send signal again to continue with process. It was rollbacked.");
        processServicesClient.signalProcessInstance(CONTAINER_ID, longScriptPid, SIGNAL_NAME, null);

        assertProcessInstanceState(longScriptPid, org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        assertProcessVariable(longScriptPid, variableKey, variableValueTwo);

        processServicesClient.signalProcessInstance(CONTAINER_ID, longScriptPid, SIGNAL_2_NAME, null);
        assertProcessInstanceState(longScriptPid, org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

        assertThat(deploymentScenario.getKieServerDeployment().getInstances().iterator().next()).isNotEqualTo(kieServerInstance);
    }

    private void signalStartLongScript(Long pid) {
        new Thread(() -> {
            try {
                ProcessServicesClient processClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
                processClient.signalProcessInstance(CONTAINER_ID, pid, SIGNAL_NAME, null);
            } catch (KieServicesHttpException e) {
                // Expected
            }
        }).start();
    }

    private void assertProcessVariable(Long pid, String variableKey, String expectedValue) {
        Map<String, Object> variables = processServicesClient.getProcessInstanceVariables(CONTAINER_ID, pid);
        assertThat(variables).isNotNull().hasSize(2).containsKeys(variableKey);
        assertThat(variables.get(variableKey)).isNotNull().isEqualTo(expectedValue);
    }

    private void assertProcessInstanceState(Long pid, int state) {
        ProcessInstance processInstance = processServicesClient.getProcessInstance(CONTAINER_ID, pid);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getState()).isNotNull().isEqualTo(state);
    }

}