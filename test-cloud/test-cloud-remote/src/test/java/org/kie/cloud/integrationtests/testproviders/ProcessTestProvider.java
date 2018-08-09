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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import java.util.List;

import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;

public class ProcessTestProvider {

    static {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());
    }

    public static void testProcess(KieServerDeployment kieServerDeployment) {
        String containerId = "testProcess";
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);

        ServiceResponse<KieContainerResource> createContainer = kieServerClient.createContainer(containerId, new KieContainerResource(containerId, new ReleaseId(Kjar.DEFINITION_SNAPSHOT.getGroupId(), Kjar.DEFINITION_SNAPSHOT.getName(), Kjar.DEFINITION_SNAPSHOT.getVersion())));
        KieServerAssert.assertSuccess(createContainer);

        ProcessServicesClient processClient = KieServerClientProvider.getProcessClient(kieServerDeployment);
        UserTaskServicesClient taskClient = KieServerClientProvider.getTaskClient(kieServerDeployment);

        try {
            Long userTaskPid = processClient.startProcess(containerId, Constants.ProcessId.USERTASK);
            assertThat(userTaskPid).isNotNull();

            List<TaskSummary> tasks = taskClient.findTasks(Constants.User.YODA, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            taskClient.completeAutoProgress(containerId, tasks.get(0).getId(), Constants.User.YODA, null);

            ProcessInstance userTaskPi = processClient.getProcessInstance(containerId, userTaskPid);
            assertThat(userTaskPi).isNotNull();
            assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
        } finally {
            kieServerClient.disposeContainer(containerId);
        }
    }

    public static void testMultipleDifferentProcesses(KieServerDeployment kieServerDeployment) {
        String containerId = "testMultipleDifferentProcesses";
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);

        ProcessServicesClient processClient = kieServerClient.getServicesClient(ProcessServicesClient.class);
        UserTaskServicesClient taskClient = kieServerClient.getServicesClient(UserTaskServicesClient.class);

        ServiceResponse<KieContainerResource> createContainer = kieServerClient.createContainer(containerId, new KieContainerResource(containerId, new ReleaseId(Kjar.DEFINITION_SNAPSHOT.getGroupId(), Kjar.DEFINITION_SNAPSHOT.getName(), Kjar.DEFINITION_SNAPSHOT.getVersion())));
        KieServerAssert.assertSuccess(createContainer);

        Long userTaskPid = processClient.startProcess(containerId, Constants.ProcessId.USERTASK);
        assertThat(userTaskPid).isNotNull();
        Long signalTaskPid = processClient.startProcess(containerId, Constants.ProcessId.SIGNALTASK);
        assertThat(signalTaskPid).isNotNull();

        finishUserTaskProcess(taskClient, containerId);
        finishSignalTaskProcess(processClient, containerId, signalTaskPid);

        ProcessInstance userTaskPi = processClient.getProcessInstance(containerId, userTaskPid);
        assertThat(userTaskPi).isNotNull();
        assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

        ProcessInstance signalTaskPi = processClient.getProcessInstance(containerId, signalTaskPid);
        assertThat(signalTaskPi).isNotNull();
        assertThat(signalTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }

    private static void finishUserTaskProcess(UserTaskServicesClient taskClient, String containerId) {
        List<TaskSummary> tasks = taskClient.findTasks(Constants.User.YODA, 0, 10);
        assertThat(tasks).hasSize(1);

        taskClient.completeAutoProgress(containerId, tasks.get(0).getId(), Constants.User.YODA, null);
    }

    private static void finishSignalTaskProcess(ProcessServicesClient processClient, String containerId, Long pid) {
        List<String> signals = processClient.getAvailableSignals(containerId, pid);
        assertThat(signals)
                .hasSize(1)
                .contains(Constants.Signal.SIGNAL_NAME, atIndex(0));

        processClient.signal(containerId, Constants.Signal.SIGNAL_NAME, null);
    }
}
