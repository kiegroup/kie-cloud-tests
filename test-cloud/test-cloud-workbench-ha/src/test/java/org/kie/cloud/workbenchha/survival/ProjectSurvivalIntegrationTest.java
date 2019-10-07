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

package org.kie.cloud.workbenchha.survival;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerPersistentScenario;
import org.kie.cloud.runners.ProjectRunner;
import org.kie.cloud.runners.provider.ProjectRunnerProvider;
import org.kie.cloud.workbenchha.AbstractWorkbenchHaIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore("Survival scenarios are not supported yet.")
public class ProjectSurvivalIntegrationTest extends AbstractWorkbenchHaIntegrationTest {

    private static ClusteredWorkbenchKieServerPersistentScenario deploymentScenario;

    private static final String SPACE_NAME = "test-space";

    @Before
    public void setUp() {
        defaultWorkbenchClient.createSpace(SPACE_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());
    }

    @After
    public void cleanUp(){
        defaultWorkbenchClient.deleteSpace(SPACE_NAME);
    }

    @Test
    public void testCreateAndDeleteProjects() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<ProjectRunner> runners = ProjectRunnerProvider.getAllRunners(deploymentScenario.getWorkbenchDeployment());

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        //Create task to create projects for all users
        List<Callable<Collection<String>>> createTasks = runners.stream().map(runner -> runner.createProjectsWithDelays(SPACE_NAME,UUID.randomUUID().toString().substring(0, 6), 1, 5)).collect(Collectors.toList());
        List<Future<Collection<String>>> futures = executorService.invokeAll(createTasks);

        List<Instance> allPods = deploymentScenario.getWorkbenchDeployment().getInstances();
        deploymentScenario.getWorkbenchDeployment().deleteInstances(allPods);

        List<String> expectedList = getAllStringFromFutures(futures);

        //Check that all projects where created
        checkProjectsWereCreated(SPACE_NAME, expectedList, runners.size(), 5);
        
        //DELETE ALL

        //Create tasks to delete projects
        List<Callable<Void>> deleteTasks = runners.stream().map(pr -> pr.deleteProjects(SPACE_NAME)).collect(Collectors.toList());

        //Execute task and wait for all threads to finished
        List<Future<Void>> deleteFutures = executorService.invokeAll(deleteTasks);
        
        allPods = deploymentScenario.getWorkbenchDeployment().getInstances();
        deploymentScenario.getWorkbenchDeployment().deleteInstances(allPods);

        getAllDeleteDone(deleteFutures);

        //Check all projects was deleted
        assertThat(defaultWorkbenchClient.getProjects(SPACE_NAME)).isNotNull().isEmpty();
    }
}
