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
package org.kie.cloud.integrationtests.sso.s2i;

import java.util.Arrays;
import java.util.Collection;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;

@RunWith(Parameterized.class)
public class KieServerS2iWithSsoJbpmIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<GenericScenario> {

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public KieServerS2ISettingsBuilder kieServerS2ISettingsBuilder;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        KieServerS2ISettingsBuilder kieServerHttpsS2ISettings = deploymentScenarioFactory.getKieServerHttpsS2ISettingsBuilder();

        return Arrays.asList(new Object[][]{
            {"KIE Server HTTPS S2I", kieServerHttpsS2ISettings}
        });
    }

    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected UserTaskServicesClient taskServicesClient;

    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + Kjar.DEFINITION.toString();

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    private String repositoryName;

    private static final String SECURED_URL_PREFIX = "secured-";
    private static final String RANDOM_URL_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";
    private static final String KIE_SERVER_NAME = "kieserver-sso-jbpm";
    private static final String KIE_SERVER_HOSTNAME = KIE_SERVER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        repositoryName = gitProvider.createGitRepositoryWithPrefix("KieServerS2iJbpmRepository", ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

        DeploymentSettings kieServerS2Isettings = kieServerS2ISettingsBuilder
                .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                .withSourceLocation(gitProvider.getRepositoryUrl(repositoryName), REPO_BRANCH, DEFINITION_PROJECT_NAME)
                .withHostame(RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                .withSecuredHostame(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                .build();

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withKieServer(kieServerS2Isettings)
                .withSso()
                .build();
    }

    @Before
    public void setUp() {
        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(0));
        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployments().get(0));
        taskServicesClient = KieServerClientProvider.getTaskClient(deploymentScenario.getKieServerDeployments().get(0));
    }

    @After
    public void deleteRepo() {
        gitProvider.deleteGitRepository(repositoryName);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testContainerAfterExecServerS2IStart() {
        List<KieContainerResource> containers = kieServicesClient.listContainers().getResult().getContainers();
        assertThat(containers).isNotNull().hasSize(1);

        KieContainerResource container = containers.get(0);
        assertThat(container).isNotNull();
        assertThat(container.getContainerId()).isNotNull().isEqualTo(CONTAINER_ID);

        ReleaseId containerReleaseId = container.getResolvedReleaseId();
        assertThat(containerReleaseId).isNotNull();
        assertThat(containerReleaseId.getGroupId()).isNotNull().isEqualTo(PROJECT_GROUP_ID);
        assertThat(containerReleaseId.getArtifactId()).isNotNull().isEqualTo(DEFINITION_PROJECT_NAME);
        assertThat(containerReleaseId.getVersion()).isNotNull().isEqualTo(DEFINITION_PROJECT_VERSION);

        Long processId = processServicesClient.startProcess(CONTAINER_ID, Constants.ProcessId.USERTASK);
        assertThat(processId).isNotNull();

        List<TaskSummary> tasks = taskServicesClient.findTasks(Constants.User.YODA, 0, 10);
        assertThat(tasks).isNotNull().hasSize(1);

        taskServicesClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), Constants.User.YODA, null);

        ProcessInstance userTaskPi = processServicesClient.getProcessInstance(CONTAINER_ID, processId);
        assertThat(userTaskPi).isNotNull();
        assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }

}
