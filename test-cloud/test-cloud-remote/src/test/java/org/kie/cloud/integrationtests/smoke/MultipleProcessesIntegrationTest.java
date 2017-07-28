package org.kie.cloud.integrationtests.smoke;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.git.GitProvider;
import org.kie.cloud.git.GitProviderFactory;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.controller.client.KieServerMgmtControllerClient;

public class MultipleProcessesIntegrationTest {

    private static final String ORGANIZATION_UNIT_NAME = "myOrgUnit";
    private static final String REPOSITORY_NAME = "myRepo";

    private static final String PROJECT_GROUP_ID = "org.kie.server.testing";
    private static final String PROJECT_NAME = "definition-project";
    private static final String PROJECT_VERSION = "1.0.0.Final";

    private static final String CONTAINER_ID = "cont-id";
    private static final String CONTAINER_ALIAS = "cont-alias";

    private static final String USERTASK_PROCESS_ID = "definition-project.usertask";
    private static final String SIGNALTASK_PROCESS_ID = "definition-project.signaltask";

    private static final String USER_YODA = "yoda";

    private static final String SIGNAL_NAME = "signal1";

    private static DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();
    private WorkbenchWithKieServerScenario workbenchWithKieServerScenario;
    private WorkbenchClientProvider workbenchClientProvider;
    private KieServerClientProvider kieServerClientProvider;
    private KieServerMgmtControllerClient kieControllerClient;
    private GitProvider gitProvider;

    @Before
    public void setUp() {
        workbenchWithKieServerScenario = deploymentScenarioFactory.getWorkbenchWithKieServerScenarioBuilder().build();
        workbenchWithKieServerScenario.deploy();

        gitProvider = GitProviderFactory.getGitProvider();
        gitProvider.createGitRepository(workbenchWithKieServerScenario.getNamespace(), ClassLoader.class.getResource("/kjars-sources").getFile());

        workbenchClientProvider = new WorkbenchClientProvider(workbenchWithKieServerScenario.getWorkbenchDeployment());
        workbenchClientProvider.createOrganizationalUnit(ORGANIZATION_UNIT_NAME, workbenchWithKieServerScenario.getWorkbenchDeployment().getUsername());
        workbenchClientProvider.cloneRepository(ORGANIZATION_UNIT_NAME, REPOSITORY_NAME, gitProvider.getRepositoryUrl(workbenchWithKieServerScenario.getNamespace()));
        workbenchClientProvider.deployProject(REPOSITORY_NAME, PROJECT_NAME);

        kieServerClientProvider = new KieServerClientProvider(workbenchWithKieServerScenario.getKieServerDeployment());
        kieControllerClient = new KieServerMgmtControllerClient(workbenchWithKieServerScenario.getWorkbenchDeployment().getUrl().toString() + "/rest/controller",
                workbenchWithKieServerScenario.getWorkbenchDeployment().getUsername(), workbenchWithKieServerScenario.getWorkbenchDeployment().getPassword());
    }

    @After
    public void tearDown() {
        InstanceLogUtil.writeDeploymentLogs(workbenchWithKieServerScenario.getWorkbenchDeployment());
        InstanceLogUtil.writeDeploymentLogs(workbenchWithKieServerScenario.getKieServerDeployment());
        workbenchWithKieServerScenario.undeploy();
        gitProvider.deleteGitRepository(workbenchWithKieServerScenario.getNamespace());
    }

    @Test
    public void testMultipleDifferentProcessesOnSameKieServer() {
        KieServerInfo serverInfo = kieServerClientProvider.getKieServerClient().getServerInfo().getResult();
        kieControllerClient.saveContainerSpec(serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, PROJECT_NAME, PROJECT_VERSION, KieContainerStatus.STARTED);

        kieServerClientProvider.waitForContainerStart(CONTAINER_ID);

        Long userTaskPid = kieServerClientProvider.getProcessClient().startProcess(CONTAINER_ID, USERTASK_PROCESS_ID);
        Assertions.assertThat(userTaskPid).isNotNull();
        Long signalTaskPid = kieServerClientProvider.getProcessClient().startProcess(CONTAINER_ID, SIGNALTASK_PROCESS_ID);
        Assertions.assertThat(signalTaskPid).isNotNull();

        finishUserTaskProcess();
        finishSignalTaskProcess(signalTaskPid);

        ProcessInstance userTaskPi = kieServerClientProvider.getProcessClient().getProcessInstance(CONTAINER_ID, userTaskPid);
        Assertions.assertThat(userTaskPi).isNotNull();
        Assertions.assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

        ProcessInstance signalTaskPi = kieServerClientProvider.getProcessClient().getProcessInstance(CONTAINER_ID, signalTaskPid);
        Assertions.assertThat(signalTaskPi).isNotNull();
        Assertions.assertThat(signalTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }

    private void finishUserTaskProcess() {
        List<TaskSummary> tasks = kieServerClientProvider.getTaskClient().findTasks(USER_YODA, 0, 10);
        Assertions.assertThat(tasks).isNotNull().hasSize(1);

        kieServerClientProvider.getTaskClient().completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), USER_YODA, null);
    }

    private void finishSignalTaskProcess(Long pid) {
        List<String> signals = kieServerClientProvider.getProcessClient().getAvailableSignals(CONTAINER_ID, pid);
        Assertions.assertThat(signals).isNotNull().hasSize(1);
        Assertions.assertThat(signals.get(0)).isEqualTo(SIGNAL_NAME);

        kieServerClientProvider.getProcessClient().signal(CONTAINER_ID, SIGNAL_NAME, null);
    }
}
