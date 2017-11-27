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

import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.common.client.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.common.rest.Authenticator;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.management.client.KieServerMgmtControllerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class KieServerWithWorkbenchSurvivalIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchWithKieServerScenario> {

    private KieServerMgmtControllerClient kieServerMgmtControllerClient;

    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected QueryServicesClient queryServicesClient;

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithWorkbenchSurvivalIntegrationTest.class);

    @Override
    protected WorkbenchWithKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchWithKieServerScenarioBuilder().build();
    }

    @Before
    public void setUp() {
        WorkbenchDeployment workbenchDeployment = deploymentScenario.getWorkbenchDeployment();
        gitProvider.createGitRepository(workbenchDeployment.getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER).getFile());
        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(workbenchDeployment.getNamespace()), workbenchDeployment, DEFINITION_PROJECT_NAME);

        kieServerMgmtControllerClient = KieServerControllerClientProvider.getKieServerMgmtControllerClient(deploymentScenario.getWorkbenchDeployment());

        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
        queryServicesClient = KieServerClientProvider.getQueryClient(deploymentScenario.getKieServerDeployment());
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
        WorkbenchUtils.saveContainerSpec(kieServerMgmtControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, DEFINITION_PROJECT_NAME, DEFINITION_PROJECT_VERSION, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        logger.debug("Start process instance");
        Long signalPid = processServicesClient.startProcess(CONTAINER_ID, SIGNALTASK_PROCESS_ID);
        assertThat(signalPid).isNotNull().isGreaterThan(0L);
        assertThat(queryServicesClient.findProcessInstances(0, 10)).isNotNull().hasSize(1);

        scaleKieServerTo(0);
        checkServerTemplateInstanceCount(serverInfo.getServerId(), 0);

        logger.debug("Send REST request to Kie Server");
        sendRESTCallsToUnavailableKieServer();

        scaleKieServerTo(1);
        checkServerTemplateInstanceCount(serverInfo.getServerId(), 1);

        logger.debug("Start new process instance");
        Long newPid = processServicesClient.startProcess(CONTAINER_ID, SIGNALTASK_PROCESS_ID);
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
        assertThat(kieServerMgmtControllerClient.listServerTemplates()).hasSize(1);
        ServerTemplate serverTemplate = kieServerMgmtControllerClient.listServerTemplates().iterator().next();
        assertThat(serverTemplate.getId()).isEqualTo(expectedId);
        assertThat(serverTemplate.getName()).isEqualTo(expectedName);
    }

    protected void checkServerTemplateInstanceCount(String serverTemplateId, int expected) {
        ServerTemplate serverTemplate = kieServerMgmtControllerClient.getServerTemplate(serverTemplateId);
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
            URL url = new URL(deploymentScenario.getKieServerDeployment().getUrl(), KIE_CONTAINER_REQUEST_URL);
            WebTarget target = httpKieServerClient.target(url.toString());
            response = target.request().get();
            assertThat(response.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Error creating list container request.", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

}
