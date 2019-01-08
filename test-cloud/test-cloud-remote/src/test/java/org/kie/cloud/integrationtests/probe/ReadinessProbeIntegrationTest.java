/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.integrationtests.probe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.guvnor.rest.client.Space;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.common.rest.Authenticator;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.wb.test.rest.client.WorkbenchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class ReadinessProbeIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<WorkbenchKieServerScenario> {

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public WorkbenchKieServerScenario workbenchKieServerScenario;

    String repositoryName;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            WorkbenchKieServerScenario workbenchKieServerScenario = deploymentScenarioFactory.getWorkbenchKieServerScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
            scenarios.add(new Object[]{"Workbench + KIE Server", workbenchKieServerScenario});
        } catch (UnsupportedOperationException ex) {
            logger.info("Workbench + KIE Server is skipped.", ex);
        }

        try {
            WorkbenchKieServerPersistentScenario workbenchKieServerPersistentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
            scenarios.add(new Object[] { "Workbench + KIE Server - Persistent", workbenchKieServerPersistentScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("Workbench + KIE Server - Persistent is skipped.", ex);
        }

        return scenarios;
    }

    @Override
    protected WorkbenchKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return workbenchKieServerScenario;
    }

    private Client httpKieServerClient;

    private static final Logger logger = LoggerFactory.getLogger(ReadinessProbeIntegrationTest.class);

    @Before
    public void setUp() {
        httpKieServerClient = new ResteasyClientBuilder()
                .establishConnectionTimeout(10, TimeUnit.SECONDS)
                .socketTimeout(10, TimeUnit.SECONDS)
                .register(new Authenticator(deploymentScenario.getKieServerDeployment().getUsername(),
                                deploymentScenario.getKieServerDeployment().getPassword()))
                .build();
    }

    @After
    public void tearDown() {
        if (httpKieServerClient != null) {
            httpKieServerClient.close();
        }

        if (repositoryName != null) {
            gitProvider.deleteGitRepository(repositoryName);
            repositoryName = null;
        }
    }

    @Test
    public void testWorkbenchReadinessProbe() {
        WorkbenchClient workbenchClient = WorkbenchClientProvider.getWorkbenchClient(deploymentScenario.getWorkbenchDeployment());

        checkBCLoginScreenAvailable();
        logger.debug("Check that workbench REST is available");
        workbenchClient.createSpace(SPACE_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());
        Collection<Space> spaces = workbenchClient.getSpaces();
        Assertions.assertThat(spaces.stream().anyMatch(x -> x.getName().equals(SPACE_NAME))).isTrue();

        logger.debug("Scale workbench to 0");
        deploymentScenario.getWorkbenchDeployment().scale(0);
        deploymentScenario.getWorkbenchDeployment().waitForScale();
        logger.debug("Scale workbench to 1");
        deploymentScenario.getWorkbenchDeployment().scale(1);
        deploymentScenario.getWorkbenchDeployment().waitForScale();

        checkBCLoginScreenAvailable();
        logger.debug("Check that workbench REST is available");
        workbenchClient = WorkbenchClientProvider.getWorkbenchClient(deploymentScenario.getWorkbenchDeployment());
        workbenchClient.createSpace(SPACE_SECOND_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());
        spaces = workbenchClient.getSpaces();
        Assertions.assertThat(spaces.stream().anyMatch(x -> x.getName().equals(SPACE_SECOND_NAME))).isTrue();
    }

    @Test
    public void testKieServerReadinessProbe() {
        String repositoryName = gitProvider.createGitRepositoryWithPrefix(deploymentScenario.getWorkbenchDeployment().getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEFINITION_PROJECT_NAME).getFile());

        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(repositoryName), deploymentScenario.getWorkbenchDeployment(), DEFINITION_PROJECT_NAME);

        KieServerInfo serverInfo = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment()).getServerInfo().getResult();
        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);

        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JAXB, this.getClass().getClassLoader());

        deploymentScenario.getKieServerDeployment().scale(0);
        deploymentScenario.getKieServerDeployment().waitForScale();
        deploymentScenario.getKieServerDeployment().scale(1);
        deploymentScenario.getKieServerDeployment().waitForScale();

        WebTarget target;
        try {
            URL url = new URL(deploymentScenario.getKieServerDeployment().getUrl(), KIE_CONTAINERS_REQUEST_URL);
            target = httpKieServerClient.target(url.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error creating list container request.", e);
        }
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + 1000L;
        while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {
            Response response = target.request().get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                ServiceResponse<?> serviceResponse = marshaller.unmarshall(response.readEntity(String.class), ServiceResponse.class);
                KieContainerResourceList containerList = (KieContainerResourceList) serviceResponse.getResult();
                Assertions.assertThat(containerList.getContainers().size()).isEqualTo(1);
                Assertions.assertThat(containerList.getContainers().get(0).getContainerId()).isEqualTo(CONTAINER_ID);
                return;
            } else {
                response.close();
                Assertions.assertThat(response.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
            }
        }
        Assertions.fail("Timeout while waiting for OpenShift router to establish connection to Kie server.");
    }

    private void checkBCLoginScreenAvailable() {
        logger.debug("Check that workbench login screen is available");
        URL url = deploymentScenario.getWorkbenchDeployment().getUrl();
        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            logger.debug("Connecting to workbench login screen");
            httpURLConnection.connect();
            logger.debug("Http response code is {}", httpURLConnection.getResponseCode());
            Assertions.assertThat(httpURLConnection.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);

            logger.debug("Reading login page content");
            String responseContent = IOUtils.toString(httpURLConnection.getInputStream(), "UTF-8");
            logger.debug("Login page content contains {} characters", responseContent.length());
            httpURLConnection.disconnect();

            Assertions.assertThat(responseContent).contains(WORKBENCH_LOGIN_SCREEN_TEXT);

        } catch (IOException e) {
            Assertions.fail("Unable to load workbench login screen", e);
        }
    }
}
