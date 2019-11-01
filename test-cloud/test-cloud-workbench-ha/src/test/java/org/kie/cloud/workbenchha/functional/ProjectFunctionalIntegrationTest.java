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

package org.kie.cloud.workbenchha.functional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.guvnor.rest.client.ProjectResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.runners.ProjectRunner;
import org.kie.cloud.runners.provider.ProjectRunnerProvider;
import org.kie.cloud.workbenchha.AbstractWorkbenchHaIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectFunctionalIntegrationTest extends AbstractWorkbenchHaIntegrationTest {

    private static final String SPACE_NAME = "test-space";
    //private static final int PROJECT_PER_USER = 5;

    @Before
    public void createTestingSpace() {
        defaultWorkbenchClient.createSpace(SPACE_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());
    }

    @After
    public void deleteTestingSpace(){
        defaultWorkbenchClient.deleteSpace(SPACE_NAME);
    }

    @Test
    public void testCreateAndDeleteProject() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<ProjectRunner> runners = ProjectRunnerProvider.getAllRunners(deploymentScenario.getWorkbenchDeployment());

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());

        //Create task to create projects for all users
        List<Callable<Collection<String>>> createTasks = runners.stream().map(runner -> runner.createProject(SPACE_NAME, UUID.randomUUID().toString().substring(0, 6))).collect(Collectors.toList());
        List<Future<Collection<String>>> futures = executorService.invokeAll(createTasks);

        List<String> expectedList = getAllStringFromFutures(futures);

        //Check that all projects where created
        assertThat(expectedList).isNotEmpty().hasSize(runners.size());
        checkProjectsWereCreated(SPACE_NAME, expectedList);

        //GET ALL

        List<Callable<Collection<ProjectResponse>>> getAllProjects = runners.stream().map(pr -> pr.getProjects(SPACE_NAME)).collect(Collectors.toList());
        List<Future<Collection<ProjectResponse>>> futuresProjects = executorService.invokeAll(getAllProjects);
        futuresProjects.forEach(futureProjects -> {
            try {
                assertThat(futureProjects.get().stream().collect(Collectors.mapping(ProjectResponse::getName, Collectors.toList()))).isNotNull().isNotEmpty().containsExactlyInAnyOrder(expectedList.stream().toArray(String[]::new));
            } catch (InterruptedException | ExecutionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        
        //DELETE ALL

        //Create tasks to delete projects
        List<Callable<Void>> deleteTasks = runners.stream().map(pr -> pr.deleteProjects(SPACE_NAME)).collect(Collectors.toList());
        //Execute task and wait for all threads to finished
        List<Future<Void>> deleteFutures = executorService.invokeAll(deleteTasks);
        getAllDeleteDone(deleteFutures);

        //Check all projects was deleted
        assertThat(defaultWorkbenchClient.getProjects(SPACE_NAME)).isNotNull().isEmpty();
    }

    // TODO add scenario when user tries to get one space (each user have it's own space) from that space get all projects.
    // Mainly check that all spaces and projects are correctly returned.

}
