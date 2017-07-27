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

package org.kie.cloud.integrationtests.smoke;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;

public class ExternalMavenRepoIntegrationTest {

    private static final String CONTAINER_ID = "cont-id";

    private static final String PROJECT_GROUP_ID = "org.kie.server.testing";
    private static final String PROJECT_NAME = "definition-project-snapshot";
    private static final String PROJECT_VERSION = "1.0.0-SNAPSHOT";

    private static final String USERTASK_PROCESS_ID = "definition-project.usertask";

    private static final String USER_YODA = "yoda";

    private static DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactory.getInstance();
    private WorkbenchWithKieServerScenario workbenchWithKieServerScenario;
    private KieServerClientProvider kieServerClientProvider;

    @Before
    public void setUp() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());

        workbenchWithKieServerScenario = deploymentScenarioFactory.getWorkbenchWithKieServerScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
        workbenchWithKieServerScenario.deploy();

        kieServerClientProvider = new KieServerClientProvider(workbenchWithKieServerScenario.getKieServerDeployment());
    }

    @After
    public void tearDown() {
        InstanceLogUtil.writeDeploymentLogs(workbenchWithKieServerScenario.getWorkbenchDeployment());
        InstanceLogUtil.writeDeploymentLogs(workbenchWithKieServerScenario.getKieServerDeployment());
        workbenchWithKieServerScenario.undeploy();
    }

    @Test
    public void testProcessFromExternalMavenRepo() {
        kieServerClientProvider.getKieServerClient().createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, new ReleaseId(PROJECT_GROUP_ID, PROJECT_NAME, PROJECT_VERSION)));

        Long userTaskPid = kieServerClientProvider.getProcessClient().startProcess(CONTAINER_ID, USERTASK_PROCESS_ID);
        Assertions.assertThat(userTaskPid).isNotNull();

        List<TaskSummary> tasks = kieServerClientProvider.getTaskClient().findTasks(USER_YODA, 0, 10);
        Assertions.assertThat(tasks).isNotNull().hasSize(1);

        kieServerClientProvider.getTaskClient().completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), USER_YODA, null);

        ProcessInstance userTaskPi = kieServerClientProvider.getProcessClient().getProcessInstance(CONTAINER_ID, userTaskPid);
        Assertions.assertThat(userTaskPi).isNotNull();
        Assertions.assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }
}
