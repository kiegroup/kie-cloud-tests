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
import java.util.List;
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
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.tests.common.provider.git.Git;
import org.kie.cloud.util.Users;
import org.kie.cloud.workbenchha.AbstractWorkbenchHaIntegrationTest;
import org.kie.cloud.workbenchha.runners.ImportRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ImportGitProjectFunctionalIntegrationTest extends AbstractWorkbenchHaIntegrationTest {

    private static final String SPACE_NAME = "test-space";

    private String repositoryName;

    @Before
    public void setUp() {
        repositoryName = Git.getProvider().createGitRepositoryWithPrefix("ImportGitProjectFunctionalIntegrationTest", ImportGitProjectFunctionalIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile());
        // TODO add kjar sources for tests with projects

        defaultWorkbenchClient = WorkbenchClientProvider.getWorkbenchClient(deploymentScenario.getWorkbenchDeployment());
        defaultWorkbenchClient.createSpace(SPACE_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());
    }

    @After
    public void cleanUp(){
        defaultWorkbenchClient.deleteSpace(SPACE_NAME);

        Git.getProvider().deleteGitRepository(repositoryName);
    }

    @Test
    public void testImportProjects() throws InterruptedException,ExecutionException {
        //Create Runners with different users.
        List<ImportRunner> runners = new ArrayList<>();
        runners.add(new ImportRunner(deploymentScenario.getWorkbenchDeployment(), Users.JOHN.getName(), Users.JOHN.getPassword()));
        //... TODO

        //Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        //Create task to create projects for all users
        List<Callable<Collection<String>>> createTasks = runners.stream().map(runner -> runner.importProjects(SPACE_NAME,Git.getProvider().getRepositoryUrl(repositoryName),"RANDOM GENERATE NAME", 1, 5)).collect(Collectors.toList());
        List<Future<Collection<String>>> futures = executorService.invokeAll(createTasks);

        List<String> expectedList = new ArrayList<>();
        //Wait to all threads finish and save all created projects names
        futures.forEach(future -> {
            try {
                expectedList.addAll(future.get());
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
