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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.kie.cloud.runners.SpaceRunner;
import org.kie.cloud.runners.provider.SpaceRunnerProvider;
import org.kie.cloud.workbenchha.AbstractWorkbenchHaIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore("Scaling scenarios are not supported yet.")
public class SpaceScaleIntegrationTest extends AbstractWorkbenchHaIntegrationTest {


    @Test
    public void testCreateAndDeleteSpacesWhileScaling() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<SpaceRunner> runners = SpaceRunnerProvider.getAllRunners(deploymentScenario.getWorkbenchDeployment());

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        //Create task to create spaces for all users
        List<Callable<Collection<String>>> createTasks = runners.stream().map(runner -> runner.createSpacesWithDelays(UUID.randomUUID().toString().substring(0, 6), 1, 10)).collect(Collectors.toList());
        List<Future<Collection<String>>> futures = executorService.invokeAll(createTasks);

        int originalWorkbenchPods = deploymentScenario.getWorkbenchDeployment().getInstances().size();
        deploymentScenario.getWorkbenchDeployment().scale(originalWorkbenchPods/2);
        

        List<String> expectedList = getAllStringFromFutures(futures);

        //Check that all spaces where created
        checkSpacesWereCreated(expectedList, runners.size(), 10);

        //Another run with scale up of WB
        createTasks = runners.stream().map(runner -> runner.createSpacesWithDelays(UUID.randomUUID().toString().substring(0, 6), 11, 10)).collect(Collectors.toList());
        futures = executorService.invokeAll(createTasks);

        deploymentScenario.getWorkbenchDeployment().scale(originalWorkbenchPods);
        

        List<String> secondExpectedList = new ArrayList<>(expectedList);
        secondExpectedList.addAll(getAllStringFromFutures(futures));        

        //Check that all spaces where created
        checkSpacesWereCreated(secondExpectedList, runners.size(), 20);

        //DELETE ALL

        //Create tasks to delete spaces
        List<Callable<Void>> deleteTasks = runners.stream().map(SpaceRunner::deleteSpaces).collect(Collectors.toList());

        //Execute task and wait for all threads to finished
        List<Future<Void>> deleteFutures = executorService.invokeAll(deleteTasks);

        deploymentScenario.getWorkbenchDeployment().scale(originalWorkbenchPods/2);

        getAllDeleteDone(deleteFutures);

        //Check all spaces was deleted
        assertThat(defaultWorkbenchClient.getSpaces()).isNotNull().isEmpty();
    }
}
