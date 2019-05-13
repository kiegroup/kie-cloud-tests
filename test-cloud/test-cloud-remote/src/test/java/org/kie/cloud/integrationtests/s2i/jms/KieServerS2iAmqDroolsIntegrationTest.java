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
package org.kie.cloud.integrationtests.s2i.jms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.AmqDeployment;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.KieServerS2IAmqSettingsBuilder;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.category.ApbNotSupported;
import org.kie.cloud.integrationtests.category.Baseline;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.provider.git.Git;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.integrationtests.shared.KieServerReflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
@Category({Baseline.class, ApbNotSupported.class}) // Because DroolsServerFilterClasses not supported yet
public class KieServerS2iAmqDroolsIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<GenericScenario> {

    private static final Logger logger = LoggerFactory.getLogger(KieServerS2iAmqDroolsIntegrationTest.class);

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public KieServerS2IAmqSettingsBuilder kieServerS2IAmqSettingsBuilder;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            KieServerS2IAmqSettingsBuilder kieServerS2IAmqSettings = deploymentScenarioFactory.getKieServerS2IAmqSettingsBuilder();
            scenarios.add(new Object[] { "KIE Server S2I AMQ", kieServerS2IAmqSettings });
        } catch (UnsupportedOperationException ex) {
            logger.info("KIE Server AMQ S2I is skipped.", ex);
        }

        return scenarios;
    }

    private KieServicesClient kieServicesClient;
    private RuleServicesClient ruleClient;

    private static final String KIE_SESSION = "kbase1.stateless";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_NAME = "Darth";
    private static final String PERSON_SURNAME_FIELD = "surname";
    private static final String PERSON_EXPECTED_SURNAME = "Vader";
    private static final String PERSON_OUT_IDENTIFIER = "person1";

    private static final Kjar DEPLOYED_KJAR = Kjar.STATELESS_SESSION;
    private static final ReleaseId RELEASE_ID = new ReleaseId(DEPLOYED_KJAR.getGroupId(), DEPLOYED_KJAR.getName(), DEPLOYED_KJAR.getVersion());
    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + DEPLOYED_KJAR.toString();

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    private String repositoryName;
    private ClassLoader kjarClassLoader;
    private KieCommands commandsFactory = KieServices.Factory.get().getCommands();
    private Set<Class<?>> extraClasses;

    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        repositoryName = Git.getProvider().createGitRepositoryWithPrefix("KieServerS2iDroolsRepository", KieServerS2iAmqDroolsIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

        DeploymentSettings kieServerS2Isettings = kieServerS2IAmqSettingsBuilder
                .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                .withSourceLocation(Git.getProvider().getRepositoryUrl(repositoryName), REPO_BRANCH, DEPLOYED_KJAR.getName())
                .withDroolsServerFilterClasses(false)
                .build();

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withKieServer(kieServerS2Isettings)
                .build();
    }

    @BeforeClass
    public static void buildKjar() {
        MavenDeployer.buildAndInstallMavenProject(KieServerS2iAmqDroolsIntegrationTest.class.getResource("/kjars-sources/stateless-session").getFile());
    }

    @Before
    public void setUp() throws ClassNotFoundException {
        AmqDeployment amqDeployment = deploymentScenario.getDeployments().stream()
                .filter(AmqDeployment.class::isInstance)
                .map(AmqDeployment.class::cast)
                .findFirst()
                .orElseThrow(()->new RuntimeException("No AMQ deployment founded."));

        kjarClassLoader = KieServices.Factory.get().newKieContainer(RELEASE_ID).getClassLoader();
        extraClasses = Collections.singleton(Class.forName(PERSON_CLASS_NAME, true, kjarClassLoader));

        kieServicesClient = KieServerClientProvider.getKieServerJmsClient(amqDeployment.getTcpUrl(), extraClasses);
        ruleClient = KieServerClientProvider.getRuleJmsClient(kieServicesClient);
    }

    @After
    public void deleteRepo() {
        Git.getProvider().deleteGitRepository(repositoryName);
    }

    @Test
    public void testContainerAfterExecServerS2IStart() {
        List<KieContainerResource> containers = kieServicesClient.listContainers().getResult().getContainers();
        assertThat(containers).hasSize(1);

        KieContainerResource container = containers.get(0);
        assertThat(container).isNotNull();
        assertThat(container.getContainerId()).isEqualTo(CONTAINER_ID);

        ReleaseId containerReleaseId = container.getResolvedReleaseId();
        assertThat(containerReleaseId).isNotNull();
        assertThat(containerReleaseId.getGroupId()).isEqualTo(DEPLOYED_KJAR.getGroupId());
        assertThat(containerReleaseId.getArtifactId()).isEqualTo(DEPLOYED_KJAR.getName());
        assertThat(containerReleaseId.getVersion()).isEqualTo(DEPLOYED_KJAR.getVersion());

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        Object person = KieServerReflections.createInstance(PERSON_CLASS_NAME, extraClasses.iterator().next().getClassLoader(), PERSON_NAME, "");
        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertThat(reply.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        ExecutionResults actualData = reply.getResult();
        Object value = actualData.getValue(PERSON_OUT_IDENTIFIER);

        assertThat(KieServerReflections.valueOf(value, PERSON_SURNAME_FIELD)).as("Expected surname to be set to 'Vader'").isEqualTo(PERSON_EXPECTED_SURNAME);
    }
}
