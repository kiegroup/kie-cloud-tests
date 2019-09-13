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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import org.kie.cloud.util.Users;
import org.kie.cloud.workbenchha.AbstractWorkbenchHaIntegrationTest;
import org.kie.cloud.workbenchha.runners.ProjectRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectFunctionalIntegrationTest extends AbstractWorkbenchHaIntegrationTest {

    private static final String SPACE_NAME = "test-space";

    @Before
    public void createTestingSpace() {
        //defaultWorkbenchClient.createSpace(SPACE_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());
    }

    @After
    public void deleteTestingSpace(){
        //defaultWorkbenchClient.deleteSpace(SPACE_NAME);
    }

    @Test
    public void testCreateAndDeleteProjects() throws InterruptedException,ExecutionException {
        defaultWorkbenchClient.createSpace(SPACE_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());

        //assertThat(defaultWorkbenchClient.getSpace(SPACE_NAME)).isNotNull();

        //Create Runners with different users.
        List<ProjectRunner> runners = new ArrayList<>();
        runners.add(new ProjectRunner(deploymentScenario.getWorkbenchDeployment(), Users.JOHN.getName(), Users.JOHN.getPassword()));
        //runners.add(new ProjectRunner(deploymentScenario.getWorkbenchDeployment(), Users.FRODO.getName(), Users.FRODO.getPassword()));
        /*runners.add(new ProjectRunner(deploymentScenario.getWorkbenchDeployment(), Users.SAM.getName(), Users.SAM.getPassword()));
        runners.add(new ProjectRunner(deploymentScenario.getWorkbenchDeployment(), Users.MERRY.getName(), Users.MERRY.getPassword()));
        runners.add(new ProjectRunner(deploymentScenario.getWorkbenchDeployment(), Users.PIPPIN.getName(), Users.PIPPIN.getPassword()));
        runners.add(new ProjectRunner(deploymentScenario.getWorkbenchDeployment(), Users.ARAGORN.getName(), Users.ARAGORN.getPassword()));
        runners.add(new ProjectRunner(deploymentScenario.getWorkbenchDeployment(), Users.LEGOLAS.getName(), Users.LEGOLAS.getPassword()));
        runners.add(new ProjectRunner(deploymentScenario.getWorkbenchDeployment(), Users.GIMLI.getName(), Users.GIMLI.getPassword()));
        runners.add(new ProjectRunner(deploymentScenario.getWorkbenchDeployment(), Users.GANDALF.getName(), Users.GANDALF.getPassword()));
        runners.add(new ProjectRunner(deploymentScenario.getWorkbenchDeployment(), Users.BOROMIR.getName(), Users.BOROMIR.getPassword()));
        */
        //... TODO

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        //Create task to create projects for all users
        List<Callable<Collection<String>>> createTasks = runners.stream().map(runner -> runner.createProjects(SPACE_NAME, UUID.randomUUID().toString().substring(0, 6), 1, 5)).collect(Collectors.toList());
        List<Future<Collection<String>>> futures = executorService.invokeAll(createTasks);

        List<String> expectedList = getAllStringFromFutures(futures);

        //Check that all projects where created
        checkProjectsWereCreated(SPACE_NAME, expectedList, runners.size(), 5);

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
        List<Callable<Void>> deleteTasks = new ArrayList<>(runners.size());
        //Add list to delete from previous create task
        assertThat(runners).as("Check size of iterating lists.").hasSameSizeAs(futures);
        Iterator runnersIterator = runners.iterator();
        Iterator futureIterator = futures.iterator();
        while(runnersIterator.hasNext() && futureIterator.hasNext()) {
            ProjectRunner sr = (ProjectRunner) runnersIterator.next();
            Future<Collection<String>> f = (Future<Collection<String>>) futureIterator.next();
            
            deleteTasks.add(sr.deleteProjects(SPACE_NAME,f.get()));
        }

        //Execute task and wait for all threads to finished
        List<Future<Void>> deleteFutures = executorService.invokeAll(deleteTasks);
        getAllDeleteDone(deleteFutures);

        //Check all projects was deleted
        assertThat(defaultWorkbenchClient.getProjects(SPACE_NAME)).isNotNull().isEmpty();
    }

    // TODO add scenario when user tries to get one space (each user have it's own space) from that space get all projects.
    // Mainly check that all spaces and projects are correctly returned.

}
