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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(JBPMOnly.class)
public class ProcessFailoverIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<WorkbenchKieServerPersistentScenario> {

    protected KieServerControllerClient kieServerControllerClient;
    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected QueryServicesClient queryServicesClient;

    private String repositoryName;

    private static final Logger logger = LoggerFactory.getLogger(ProcessFailoverIntegrationTest.class);

    private static final String variableKey = "name";
    private static final String variableValueOne = "ONE";
    private static final String variableValueTwo = "TWO";

    @Override
    protected WorkbenchKieServerPersistentScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder().build();
    }

    @Before
    public void setUp() {
        repositoryName = gitProvider.createGitRepositoryWithPrefix(deploymentScenario.getWorkbenchDeployment().getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEFINITION_PROJECT_NAME).getFile());

        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(repositoryName), deploymentScenario.getWorkbenchDeployment(), DEFINITION_PROJECT_NAME);

        kieServerControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());

        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
        queryServicesClient = KieServerClientProvider.getQueryClient(deploymentScenario.getKieServerDeployment());
    }

    @After
    public void tearDown() {
        gitProvider.deleteGitRepository(repositoryName);
    }

    @Test
    public void processFailoverTest() {
        logger.debug("Register Kie Container to Kie Server");
        KieServerInfo serverInfo = kieServicesClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieServerControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        logger.debug("Get Kie Server Instance");
        Instance kieServerInstance = deploymentScenario.getKieServerDeployment().getInstances().iterator().next();

        logger.debug("Start process instance");
        Long longScriptPid = processServicesClient.startProcess(CONTAINER_ID, Constants.ProcessId.LONG_SCRIPT, Collections.emptyMap());
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
        processServicesClient.signalProcessInstance(CONTAINER_ID, longScriptPid, Constants.Signal.SIGNAL_2_NAME, null);

        assertProcessInstanceState(longScriptPid, org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        assertProcessVariable(longScriptPid, variableKey, variableValueOne);

        logger.debug("Send signal again to continue with process. It was rollbacked.");
        processServicesClient.signalProcessInstance(CONTAINER_ID, longScriptPid, Constants.Signal.SIGNAL_NAME, null);

        assertProcessInstanceState(longScriptPid, org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        assertProcessVariable(longScriptPid, variableKey, variableValueTwo);

        processServicesClient.signalProcessInstance(CONTAINER_ID, longScriptPid, Constants.Signal.SIGNAL_2_NAME, null);
        assertProcessInstanceState(longScriptPid, org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

        assertThat(deploymentScenario.getKieServerDeployment().getInstances().iterator().next()).isNotEqualTo(kieServerInstance);
    }

    private void signalStartLongScript(Long pid) {
        new Thread(() -> {
            try {
                ProcessServicesClient processClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
                processClient.signalProcessInstance(CONTAINER_ID, pid, Constants.Signal.SIGNAL_NAME, null);
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