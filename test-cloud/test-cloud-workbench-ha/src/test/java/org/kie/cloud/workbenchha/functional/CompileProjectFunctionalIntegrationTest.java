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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.runners.CompileRunner;
import org.kie.cloud.runners.ImportRunner;
import org.kie.cloud.runners.provider.CompileRunnerProvider;
import org.kie.cloud.runners.provider.ImportRunnerProvider;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.provider.git.Git;
import org.kie.cloud.util.SpaceProjects;
import org.kie.cloud.workbenchha.AbstractWorkbenchHaIntegrationTest;
import org.kie.wb.test.rest.client.WorkbenchClient;

import static org.assertj.core.api.Assertions.assertThat;

public class CompileProjectFunctionalIntegrationTest extends AbstractWorkbenchHaIntegrationTest {

    private static ClusteredWorkbenchKieServerPersistentScenario deploymentScenario;

    private Map<String, String> projectNameRepository;
    private List<SpaceProjects> spaceProjects;

    private WorkbenchClient defaultWorkbenchClient;

    @Before
    public void setUp() {

        projectNameRepository = new HashMap<>();
        projectNameRepository.put(Kjar.DEFINITION.getName(),
                Git.getProvider().createGitRepositoryWithPrefix(
                        UUID.randomUUID().toString().substring(0, 4),
                        CompileProjectFunctionalIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile()
                                + "/" + Kjar.DEFINITION.getName()));
        projectNameRepository.put(Kjar.HELLO_RULES.getName(),
                Git.getProvider().createGitRepositoryWithPrefix(
                        UUID.randomUUID().toString().substring(0, 4),
                        CompileProjectFunctionalIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile()
                                + "/" + Kjar.HELLO_RULES.getName()));
        projectNameRepository.put(Kjar.STATELESS_SESSION.getName(),
                Git.getProvider().createGitRepositoryWithPrefix(
                        UUID.randomUUID().toString().substring(0, 4),
                        CompileProjectFunctionalIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile()
                                + "/" + Kjar.STATELESS_SESSION.getName()));

        defaultWorkbenchClient = WorkbenchClientProvider
                .getWorkbenchClient(deploymentScenario.getWorkbenchDeployment());
        // defaultWorkbenchClient.createSpace(SPACE_NAME,
        // deploymentScenario.getWorkbenchDeployment().getUsername());

        try {
            spaceProjects = importProjects();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Collecting of all task results failed.",e);
        }
    }

    @After
    public void cleanUp(){
        //defaultWorkbenchClient.deleteSpace(SPACE_NAME);

        projectNameRepository.values().forEach(Git.getProvider()::deleteGitRepository);
    }

    @Test
    public void testCompileProjects() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<CompileRunner> runners = CompileRunnerProvider.getAllRunners(deploymentScenario.getWorkbenchDeployment());

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        //Create task to create projects for all users
        List<Callable<Void>> compileTasks = runners.stream()
            .map(cr->cr.compileProjects(spaceProjects.stream()
                .filter(sp -> defaultWorkbenchClient.getSpace(sp.getSpaceName()).getOwner().equals(cr.getRunnerUser()))
                .findAny()
                .orElseThrow(()->new RuntimeException(""))))
            .collect(Collectors.toList());
        //runners.stream().map(cr ->  cr.compileProjects(getSpaceProjects())).collect(Collectors.toList()));
        List<Future<Void>> futures = executorService.invokeAll(compileTasks);

        List<String> expectedList = new ArrayList<>();
        //Wait to all threads finish and save all compiled projects names
        futures.forEach(future -> {
            try {
                future.get();
                expectedList.add("Compile is done for "+future);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Collecting of all task results failed.",e);
            }
        });

        //Check that all projects where compiled
        assertThat(expectedList).isNotEmpty().hasSize(runners.size());
    }

    @Test
    public void testDeployProjects() throws InterruptedException,ExecutionException {
        List<CompileRunner> runners = CompileRunnerProvider.getAllRunners(deploymentScenario.getWorkbenchDeployment());

        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        List<Callable<Void>> deployTasks = runners.stream()
            .map(cr->cr.deployProjects(spaceProjects.stream()
                .filter(sp -> defaultWorkbenchClient.getSpace(sp.getSpaceName()).getOwner().equals(cr.getRunnerUser()))
                .findAny()
                .orElseThrow(()->new RuntimeException(""))))
            .collect(Collectors.toList());
        List<Future<Void>> futures = executorService.invokeAll(deployTasks);

        List<String> expectedList = new ArrayList<>();
        //Wait to all threads finish and save all compiled projects names
        futures.forEach(future -> {
            try {
                future.get();
                expectedList.add("Deploy is done for "+future);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Collecting of all task results failed.",e);
            }
        });

        //Check that all projects where compiled
        assertThat(expectedList).isNotEmpty().hasSize(runners.size());
    }

    @Test
    public void testInstallProjects() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<CompileRunner> runners = CompileRunnerProvider.getAllRunners(deploymentScenario.getWorkbenchDeployment());

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        List<Callable<Void>> installTasks = runners.stream()
            .map(cr->cr.installProjects(spaceProjects.stream()
                .filter(sp -> defaultWorkbenchClient.getSpace(sp.getSpaceName()).getOwner().equals(cr.getRunnerUser()))
                .findAny()
                .orElseThrow(()->new RuntimeException(""))))
            .collect(Collectors.toList());
        List<Future<Void>> futures = executorService.invokeAll(installTasks);

        List<String> expectedList = new ArrayList<>();
        //Wait to all threads finish and save all compiled projects names
        futures.forEach(future -> {
            try {
                future.get();
                expectedList.add("Install is done for "+future);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Collecting of all task results failed.",e);
            }
        });

        //Check that all projects where compiled
        assertThat(expectedList).isNotEmpty().hasSize(runners.size());
    }


    private List<SpaceProjects> importProjects() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<ImportRunner> runners = ImportRunnerProvider.getAllRunners(deploymentScenario.getWorkbenchDeployment());

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        //Create task to create projects for all users
        
        // use in tests more repos
        List<Callable<SpaceProjects>> createTasks = runners.stream().map(runner -> runner.importProjects(UUID.randomUUID().toString().substring(0, 4),projectNameRepository)).collect(Collectors.toList());

        List<Future<SpaceProjects>> futures = executorService.invokeAll(createTasks);

        List<SpaceProjects> expectedList = new ArrayList<>();
        //Wait to all threads finish and save all created projects names
        futures.forEach(future -> {
            try {
                expectedList.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Collecting of all task results failed.",e);
            }
        });

        //Check that all projects where created
        assertThat(expectedList).isNotEmpty().hasSize(runners.size());        

        assertThat(defaultWorkbenchClient.getSpaces().stream().map(Space::getName)).as("Check all spaces were created").containsAll(expectedList.stream().map(SpaceProjects::getSpaceName).collect(Collectors.toList()));
        expectedList.forEach(spaceProjects -> {
            assertThat(defaultWorkbenchClient.getProjects(spaceProjects.getSpaceName()).stream().map(ProjectResponse::getName)).as("Check Projects in space %s",spaceProjects.getSpaceName()).containsAll(spaceProjects.getProjectNames());
        });

        return expectedList;
    }
}
