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

import java.io.IOException;
import java.net.URL;
import java.util.stream.StreamSupport;
import javax.net.ssl.HttpsURLConnection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchHttpsIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchWithKieServerScenario> {

    private static final String organizationalUnitRestRequest = "rest/organizationalunits";
    private static final String ORGANIZATION_UNIT_NAME = "myOrgUnit";
    private static final String WORKBENCH_LOGIN_SCREEN_TEXT = "Sign In";

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchHttpsIntegrationTest.class);

    @Override
    protected WorkbenchWithKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchWithKieServerScenarioBuilder().build();
    }

    @Test
    public void testLoginScreen() throws InterruptedException {
        final URL url = deploymentScenario.getWorkbenchDeployment().getSecureUrl();
        logger.debug("Test login screen on url {}", url.toString());

        try (CloseableHttpClient httpClient = HttpsUtils.createHttpClient()) {
            final HttpGet httpGet = new HttpGet(url.toString());
            try (final CloseableHttpResponse response = httpClient.execute(httpGet)) {
                // Test that login screen is available
                Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);

                String responseContent = HttpsUtils.readResponseContent(response);
                Assertions.assertThat(responseContent).contains(WORKBENCH_LOGIN_SCREEN_TEXT);
            }
        } catch (IOException e) {
            Assertions.fail("Error in downloading workbench login screen using secure connection", e);
        }
    }

    @Test
    public void testSecureRest() {
        try {
            CredentialsProvider credentialsProvider = HttpsUtils.createCredentialsProvider(deploymentScenario.getWorkbenchDeployment().getUsername(),
                    deploymentScenario.getWorkbenchDeployment().getPassword());
            try (CloseableHttpClient httpClient = HttpsUtils.createHttpClient(credentialsProvider)) {
                // Create organizational unit using REST API
                Assertions.assertThat(createOrganizationalUnit(ORGANIZATION_UNIT_NAME, httpClient)).isTrue();

                // Check that organizational unit exists
                Assertions.assertThat(organizationalUnitExists(ORGANIZATION_UNIT_NAME, httpClient)).isTrue();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to workbench REST API", e);
        }
    }

    private boolean createOrganizationalUnit(String name, CloseableHttpClient httpClient) {
        try (CloseableHttpResponse response = httpClient.execute(organizationalUnitCreateRequest(name))) {
            return (response.getStatusLine().getStatusCode() == HttpsURLConnection.HTTP_ACCEPTED);
        } catch (Exception e) {
            throw new RuntimeException("Error creating new organizational unit", e);
        }
    }

    private boolean organizationalUnitExists(String name, CloseableHttpClient httpClient) {
        try (final CloseableHttpResponse response = httpClient.execute(organizationalUnitsListRequest(name))) {
            Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);

            String responseContent = HttpsUtils.readResponseContent(response);
            Assertions.assertThat(responseContent).isNotEmpty();
            Gson gson = new Gson();
            JsonArray orgUnitsJson = gson.fromJson(responseContent, JsonArray.class);
            return StreamSupport.stream(orgUnitsJson.spliterator(), false)
                    .map(x -> x.getAsJsonObject())
                    .map(o -> o.get("name").getAsString())
                    .anyMatch(s -> s.equals(name));
        } catch (Exception e) {
            throw new RuntimeException("Can not obtain list of organizational units", e);
        }
    }

    private HttpPost organizationalUnitCreateRequest(String name) {
        try {
            final URL url = new URL(deploymentScenario.getWorkbenchDeployment().getSecureUrl(), organizationalUnitRestRequest);
            final HttpPost request = new HttpPost(url.toString());
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(createOrganizationalUnitJson(name)));

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error in creating rest request for new organizational unit", e);
        }
    }

    private HttpGet organizationalUnitsListRequest(String name) {
        try {
            final URL url = new URL(deploymentScenario.getWorkbenchDeployment().getSecureUrl(), organizationalUnitRestRequest);
            final HttpGet request = new HttpGet(url.toString());
            request.setHeader("Content-Type", "application/json");

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error in creating request for list of organizational units", e);
        }
    }

    private String createOrganizationalUnitJson(String name) {
        JsonObject organizationalUnit = new JsonObject();
        organizationalUnit.addProperty("name", ORGANIZATION_UNIT_NAME);
        organizationalUnit.addProperty("owner", deploymentScenario.getWorkbenchDeployment().getUsername());

        Gson gson = new Gson();
        return gson.toJson(organizationalUnit);
    }
}
