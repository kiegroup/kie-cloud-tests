/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.integrationtests.testproviders;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.KjarDeployer;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.time.Constants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

public class ProcessTestProvider {

    private static final int TASKS_PAGE_SIZE = 10000;

    private ProcessTestProvider() {}

    /**
     * Create provider instance
     * 
     * @return provider instance
     */
    public static ProcessTestProvider create() {
        return create(null);
    }

    /**
     * Create provider instance and init it with given environment
     * 
     * @param environment if not null, initialize this provider with the environment
     * 
     * @return provider instance
     */
    public static ProcessTestProvider create(Map<String, String> environment) {
        ProcessTestProvider provider = new ProcessTestProvider();
        if (Objects.nonNull(environment)) {
            provider.init(environment);
        }
        return provider;
    }

    private void init(Map<String, String> environment) {
        KjarDeployer.create(Kjar.DEFINITION_SNAPSHOT).deploy(environment);
    }

    public void testDeployFromKieServerAndExecuteProcesses(KieServerDeployment kieServerDeployment) {
        testDeployFromKieServerAndExecuteProcessWithUserTask(kieServerDeployment);
        testDeployFromKieServerAndExecuteProcessWithSignal(kieServerDeployment);
    }

    public void testExecuteProcesses(KieServerDeployment kieServerDeployment, String containerId) {
        ProcessServicesClient processClient = KieServerClientProvider.getProcessClient(kieServerDeployment);
        UserTaskServicesClient taskClient = KieServerClientProvider.getTaskClient(kieServerDeployment);

        testExecuteProcessWithUserTask(processClient, taskClient, containerId);
        testExecuteProcessWithSignal(processClient, containerId);
    }

    public void testExecuteProcesses(SmartRouterDeployment smartRouterDeployment, KieServerDeployment kieServerDeployment, String containerId) {
        KieServicesClient smartRouterClient = KieServerClientProvider.getSmartRouterClient(smartRouterDeployment, kieServerDeployment.getUsername(), kieServerDeployment.getPassword());
        ProcessServicesClient processClient = smartRouterClient.getServicesClient(ProcessServicesClient.class);
        UserTaskServicesClient taskClient = smartRouterClient.getServicesClient(UserTaskServicesClient.class);

        testExecuteProcessWithUserTask(processClient, taskClient, containerId);
        testExecuteProcessWithSignal(processClient, containerId);
    }

    private void testDeployFromKieServerAndExecuteProcessWithUserTask(KieServerDeployment kieServerDeployment) {
        String containerId = "testProcessWithUserTask";
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);

        ServiceResponse<KieContainerResource> createContainer = kieServerClient.createContainer(containerId, new KieContainerResource(containerId, new ReleaseId(Kjar.DEFINITION_SNAPSHOT.getGroupId(),
                                                                                                                                                                 Kjar.DEFINITION_SNAPSHOT.getArtifactName(),
                                                                                                                                                                 Kjar.DEFINITION_SNAPSHOT.getVersion())));
        KieServerAssert.assertSuccess(createContainer);
        kieServerDeployment.waitForContainerRespin();

        ProcessServicesClient processClient = KieServerClientProvider.getProcessClient(kieServerDeployment);
        UserTaskServicesClient taskClient = KieServerClientProvider.getTaskClient(kieServerDeployment);
        try {
            testExecuteProcessWithUserTask(processClient, taskClient, containerId);
        } finally {
            kieServerClient.disposeContainer(containerId);
            kieServerDeployment.waitForContainerRespin();
        }
    }

    private void testExecuteProcessWithUserTask(ProcessServicesClient processClient, UserTaskServicesClient taskClient, String containerId) {
        int tasksCount = taskClient.findTasks(Constants.User.YODA, 0, TASKS_PAGE_SIZE).size();

        Long userTaskPid = processClient.startProcess(containerId, Constants.ProcessId.USERTASK);
        assertThat(userTaskPid).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasks(Constants.User.YODA, 0, TASKS_PAGE_SIZE);
        assertThat(tasks).hasSize(tasksCount + 1);

        taskClient.completeAutoProgress(containerId, tasks.get(0).getId(), Constants.User.YODA, null);

        ProcessInstance userTaskPi = processClient.getProcessInstance(containerId, userTaskPid);
        assertThat(userTaskPi).isNotNull();
        assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }

    private void testDeployFromKieServerAndExecuteProcessWithSignal(KieServerDeployment kieServerDeployment) {
        String containerId = "testProcessWithSignal";
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);
        ProcessServicesClient processClient = KieServerClientProvider.getProcessClient(kieServerDeployment);

        ServiceResponse<KieContainerResource> createContainer = kieServerClient.createContainer(containerId, new KieContainerResource(containerId, new ReleaseId(Kjar.DEFINITION_SNAPSHOT.getGroupId(),
                                                                                                                                                                 Kjar.DEFINITION_SNAPSHOT.getArtifactName(),
                                                                                                                                                                 Kjar.DEFINITION_SNAPSHOT.getVersion())));
        KieServerAssert.assertSuccess(createContainer);
        kieServerDeployment.waitForContainerRespin();

        try {
            testExecuteProcessWithSignal(processClient, containerId);
        } finally {
            kieServerClient.disposeContainer(containerId);
            kieServerDeployment.waitForContainerRespin();
        }
    }

    private void testExecuteProcessWithSignal(ProcessServicesClient processClient, String containerId) {
        Long signalTaskPid = processClient.startProcess(containerId, Constants.ProcessId.SIGNALTASK);
        assertThat(signalTaskPid).isNotNull();

        List<String> signals = processClient.getAvailableSignals(containerId, signalTaskPid);
        assertThat(signals).hasSize(1).contains(Constants.Signal.SIGNAL_NAME, atIndex(0));

        processClient.signalProcessInstance(containerId, signalTaskPid, Constants.Signal.SIGNAL_NAME, null);

        ProcessInstance signalTaskPi = processClient.getProcessInstance(containerId, signalTaskPid);
        assertThat(signalTaskPi).isNotNull();
        assertThat(signalTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }
}
