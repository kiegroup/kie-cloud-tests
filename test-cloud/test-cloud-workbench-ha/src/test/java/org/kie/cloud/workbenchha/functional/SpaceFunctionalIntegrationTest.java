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

import org.guvnor.rest.client.Space;
import org.junit.Test;
import org.kie.cloud.runners.SpaceRunner;
import org.kie.cloud.runners.provider.SpaceRunnerProvider;
import org.kie.cloud.workbenchha.AbstractWorkbenchHaIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class SpaceFunctionalIntegrationTest extends AbstractWorkbenchHaIntegrationTest {


    @Test
    public void testCreateAndDeleteSpaces() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<SpaceRunner> runners = SpaceRunnerProvider.getAllRunners(deploymentScenario.getWorkbenchDeployment());

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());

        //Create task to create spaces for all users
        List<Callable<Collection<String>>> createTasks = runners.stream().map(runner -> runner.createSpaces(UUID.randomUUID().toString().substring(0, 6), 1, 5)).collect(Collectors.toList());

        //createTasks = runners.stream().map((UUID.randomUUID().toString().substring(0, 6), 1, 5) -> SpaceRunner::createSpaces).collect(Collectors.toList());

        List<Future<Collection<String>>> futures = executorService.invokeAll(createTasks);

        List<String> expectedList = getAllStringFromFutures(futures);

        //Check that all spaces where created
        checkSpacesWereCreated(expectedList, runners.size(), 5);

        //GET ALL

        List<Callable<Collection<Space>>> getAllTask = runners.stream().map(SpaceRunner::getAllSpaces).collect(Collectors.toList());
        List<Future<Collection<Space>>> futuresSpaces = executorService.invokeAll(getAllTask);
        futuresSpaces.forEach(futureSpaces -> {
            try {
                assertThat(futureSpaces.get().stream().collect(Collectors.mapping(Space::getName, Collectors.toList()))).containsExactlyInAnyOrder(expectedList.stream().toArray(String[]::new));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Collecting of all task results failed.",e);
            }
        });
        
        //DELETE ALL
        //Create tasks to delete spaces
        List<Callable<Void>> deleteTasks = runners.stream().map(SpaceRunner::deleteSpaces).collect(Collectors.toList());
        //Add list to delete from previous create task

        // TODO assign to runner 1) Add all futures togehter and assign to them by runner size
        // 2) return resutls from create run in pair where is runner and result together
        // 3) save created spaces by the runner into the instance

        //Execute task and wait for all threads to finished
        List<Future<Void>> deleteFutures = executorService.invokeAll(deleteTasks);
        getAllDeleteDone(deleteFutures);

        //Check all spaces was deleted
        assertThat(defaultWorkbenchClient.getSpaces()).isNotNull().isEmpty();
    }
}
