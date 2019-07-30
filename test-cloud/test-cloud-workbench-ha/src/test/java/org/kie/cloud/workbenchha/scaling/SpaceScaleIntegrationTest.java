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

package org.kie.cloud.workbenchha.scaling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.guvnor.rest.client.Space;
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
import org.kie.cloud.workbenchha.runners.SpaceRunner;
import org.kie.wb.test.rest.client.WorkbenchClient;

import static org.assertj.core.api.Assertions.assertThat;

public class SpaceScaleIntegrationTest extends AbstractCloudIntegrationTest {

    private static ClusteredWorkbenchKieServerPersistentScenario deploymentScenario;

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
        deploymentScenario.setLogFolderName(SpaceScaleIntegrationTest.class.getSimpleName());
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
    }

    @Test
    public void testCreateAndDeleteSpacesWhileScaling() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<SpaceRunner> runners = new ArrayList<>();
        runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.FRODO.getName(), Users.FRODO.getPassword()));
        //... TODO

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        //Create task to create spaces for all users
        List<Callable<Collection<String>>> createTasks = runners.stream().map(runner -> runner.createSpacesWithDelays("RANDOM GENERATE NAME", 1, 10)).collect(Collectors.toList());
        List<Future<Collection<String>>> futures = executorService.invokeAll(createTasks);

        int originalWorkbenchPods = deploymentScenario.getWorkbenchDeployment().getInstances().size();
        deploymentScenario.getWorkbenchDeployment().scale(originalWorkbenchPods/2);
        

        List<String> expectedList = new ArrayList<>();
        //Wait to all threads finish and save all created spaces names
        futures.forEach(future -> {
            try {
                expectedList.addAll(future.get());
            } catch (InterruptedException | ExecutionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        //Check that all spaces where created
        assertThat(expectedList).isNotEmpty().hasSize(runners.size() * 10);      
        Collection<Space> spaces = defaultWorkbenchClient.getSpaces();
        assertThat(spaces).isNotNull();
        List<String> resultList = spaces.stream().collect(Collectors.mapping(Space::getName, Collectors.toList()));
        assertThat(resultList).containsExactlyInAnyOrder(resultList.stream().toArray(String[]::new));

        //Another run with scale up of WB
        createTasks = runners.stream().map(runner -> runner.createSpacesWithDelays("RANDOM GENERATE NAME", 11, 10)).collect(Collectors.toList());
        futures = executorService.invokeAll(createTasks);

        deploymentScenario.getWorkbenchDeployment().scale(originalWorkbenchPods);
        

        List<String> secondExpectedList = new ArrayList<>();
        //Wait to all threads finish and save all created spaces names
        futures.forEach(future -> {
            try {
                secondExpectedList.addAll(future.get());
            } catch (InterruptedException | ExecutionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        //Check that all spaces where created
        assertThat(secondExpectedList).isNotEmpty().hasSize(runners.size() * 5);      
        spaces = defaultWorkbenchClient.getSpaces();
        assertThat(spaces).isNotNull();
        resultList = spaces.stream().collect(Collectors.mapping(Space::getName, Collectors.toList()));
        expectedList.addAll(secondExpectedList);
        assertThat(resultList).containsExactlyInAnyOrder(expectedList.stream().toArray(String[]::new));

        //DELETE ALL

        //Create tasks to delete spaces
        List<Callable<Void>> deleteTasks = new ArrayList<>(runners.size());
        //Add list to delete from previous create task
        assertThat(runners).as("Check size of iterating lists.").hasSameSizeAs(futures);
        Iterator runnersIterator = runners.iterator();
        Iterator futureIterator = futures.iterator();
        while(runnersIterator.hasNext() && futureIterator.hasNext()) {
            SpaceRunner sr = (SpaceRunner) runnersIterator.next();
            Future<Collection<String>> f = (Future<Collection<String>>) futureIterator.next();
            
            deleteTasks.add(sr.deleteSpaces(f.get()));
        }

        //Execute task and wait for all threads to finished
        List<Future<Void>> deleteFutures = executorService.invokeAll(deleteTasks);

        deploymentScenario.getWorkbenchDeployment().scale(originalWorkbenchPods/2);

        deleteFutures.forEach(t -> {
            try {
                t.get();
            } catch (InterruptedException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }); // To wait for all tasks to complete

        //Check all spaces from second list were deleted 
        spaces = defaultWorkbenchClient.getSpaces();
        assertThat(spaces).isNotNull();
        resultList = spaces.stream().collect(Collectors.mapping(Space::getName, Collectors.toList()));
        assertThat(resultList).doesNotContain(secondExpectedList.stream().toArray(String[]::new));
    }
}
