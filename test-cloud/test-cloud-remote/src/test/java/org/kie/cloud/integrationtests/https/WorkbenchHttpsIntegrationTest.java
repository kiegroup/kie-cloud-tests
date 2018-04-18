/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.integrationtests.https;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.StreamSupport;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.common.util.HttpsUtils;
import org.kie.cloud.maven.constants.MavenConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class WorkbenchHttpsIntegrationTest extends AbstractCloudHttpsIntegrationTest<DeploymentScenario> {

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public DeploymentScenario workbenchScenario;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchHttpsIntegrationTest.class);

    private static final String SERVER_ID_PARAMETER = "server-id";
    private static final String SERVER_NAME_PARAMETER = "server-name";
    private static final String SERVER_TEMPLATE_PARAMETER = "server-template";

    private static final String SERVER_ID = "KieServerId";
    private static final String SERVER_NAME = "KieServer";

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        WorkbenchKieServerPersistentScenario workbenchKieServerPersistentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder().build();

        WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario workbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario = deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();

        return Arrays.asList(new Object[][]{
            {"Workbench + KIE Server - Persistent", workbenchKieServerPersistentScenario},
            {"Workbench + Smart router + 2 KIE Servers + 2 Databases", workbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario},
           });
    }

    @Override
    protected DeploymentScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return workbenchScenario;
    }

    @Test
    public void testLoginScreen() throws InterruptedException {
        for (final WorkbenchDeployment workbenchDeployment : deploymentScenario.getWorkbenchDeployments()) {
            final URL url = workbenchDeployment.getSecureUrl();
            logger.debug("Test login screen on url {}", url.toString());

            final HttpGet httpGet = new HttpGet(url.toString());
            try (CloseableHttpClient httpClient = HttpsUtils.createHttpClient();
                final CloseableHttpResponse response = httpClient.execute(httpGet)) {
                // Test that login screen is available
                assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);

                String responseContent = HttpsUtils.readResponseContent(response);
                assertThat(responseContent).contains(WORKBENCH_LOGIN_SCREEN_TEXT);
            } catch (IOException e) {
                logger.error("Error in downloading workbench login screen using secure connection", e);
                fail("Error in downloading workbench login screen using secure connection", e);
            }
        }
    }

    @Test
    public void testSecureRest() {
        for (final WorkbenchDeployment workbenchDeployment : deploymentScenario.getWorkbenchDeployments()) {
            try {
                CredentialsProvider credentialsProvider = HttpsUtils.createCredentialsProvider(workbenchDeployment.getUsername(), workbenchDeployment.getPassword());
                try (CloseableHttpClient httpClient = HttpsUtils.createHttpClient(credentialsProvider)) {
                    // Create server template using REST API
                    assertThat(createServerTemplate(SERVER_ID, httpClient, workbenchDeployment.getSecureUrl())).isTrue();

                    // Check that server template exists
                    assertThat(serverTemplateExists(SERVER_ID, httpClient, workbenchDeployment.getSecureUrl())).isTrue();
                }
            } catch (IOException e) {
                logger.error("Unable to connect to workbench REST API", e);
                throw new RuntimeException("Unable to connect to workbench REST API", e);
            }
        }
    }

    private boolean createServerTemplate(String serverTemplateName, CloseableHttpClient httpClient, URL secureWorkbenchUrl) {
        try (CloseableHttpResponse response = httpClient.execute(serverTemplateCreateRequest(secureWorkbenchUrl, serverTemplateName))) {
            return (response.getStatusLine().getStatusCode() == HttpsURLConnection.HTTP_CREATED);
        } catch (Exception e) {
            throw new RuntimeException("Error creating new server template", e);
        }
    }

    private boolean serverTemplateExists(String serverTemplateName, CloseableHttpClient httpClient, URL secureWorkbenchUrl) {
        try (final CloseableHttpResponse response = httpClient.execute(serverTemplatesListRequest(secureWorkbenchUrl))) {
            assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);

            String responseContent = HttpsUtils.readResponseContent(response);
            assertThat(responseContent).isNotEmpty();
            Gson gson = new Gson();
            JsonObject serverTemplatesJson = gson.fromJson(responseContent, JsonObject.class);
            JsonArray serverTemplatesJsonArray = serverTemplatesJson.get(SERVER_TEMPLATE_PARAMETER).getAsJsonArray();
            return StreamSupport.stream(serverTemplatesJsonArray.spliterator(), false)
                    .map(x -> x.getAsJsonObject())
                    .map(o -> o.get(SERVER_ID_PARAMETER).getAsString())
                    .anyMatch(s -> s.equals(serverTemplateName));
        } catch (Exception e) {
            throw new RuntimeException("Can not obtain list of server templates", e);
        }
    }

    private HttpPut serverTemplateCreateRequest(URL secureWorkbenchUrl, String serverTemplateName) {
        try {
            final URL url = new URL(secureWorkbenchUrl, "rest/controller/management/servers/" + serverTemplateName);
            final HttpPut request = new HttpPut(url.toString());
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(createServerTemplateJson(serverTemplateName)));

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error in creating rest request for new server template", e);
        }
    }

    private HttpGet serverTemplatesListRequest(URL secureWorkbenchUrl) {
        try {
            final URL url = new URL(secureWorkbenchUrl, "rest/controller/management/servers");
            final HttpGet request = new HttpGet(url.toString());
            request.setHeader("Content-Type", "application/json");

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error in creating request for list of server templates", e);
        }
    }

    private String createServerTemplateJson(String serverTemplateName) {
        JsonObject serverTemplate = new JsonObject();
        serverTemplate.addProperty(SERVER_ID_PARAMETER, serverTemplateName);
        serverTemplate.addProperty(SERVER_NAME_PARAMETER, SERVER_NAME);

        Gson gson = new Gson();
        return gson.toJson(serverTemplate);
    }
}
