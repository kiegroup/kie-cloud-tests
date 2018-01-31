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
package org.kie.cloud.integrationtests.s2i;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.SolverServicesClient;

@RunWith(Parameterized.class)
public class KieServerS2iOptaplannerIntegrationTest extends AbstractCloudIntegrationTest<GenericScenario> {

    @Parameter
    public KieServerS2ISettingsBuilder kieServerS2ISettingsBuilder;

    private static final String CLOUD_BALANCE_SOLVER_ID = "cloudsolver";
    private static final String CLOUD_BALANCE_SOLVER_CONFIG = "cloudbalance-solver.xml";

    private static final String CLASS_CLOUD_BALANCE = "org.kie.server.testing.CloudBalance";
    private static final String CLASS_CLOUD_COMPUTER = "org.kie.server.testing.CloudComputer";
    private static final String CLASS_CLOUD_PROCESS = "org.kie.server.testing.CloudProcess";
    private static final String CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE =
            "org.kie.server.testing.AddComputerProblemFactChange";
    private static final String CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE =
            "org.kie.server.testing.DeleteComputerProblemFactChange";
    private static final String CLASS_CLOUD_GENERATOR = "org.kie.server.testing.CloudBalancingGenerator";

    private static final Kjar DEPLOYED_KJAR = Kjar.CLOUD_BALANCE_SNAPSHOT;
    private static final ReleaseId CLOUD_BALANCE_RELEASE_ID = new ReleaseId(DEPLOYED_KJAR.getGroupId(), DEPLOYED_KJAR.getName(), DEPLOYED_KJAR.getVersion());
    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + DEPLOYED_KJAR.toString();

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    private String repositoryName;
    private KieContainer kieContainer;
    private SolverServicesClient solverClient;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        KieServerS2ISettingsBuilder kieServerHttpsS2ISettings = deploymentScenarioFactory.getKieServerHttpsS2ISettingsBuilder();
        KieServerS2ISettingsBuilder kieServerBasicS2ISettings = deploymentScenarioFactory.getKieServerBasicS2ISettingsBuilder();

        return Arrays.asList(new Object[][]{
            {kieServerHttpsS2ISettings}, {kieServerBasicS2ISettings}
        });
    }

    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        repositoryName = gitProvider.createGitRepositoryWithPrefix("KieServerS2iOptaplannerRepository", ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

        DeploymentSettings kieServerS2Isettings = kieServerS2ISettingsBuilder
                .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                .withSourceLocation(gitProvider.getRepositoryUrl(repositoryName), REPO_BRANCH, DEPLOYED_KJAR.getName())
                .build();

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withKieServer(kieServerS2Isettings)
                .build();
    }

    @Before
    public void setUp() throws ClassNotFoundException {
        kieContainer = KieServices.Factory.get().newKieContainer(CLOUD_BALANCE_RELEASE_ID);

        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(
                deploymentScenario.getKieServerDeployments().get(0),
                extraClasses(kieContainer));
        solverClient = kieServerClient.getServicesClient(SolverServicesClient.class);
    }

    @After
    public void deleteRepo() {
        gitProvider.deleteGitRepository(repositoryName);
    }

    @Test
    public void testExecuteSolver() throws Exception {
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID, CLOUD_BALANCE_SOLVER_CONFIG);
        assertThat(solverInstance).isNotNull();

        Object planningProblem = loadPlanningProblem(kieContainer, 5, 15);
        solverClient.solvePlanningProblem(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID, planningProblem);

        solverInstance = solverClient.getSolver(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID); // wait for 15 seconds
        for (int i = 0; i < 5 && solverInstance.getStatus() == SolverInstance.SolverStatus.SOLVING; i++) {
            Thread.sleep(3000);
            solverInstance = solverClient.getSolver(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID);
        }

        assertThat(solverInstance.getStatus()).isEqualTo(SolverInstance.SolverStatus.NOT_SOLVING);
        assertThat(solverInstance.getScoreWrapper()).isNotNull();
        assertThat(solverInstance.getScoreWrapper().getScoreString()).isNotEmpty();
    }

    private Set<Class<?>> extraClasses(KieContainer kieContainer) throws ClassNotFoundException {
        Set<Class<?>> extra = new HashSet<>();

        extra.add(Class.forName(CLASS_CLOUD_BALANCE, true, kieContainer.getClassLoader()));
        extra.add(Class.forName(CLASS_CLOUD_COMPUTER, true, kieContainer.getClassLoader()));
        extra.add(Class.forName(CLASS_CLOUD_PROCESS, true, kieContainer.getClassLoader()));
        extra.add(Class.forName(CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE, true, kieContainer.getClassLoader()));
        extra.add(Class.forName(CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE, true,
                kieContainer.getClassLoader()));

        return extra;
    }

    private Object loadPlanningProblem(KieContainer kieContainer, int computerListSize,
            int processListSize) throws NoSuchMethodException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, InvocationTargetException {
        Class<?> cbgc = kieContainer.getClassLoader().loadClass(CLASS_CLOUD_GENERATOR);
        Object cbgi = cbgc.newInstance();

        Method method = cbgc.getMethod("createCloudBalance",
                int.class,
                int.class);
        return method.invoke(cbgi,
                computerListSize,
                processListSize);
    }
}
