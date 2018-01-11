package org.kie.cloud.integrationtests.smoke;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.WorkbenchKieServerDatabaseScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;

@Category(Smoke.class)
public class MultipleProcessesIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchKieServerDatabaseScenario> {

    private KieServerControllerClient kieControllerClient;

    private KieServicesClient kieServerClient;
    private ProcessServicesClient processClient;
    private UserTaskServicesClient taskClient;

    @Override
    protected WorkbenchKieServerDatabaseScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchKieServerDatabaseScenarioBuilder().build();
    }

    @Before
    public void setUp() {
        WorkbenchUtils.deployProjectToWorkbench(gitProvider, deploymentScenario.getWorkbenchDeployment(), DEFINITION_PROJECT_NAME);

        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        processClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
        taskClient = KieServerClientProvider.getTaskClient(deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testMultipleDifferentProcessesOnSameKieServer() {
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, DEFINITION_PROJECT_NAME, DEFINITION_PROJECT_VERSION, KieContainerStatus.STARTED);

        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        Long userTaskPid = processClient.startProcess(CONTAINER_ID, USERTASK_PROCESS_ID);
        Assertions.assertThat(userTaskPid).isNotNull();
        Long signalTaskPid = processClient.startProcess(CONTAINER_ID, SIGNALTASK_PROCESS_ID);
        Assertions.assertThat(signalTaskPid).isNotNull();

        finishUserTaskProcess();
        finishSignalTaskProcess(signalTaskPid);

        ProcessInstance userTaskPi = processClient.getProcessInstance(CONTAINER_ID, userTaskPid);
        Assertions.assertThat(userTaskPi).isNotNull();
        Assertions.assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

        ProcessInstance signalTaskPi = processClient.getProcessInstance(CONTAINER_ID, signalTaskPid);
        Assertions.assertThat(signalTaskPi).isNotNull();
        Assertions.assertThat(signalTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }

    private void finishUserTaskProcess() {
        List<TaskSummary> tasks = taskClient.findTasks(USER_YODA, 0, 10);
        Assertions.assertThat(tasks).isNotNull().hasSize(1);

        taskClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), USER_YODA, null);
    }

    private void finishSignalTaskProcess(Long pid) {
        List<String> signals = processClient.getAvailableSignals(CONTAINER_ID, pid);
        Assertions.assertThat(signals).isNotNull().hasSize(1);
        Assertions.assertThat(signals.get(0)).isEqualTo(SIGNAL_NAME);

        processClient.signal(CONTAINER_ID, SIGNAL_NAME, null);
    }
}
