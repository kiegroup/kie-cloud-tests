/*
 * Copyright 2017 JBoss by Red Hat.
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
package org.kie.cloud.integrationtests.survival;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.common.rest.Authenticator;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.KieServerControllerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(JBPMOnly.class)
public class KieServerWithWorkbenchSurvivalIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<WorkbenchKieServerPersistentScenario> {

    private KieServerControllerClient kieServerControllerClient;

    private String repositoryName;

    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected QueryServicesClient queryServicesClient;

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithWorkbenchSurvivalIntegrationTest.class);

    @Override
    protected WorkbenchKieServerPersistentScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder().build();
    }

    @Before
    public void setUp() {
        repositoryName = gitProvider.createGitRepositoryWithPrefix(deploymentScenario.getWorkbenchDeployment().getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER).getFile() + "/" + DEFINITION_PROJECT_NAME);

        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(repositoryName), deploymentScenario.getWorkbenchDeployment(), DEFINITION_PROJECT_NAME);

        kieServerControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());

        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
        queryServicesClient = KieServerClientProvider.getQueryClient(deploymentScenario.getKieServerDeployment());
    }

    @After
    public void tearDown() {
        gitProvider.deleteGitRepository(repositoryName);
    }

    @Test
    public void kieServerScaleTest() {
        KieServerInfo serverInfo = kieServicesClient.getServerInfo().getResult();
        checkServerTemplateIsRegistred(serverInfo.getServerId(), serverInfo.getName());

        scaleKieServerTo(0);
        checkServerTemplateInstanceCount(serverInfo.getServerId(), 0);

        scaleKieServerTo(1);
        checkServerTemplateInstanceCount(serverInfo.getServerId(), 1);

        logger.debug("Register Kie Container to Kie Server");
        WorkbenchUtils.saveContainerSpec(kieServerControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        logger.debug("Start process instance");
        Long signalPid = processServicesClient.startProcess(CONTAINER_ID, Constants.ProcessId.SIGNALTASK);
        assertThat(signalPid).isNotNull().isGreaterThan(0L);
        assertThat(queryServicesClient.findProcessInstances(0, 10)).isNotNull().hasSize(1);

        scaleKieServerTo(0);
        checkServerTemplateInstanceCount(serverInfo.getServerId(), 0);

        logger.debug("Send REST request to Kie Server");
        sendRESTCallsToUnavailableKieServer();

        scaleKieServerTo(1);
        checkServerTemplateInstanceCount(serverInfo.getServerId(), 1);

        logger.debug("Start new process instance");
        Long newPid = processServicesClient.startProcess(CONTAINER_ID, Constants.ProcessId.SIGNALTASK);
        assertThat(newPid).isNotNull().isGreaterThan(0L);

        logger.debug("Check started processes");
        assertThat(queryServicesClient.findProcessInstances(0, 10)).isNotNull().hasSize(2);

        ProcessInstance processInstance = processServicesClient.getProcessInstance(CONTAINER_ID, signalPid);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getState()).isNotNull().isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
    }

    private void scaleKieServerTo(int count) {
        logger.debug("Scale Kie Server to " + count);
        deploymentScenario.getKieServerDeployment().scale(count);
        deploymentScenario.getKieServerDeployment().waitForScale();
    }

    protected void checkServerTemplateIsRegistred(String expectedId, String expectedName) {
        assertThat(kieServerControllerClient.listServerTemplates().getServerTemplates()).hasSize(1);
        ServerTemplate serverTemplate = kieServerControllerClient.listServerTemplates().getServerTemplates()[0];
        assertThat(serverTemplate.getId()).isEqualTo(expectedId);
        assertThat(serverTemplate.getName()).isEqualTo(expectedName);
    }

    protected void checkServerTemplateInstanceCount(String serverTemplateId, int expected) {
        ServerTemplate serverTemplate = kieServerControllerClient.getServerTemplate(serverTemplateId);
        assertThat(serverTemplate).isNotNull();
        assertThat(serverTemplate.getServerInstanceKeys()).isNotNull().hasSize(expected);
    }

    private void sendRESTCallsToUnavailableKieServer() {
        Client httpKieServerClient = new ResteasyClientBuilder()
                .establishConnectionTimeout(10, TimeUnit.SECONDS)
                .socketTimeout(10, TimeUnit.SECONDS)
                .register(new Authenticator(deploymentScenario.getKieServerDeployment().getUsername(),
                                deploymentScenario.getKieServerDeployment().getPassword()))
                .build();

        Response response = null;

        try {
            URL url = new URL(deploymentScenario.getKieServerDeployment().getUrl().orElseGet(deploymentScenario.getKieServerDeployment().getSecureUrl()::get), KIE_CONTAINERS_REQUEST_URL);
            WebTarget target = httpKieServerClient.target(url.toString());
            response = target.request().get();
            assertThat(response.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        } catch (ProcessingException e) {
            validateProcessingExceptionCausedBySocketTimeoutException(e);
        } catch (Exception e) {
            throw new RuntimeException("Error creating list container request.", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Check that ProcessingException is caused by SocketTimeoutException, otherwise throw the ProcessingException.
     * @param e ProcessingException
     */
    private void validateProcessingExceptionCausedBySocketTimeoutException(ProcessingException e) {
        if (e.getCause() instanceof SocketTimeoutException) {
            logger.debug("ProcessingException caused by SocketTimeoutException, indicates that Kie server is unavailable.");
        } else {
            throw e;
        }
    }
}
