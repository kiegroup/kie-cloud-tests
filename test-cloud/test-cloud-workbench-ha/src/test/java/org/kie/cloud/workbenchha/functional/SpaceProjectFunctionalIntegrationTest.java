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

import java.time.Duration;
import java.util.ArrayList;
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
import org.guvnor.rest.client.Space;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.runners.SpaceProjectRunner;
import org.kie.cloud.runners.provider.SpaceProjectRunnerProvider;
import org.kie.cloud.util.SpaceProjects;
import org.kie.cloud.workbenchha.AbstractWorkbenchHaIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class SpaceProjectFunctionalIntegrationTest extends AbstractWorkbenchHaIntegrationTest {

    private static final String SPACE_NAME = "test-space-2";
    //private static final int PROJECT_PER_USER = 5;

    @Before
    public void createTestingSpace() {
        //defaultWorkbenchClient.getSpaces().forEach(space->defaultWorkbenchClient.deleteSpace(space.getName()));
        defaultWorkbenchClient.createSpace(SPACE_NAME, "adminUser");//deploymentScenario.getWorkbenchDeployment().getUsername());
    }

    @After
    public void deleteTestingSpace(){
        defaultWorkbenchClient.deleteSpace(SPACE_NAME);
    }

    @Test
    public void testCreateAndDeleteSpaceAndProject() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<SpaceProjectRunner> runners = SpaceProjectRunnerProvider.getAllRunners(deploymentScenario.getWorkbenchDeployment());

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());

        //Create task to create projects for all users
        List<Callable<SpaceProjects>> createTasks = runners.stream().map(runner -> runner.createSpaceAndProject(UUID.randomUUID().toString().substring(0, 4), UUID.randomUUID().toString().substring(0, 6))).collect(Collectors.toList());
        //List<Callable<SpaceProjects>> createTasks = runners.stream().map(runner -> runner.createSpaceAndProject(UUID.randomUUID().toString().substring(0, 4), UUID.randomUUID().toString().substring(0, 6))).collect(Collectors.toList());
        List<Future<SpaceProjects>> futures = executorService.invokeAll(createTasks);

        
        List<SpaceProjects> expectedList = getAllSpaceProjectsFromFutures(futures);

        // TODO delete, try to add there thread sleep so indexing failures can be fixed, maybe?
        Thread.sleep(Duration.ofSeconds(30).toMillis());

        //Check that all projects where created
        assertThat(expectedList).isNotEmpty().hasSize(runners.size());

        assertThat(defaultWorkbenchClient.getSpaces().stream().map(Space::getName)).as("Check all spaces were created").containsAll(expectedList.stream().map(SpaceProjects::getSpaceName).collect(Collectors.toList()));
        expectedList.forEach(spaceProjects -> {
            System.out.println("** "+spaceProjects.getSpaceName()+"  "+defaultWorkbenchClient.getProjects(spaceProjects.getSpaceName()).stream().map(ProjectResponse::getName).collect(Collectors.toList()));
            assertThat(defaultWorkbenchClient.getProjects(spaceProjects.getSpaceName()).stream().map(ProjectResponse::getName)).as("Check Project in space %s",spaceProjects.getSpaceName()).containsAll(spaceProjects.getProjectNames());
        });
        
        //DELETE ALL

        //Create tasks to delete projects
        List<Callable<Void>> deleteTasks = runners.stream().map(SpaceProjectRunner::deleteProjects).collect(Collectors.toList());
        //Execute task and wait for all threads to finished
        List<Future<Void>> deleteFutures = executorService.invokeAll(deleteTasks);
        getAllDeleteDone(deleteFutures);

        //Check all projects was deleted
        expectedList.forEach(spaceProjects-> {
            assertThat(defaultWorkbenchClient.getProjects(spaceProjects.getSpaceName())).isNotNull().isEmpty();
        });
        
    }

    protected List<SpaceProjects> getAllSpaceProjectsFromFutures(List<Future<SpaceProjects>> futures) {
        List<SpaceProjects> list = new ArrayList<>();

        //Wait to all threads finish
        futures.forEach(future -> {
            try {
                list.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Collecting of all task results failed.",e);
            }
        });

        return list;
    }

    protected void checkProjectsWereCreated(String spaceName, Collection<String> expectedProjectNames) {
        Collection<ProjectResponse> projects = defaultWorkbenchClient.getProjects(spaceName);
        assertThat(projects).isNotNull();
        List<String> resultList = projects.stream().collect(Collectors.mapping(ProjectResponse::getName, Collectors.toList()));
        assertThat(resultList).containsExactlyInAnyOrder(expectedProjectNames.stream().toArray(String[]::new));
    }
}
