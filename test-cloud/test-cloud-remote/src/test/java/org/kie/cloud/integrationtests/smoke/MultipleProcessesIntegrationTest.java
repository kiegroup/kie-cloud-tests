package org.kie.cloud.integrationtests.smoke;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.integrationtests.util.Constants;
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
public class MultipleProcessesIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<WorkbenchKieServerPersistentScenario> {

    private KieServerControllerClient kieControllerClient;

    private String repositoryName;

    private KieServicesClient kieServerClient;
    private ProcessServicesClient processClient;
    private UserTaskServicesClient taskClient;

    @Override
    protected WorkbenchKieServerPersistentScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder().build();
    }

    @Before
    public void setUp() {
        repositoryName = gitProvider.createGitRepositoryWithPrefix(deploymentScenario.getWorkbenchDeployment().getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEFINITION_PROJECT_NAME).getFile());

        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(repositoryName), deploymentScenario.getWorkbenchDeployment(), DEFINITION_PROJECT_NAME);

        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        processClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
        taskClient = KieServerClientProvider.getTaskClient(deploymentScenario.getKieServerDeployment());
    }

    @After
    public void tearDown() {
        gitProvider.deleteGitRepository(repositoryName);
    }

    @Test
    public void testMultipleDifferentProcessesOnSameKieServer() {
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);

        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        Long userTaskPid = processClient.startProcess(CONTAINER_ID, Constants.ProcessId.USERTASK);
        Assertions.assertThat(userTaskPid).isNotNull();
        Long signalTaskPid = processClient.startProcess(CONTAINER_ID, Constants.ProcessId.SIGNALTASK);
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
        List<TaskSummary> tasks = taskClient.findTasks(Constants.User.YODA, 0, 10);
        Assertions.assertThat(tasks).isNotNull().hasSize(1);

        taskClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), Constants.User.YODA, null);
    }

    private void finishSignalTaskProcess(Long pid) {
        List<String> signals = processClient.getAvailableSignals(CONTAINER_ID, pid);
        Assertions.assertThat(signals).isNotNull().hasSize(1);
        Assertions.assertThat(signals.get(0)).isEqualTo(Constants.Signal.SIGNAL_NAME);

        processClient.signal(CONTAINER_ID, Constants.Signal.SIGNAL_NAME, null);
    }
}
