/*
 * Copyright 2018 JBoss by Red Hat.
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
package org.kie.cloud.integrationtests.sso;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentSSOScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.client.KieServerControllerClient;

public class AuthSSOIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchKieServerPersistentSSOScenario> {

    private final String SECURED_URL_PREFIX = "secured-";
    private final String RANDOM_URL_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";

    private static final String BUSINESS_CENTRAL_NAME = "rhpamcentr";
    private static final String KIE_SERVER_NAME = "kieserver";

    private static final String BUSINESS_CENTRAL_HOSTNAME = BUSINESS_CENTRAL_NAME + DeploymentConstants.getDefaultDomainSuffix();
    private static final String KIE_SERVER_HOSTNAME = KIE_SERVER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    @Override
    protected WorkbenchKieServerPersistentSSOScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchKieServerPersistentSSOScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withBusinessCentralMavenUser("mavenUser", "mavenUser1!")
                .withHttpWorkbenchHostname(RANDOM_URL_PREFIX + BUSINESS_CENTRAL_HOSTNAME)
                .withHttpsWorkbenchHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + BUSINESS_CENTRAL_HOSTNAME)
                .withHttpKieServerHostname(RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                .withHttpsKieServerHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                .build();
    }

    private KieServerControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;
    private ProcessServicesClient processClient;
    private UserTaskServicesClient taskClient;
    private QueryServicesClient queryClient;

    @Before
    public void setUp() {
        String repositoryName = gitProvider.createGitRepositoryWithPrefix(deploymentScenario.getWorkbenchDeployment().getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEFINITION_PROJECT_NAME).getFile());

        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(repositoryName), deploymentScenario.getWorkbenchDeployment(), DEFINITION_PROJECT_NAME);

        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());

        processClient = kieServerClient.getServicesClient(ProcessServicesClient.class);
        taskClient = kieServerClient.getServicesClient(UserTaskServicesClient.class);
        queryClient = kieServerClient.getServicesClient(QueryServicesClient.class);
    }

    @Test
    public void testWorkbenchControllerPersistence() {
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        String kieServerLocation = serverInfo.getLocation();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);

        verifyOneServerTemplateWithContainer(kieServerLocation, CONTAINER_ID);

        scaleToZeroAndBackToOne(deploymentScenario.getWorkbenchDeployment());

        verifyOneServerTemplateWithContainer(kieServerLocation, CONTAINER_ID);
    }

    @Test
    @Ignore("Ignored as the tests are affected by RHPAM-1361. Unignore when the JIRA will be fixed. https://issues.jboss.org/browse/RHPAM-1361")
    public void testMultipleDifferentProcessesOnSameKieServer() {
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);

        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        Long userTaskPid = processClient.startProcess(CONTAINER_ID, Constants.ProcessId.USERTASK);
        assertThat(userTaskPid).isNotNull();
        Long signalTaskPid = processClient.startProcess(CONTAINER_ID, Constants.ProcessId.SIGNALTASK);
        assertThat(signalTaskPid).isNotNull();

        finishUserTaskProcess();
        finishSignalTaskProcess(signalTaskPid);

        ProcessInstance userTaskPi = processClient.getProcessInstance(CONTAINER_ID, userTaskPid);
        assertThat(userTaskPi).isNotNull();
        assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

        ProcessInstance signalTaskPi = processClient.getProcessInstance(CONTAINER_ID, signalTaskPid);
        assertThat(signalTaskPi).isNotNull();
        assertThat(signalTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }

    private void finishUserTaskProcess() {
        List<TaskSummary> tasks = taskClient.findTasks(Constants.User.YODA, 0, 10);
        assertThat(tasks).hasSize(1);

        taskClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), Constants.User.YODA, null);
    }

    private void finishSignalTaskProcess(Long pid) {
        List<String> signals = processClient.getAvailableSignals(CONTAINER_ID, pid);
        assertThat(signals)
                .hasSize(1)
                .contains(Constants.Signal.SIGNAL_NAME, atIndex(0));

        processClient.signal(CONTAINER_ID, Constants.Signal.SIGNAL_NAME, null);
    }

    private void verifyOneServerTemplateWithContainer(String kieServerLocation, String containerId) {
        ServerTemplateList serverTemplates = kieControllerClient.listServerTemplates();
        assertThat(serverTemplates.getServerTemplates()).as("Number of server templates differ.").hasSize(1);

        ServerTemplate serverTemplate = serverTemplates.getServerTemplates()[0];
        assertThat(serverTemplate.getServerInstanceKeys()).hasSize(1);
        assertThat(serverTemplate.getServerInstanceKeys().iterator().next().getUrl()).isEqualTo(kieServerLocation);
        assertThat(serverTemplate.getContainersSpec()).hasSize(1);
        assertThat(serverTemplate.getContainersSpec().iterator().next().getId()).isEqualTo(containerId);
    }

    private void scaleToZeroAndBackToOne(Deployment deployment) {
        deployment.scale(0);
        deployment.waitForScale();
        deployment.scale(1);
        deployment.waitForScale();
    }
}
