/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.workbenchha.load;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.guvnor.rest.client.CloneProjectRequest;
import org.guvnor.rest.client.ProjectResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.util.Users;
import org.kie.cloud.workbenchha.runners.CompileRunner;
import org.kie.wb.test.rest.client.WorkbenchClient;

import static org.assertj.core.api.Assertions.assertThat;

public class CompileProjectLoadIntegrationTest extends AbstractCloudIntegrationTest {

    private static ClusteredWorkbenchKieServerPersistentScenario deploymentScenario;

    private static final String SPACE_NAME = "test-space";
    private static final String PROJECT_NAME = "test-project";

    private WorkbenchClient defaultWorkbenchClient;

    @BeforeClass
    public static void initializeDeployment() {
        try {
            deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerPersistentScenarioBuilder()
                    .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(),
                            MavenConstants.getMavenRepoPassword())
                    .deploySso()
                    .build();
        } catch (UnsupportedOperationException ex) {
            Assume.assumeFalse(ex.getMessage().startsWith("Not supported"));
        }
        deploymentScenario.setLogFolderName(CompileProjectLoadIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        
        Map<String, String> users = Stream.of(Users.class.getEnumConstants()).collect(Collectors.toMap(Users::getName, Users::getPassword));
        SsoDeployer.createUsers(deploymentScenario.getSsoDeployment(), users);
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Before
    public void setUp() {
        defaultWorkbenchClient = WorkbenchClientProvider.getWorkbenchClient(deploymentScenario.getWorkbenchDeployment());
        defaultWorkbenchClient.createSpace(SPACE_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());
        // TODO import project
        final CloneProjectRequest cloneProjectRequest = new CloneProjectRequest();
        cloneProjectRequest.setName(PROJECT_NAME);
        cloneProjectRequest.setGitURL(""); // TODO add some resource
        defaultWorkbenchClient.cloneRepository(SPACE_NAME, cloneProjectRequest);
    }

    @After
    public void cleanUp(){
        defaultWorkbenchClient.deleteSpace(SPACE_NAME);
    }

    @Test
    public void testImportProjects() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<CompileRunner> runners = new ArrayList<>();
        runners.add(new CompileRunner(deploymentScenario.getWorkbenchDeployment(), Users.JOHN.getName(), Users.JOHN.getPassword()));
        //... TODO

        // TODO need to be updated - not finished !!!

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        //Create task to create projects for all users
        List<Callable<Void>> createTasks = runners.stream().map(runner -> runner.compileProjects(SPACE_NAME,Arrays.asList(PROJECT_NAME))).collect(Collectors.toList());
        List<Future<Void>> futures = executorService.invokeAll(createTasks);

        List<String> expectedList = new ArrayList<>();
        //Wait to all threads finish and save all created projects names
        futures.forEach(future -> {
            try {
            future.get();
            } catch (InterruptedException | ExecutionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        //Check that all projects where created
        assertThat(expectedList).isNotEmpty().hasSize(runners.size() * 5);        
        Collection<ProjectResponse> projects = defaultWorkbenchClient.getProjects(SPACE_NAME);
        assertThat(projects).isNotNull();
        List<String> resultList = projects.stream().collect(Collectors.mapping(ProjectResponse::getName, Collectors.toList()));
        assertThat(resultList).containsExactlyInAnyOrder(resultList.stream().toArray(String[]::new));

    }
}
