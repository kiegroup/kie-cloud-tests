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
import org.junit.Ignore;
import org.junit.Test;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.runners.ImportRunner;
import org.kie.cloud.runners.provider.ImportRunnerProvider;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.provider.git.Git;
import org.kie.cloud.util.SpaceProjects;
import org.kie.cloud.workbenchha.AbstractWorkbenchHaIntegrationTest;
import org.kie.cloud.workbenchha.functional.ImportGitProjectFunctionalIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore("Survival scenarios are not supported yet.")
public class ImportProjectSurvivalIntegrationTest extends AbstractWorkbenchHaIntegrationTest {

    private Map<String,String> projectNameRepository;

    @Before
    public void setUp() {
        projectNameRepository = new HashMap<>();
        projectNameRepository.put(Kjar.DEFINITION.getName(), Git.getProvider().getRepositoryUrl(Git.getProvider().createGitRepositoryWithPrefix(UUID.randomUUID().toString().substring(0, 4), ImportGitProjectFunctionalIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile() + "/" + Kjar.DEFINITION.getName())));
        projectNameRepository.put(Kjar.HELLO_RULES.getName(), Git.getProvider().getRepositoryUrl(Git.getProvider().createGitRepositoryWithPrefix(UUID.randomUUID().toString().substring(0, 4), ImportGitProjectFunctionalIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile() + "/" + Kjar.HELLO_RULES.getName())));
        projectNameRepository.put(Kjar.STATELESS_SESSION.getName(), Git.getProvider().getRepositoryUrl(Git.getProvider().createGitRepositoryWithPrefix(UUID.randomUUID().toString().substring(0, 4), ImportGitProjectFunctionalIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile() + "/" + Kjar.STATELESS_SESSION.getName())));

        defaultWorkbenchClient = WorkbenchClientProvider.getWorkbenchClient(deploymentScenario.getWorkbenchDeployment());
        //defaultWorkbenchClient.createSpace(SPACE_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());
    }

    @After
    public void cleanUp(){
        //defaultWorkbenchClient.deleteSpace(SPACE_NAME);

        projectNameRepository.values().forEach(Git.getProvider()::deleteGitRepository);
    }

    @Test
    public void testImportProjects() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<ImportRunner> runners = ImportRunnerProvider.getAllRunners(deploymentScenario.getWorkbenchDeployment());

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        //Create task to create projects for all users
        
        // TODO use in tests more repos
        List<Callable<SpaceProjects>> createTasks = runners.stream().map(runner -> runner.asyncImportProjects(UUID.randomUUID().toString().substring(0, 4),projectNameRepository)).collect(Collectors.toList());

        List<Future<SpaceProjects>> futures = executorService.invokeAll(createTasks);

        // Delete all pods
        List<Instance> allPods = deploymentScenario.getWorkbenchDeployment().getInstances();
        deploymentScenario.getWorkbenchDeployment().deleteInstances(allPods);

        deploymentScenario.getWorkbenchDeployments().stream().forEach(WorkbenchDeployment::waitForScale);

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

        
    }
}
