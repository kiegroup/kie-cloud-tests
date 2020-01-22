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

package org.kie.cloud.workbenchha;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.guvnor.rest.client.ProjectResponse;
import org.guvnor.rest.client.Space;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
//import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.util.Users;
import org.kie.wb.test.rest.client.WorkbenchClient;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractWorkbenchHaIntegrationTest extends AbstractCloudIntegrationTest {

    //protected WorkbenchKieServerPersistentScenario deploymentScenario;
    protected ClusteredWorkbenchKieServerDatabasePersistentScenario deploymentScenario; // TODO parametrized for jbpm and drools template

    protected WorkbenchClient defaultWorkbenchClient;

    // TODO this needs to be configure different way, as this solution right now does not support 2 and more test running in parralle
    // probably becuase deployment scenarios are (static) is override :( - my bad
    @Before
    public void initializeDeployment() {
        try {
            //deploymentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
            deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder()
                    .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(),
                            MavenConstants.getMavenRepoPassword())
                    .deploySso()
                    .build();
        } catch (UnsupportedOperationException ex) {
            Assume.assumeFalse(ex.getMessage().startsWith("Not supported"));
        }
        deploymentScenario.setLogFolderName(this.getClass().getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);
        
        // Create users in SSO
        Map<String, String> users = Stream.of(Users.class.getEnumConstants()).collect(Collectors.toMap(Users::getName, Users::getPassword));
        SsoDeployer.createUsers(deploymentScenario.getSsoDeployment(), users);

        // Create default Workbench user
        defaultWorkbenchClient = WorkbenchClientProvider.getWorkbenchClient(deploymentScenario.getWorkbenchDeployment());
    }

    @After
    public void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    protected void checkSpacesWereCreated(Collection<String> expectedSpaceNames, int runnersSize, int retries) {
        assertThat(expectedSpaceNames).isNotEmpty().hasSize(runnersSize * retries);
        Collection<Space> spaces = defaultWorkbenchClient.getSpaces();
        assertThat(spaces).isNotNull();
        List<String> resultSpaceNameList = spaces.stream().collect(Collectors.mapping(Space::getName, Collectors.toList()));
        assertThat(resultSpaceNameList).containsExactlyInAnyOrder(expectedSpaceNames.stream().toArray(String[]::new));
    }

    protected void checkProjectsWereCreated(String spaceName, Collection<String> expectedProjectNames) {
        Collection<ProjectResponse> projects = defaultWorkbenchClient.getProjects(spaceName);
        assertThat(projects).isNotNull();
        List<String> resultList = projects.stream().collect(Collectors.mapping(ProjectResponse::getName, Collectors.toList()));
        assertThat(resultList).containsExactlyInAnyOrder(expectedProjectNames.stream().toArray(String[]::new));
    }

    protected boolean wereProjectsCreated(String spaceName, Collection<String> expectedProjectNames) {
        return defaultWorkbenchClient.getProjects(spaceName).stream().collect(Collectors.mapping(ProjectResponse::getName, Collectors.toList())).containsAll(expectedProjectNames);
    }

    protected List<String> getAllStringFromFutures(List<Future<Collection<String>>> futures) {
        List<String> list = new ArrayList<>();

        //Wait to all threads finish
        futures.forEach(future -> {
            try {
                list.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Collecting of all task results failed.",e);
            }
        });

        return list;
    }

    protected void getAllDeleteDone(List<Future<Void>> deleteFutures) {
        //Wait to all delete threads finish
        deleteFutures.forEach(t -> {
            try {
                t.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Collecting of all task results failed.",e);
            }
        });
    }


    // Generic function to split a list into two subLists
    protected <T> List<List<T>> split(List<T> list) {
        List<List<T>> parts = new ArrayList<List<T>>();

        int size = list.size();
        if (size <= 1) {
            throw new RuntimeException("List size is smaller then 2 so list cannot be split.");
        }
        parts.add(new ArrayList<>(list.subList(0, (size + 1) / 2)));
        parts.add(new ArrayList<>(list.subList((size + 1) / 2, size)));

        return parts;
    }
}