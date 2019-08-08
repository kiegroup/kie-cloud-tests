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
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.util.Users;
import org.kie.wb.test.rest.client.WorkbenchClient;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractWorkbenchHaIntegrationTest extends AbstractCloudIntegrationTest {

    protected static ClusteredWorkbenchKieServerPersistentScenario deploymentScenario;

    protected WorkbenchClient defaultWorkbenchClient;

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
        deploymentScenario.setLogFolderName(AbstractWorkbenchHaIntegrationTest.class.getSimpleName()); // TODO check if works ok
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

    protected void checkSpacesWereCreated(Collection<String> expectedSpaceNames, int runnersSize, int retries) {
        assertThat(expectedSpaceNames).isNotEmpty().hasSize(runnersSize * retries);
        Collection<Space> spaces = defaultWorkbenchClient.getSpaces();
        assertThat(spaces).isNotNull();
        List<String> resultSpaceNameList = spaces.stream().collect(Collectors.mapping(Space::getName, Collectors.toList()));
        assertThat(resultSpaceNameList).containsExactlyInAnyOrder(expectedSpaceNames.stream().toArray(String[]::new));
    }

    protected void checkProjectsWereCreated(String spaceName, Collection<String> expectedProjectNames, int runnersSize, int retries) {
        assertThat(expectedProjectNames).isNotEmpty().hasSize(runnersSize * retries);        
        Collection<ProjectResponse> projects = defaultWorkbenchClient.getProjects(spaceName);
        assertThat(projects).isNotNull();
        List<String> resultList = projects.stream().collect(Collectors.mapping(ProjectResponse::getName, Collectors.toList()));
        assertThat(resultList).containsExactlyInAnyOrder(expectedProjectNames.stream().toArray(String[]::new));
    }

    protected List<String> getAllStringFromFutures(List<Future<Collection<String>>> futures) {
        List<String> list = new ArrayList<>();

        //Wait to all threads finish
        futures.forEach(future -> {
            try {
                list.addAll(future.get());
            } catch (InterruptedException | ExecutionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }


    // Generic function to split a list into two sublists
    protected <T> List[] split(List<T> list) {
        int size = list.size();

        List<T> first = new ArrayList<>(list.subList(0, (size + 1) / 2));
        List<T> second = new ArrayList<>(list.subList((size + 1) / 2, size));

        return new List[] { first, second };
    }
}