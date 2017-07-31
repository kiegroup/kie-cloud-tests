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

import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.maven.constants.MavenConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchHttpsIntegrationTest {

    private static final String organizationalUnitRestRequest = "rest/organizationalunits";
    private static final String ORGANIZATION_UNIT_NAME = "myOrgUnit";
    private static final String WORKBENCH_LOGIN_SCREEN_TEXT = "Sign In";

    private static DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();
    private WorkbenchWithKieServerScenario workbenchWithKieServerScenario;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchHttpsIntegrationTest.class);

    @Before
    public void setUp() {
        workbenchWithKieServerScenario = deploymentScenarioFactory.getWorkbenchWithKieServerScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
        workbenchWithKieServerScenario.deploy();
    }

    @After
    public void tearDown() {
        InstanceLogUtil.writeDeploymentLogs(workbenchWithKieServerScenario.getWorkbenchDeployment());
        InstanceLogUtil.writeDeploymentLogs(workbenchWithKieServerScenario.getKieServerDeployment());
        workbenchWithKieServerScenario.undeploy();
    }

    @Test
    public void testLoginScreen() throws InterruptedException {
        URL url = workbenchWithKieServerScenario.getWorkbenchDeployment().getSecureUrl();
        logger.debug("Test login screen on url {}", url.toString());
        SSLConnectionSocketFactory sslsf = getSSLConnectionSocketFactory();

        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpGet httpGet = new HttpGet(url.toString());
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                // Test that login screen is available
                Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);
                try (InputStream inputStream = response.getEntity().getContent()) {
                    String responseContent = IOUtils.toString(inputStream, "UTF-8");
                    Assertions.assertThat(responseContent).contains(WORKBENCH_LOGIN_SCREEN_TEXT);
                }
            }
        } catch (Exception e) {
            Assertions.fail("Error in downloading workbench login screen using secure connection", e);
        }
    }

    @Test
    public void testSecureRest() {
        try {
            URL url = workbenchWithKieServerScenario.getWorkbenchDeployment().getSecureUrl();
            logger.debug("Test REST API for workbench on url {}", url.toString());
            url = new URL(url, organizationalUnitRestRequest);
            SSLConnectionSocketFactory sslsf = getSSLConnectionSocketFactory();

            CredentialsProvider credentialsProvider = getWorkbenchCredentialsProvider();
            try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCredentialsProvider(credentialsProvider).build()) {
                // Create organizational unit using REST API
                HttpPost createOrgUnitRequest = new HttpPost(url.toString());
                createOrgUnitRequest.setHeader("Content-Type", "application/json");
                createOrgUnitRequest.setEntity(new StringEntity(createOrganizationalUnitJson(ORGANIZATION_UNIT_NAME)));
                try (CloseableHttpResponse response = httpClient.execute(createOrgUnitRequest)) {
                    Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_ACCEPTED);
                }

                // Check that organizational unit exists
                HttpGet getOrgUnitRequest = new HttpGet(url.toString());
                getOrgUnitRequest.setHeader("Content-Type", "application/json");
                try (CloseableHttpResponse response = httpClient.execute(getOrgUnitRequest)) {
                    Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);
                    try (InputStream inputStream = response.getEntity().getContent()) {
                        String responseContent = IOUtils.toString(inputStream, "UTF-8");
                        Assertions.assertThat(responseContent).isNotEmpty();

                        Gson gson = new Gson();
                        JsonArray organizationalUnits = gson.fromJson(responseContent, JsonArray.class);
                        boolean found = false;
                        for (JsonElement item : organizationalUnits) {
                            if (item.isJsonObject()) {
                                JsonObject organizationalUnit = item.getAsJsonObject();
                                if (organizationalUnit.get("name").getAsString().equals(ORGANIZATION_UNIT_NAME)) {
                                    found = true;
                                }
                            }
                        }

                        Assertions.assertThat(found).isTrue();
                    }
                }
            }
        } catch (Exception e) {
            Assertions.fail("Unable to connect to workbench REST API", e);
        }
    }

    private class TrustAllStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            return true;
        }
    }

    private CredentialsProvider getWorkbenchCredentialsProvider() {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(workbenchWithKieServerScenario.getWorkbenchDeployment().getUsername(),
                workbenchWithKieServerScenario.getWorkbenchDeployment().getPassword());
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);

        return provider;
    }

    private String createOrganizationalUnitJson(String name) {
        JsonObject organizationalUnit = new JsonObject();
        organizationalUnit.addProperty("name", ORGANIZATION_UNIT_NAME);
        organizationalUnit.addProperty("owner", workbenchWithKieServerScenario.getWorkbenchDeployment().getUsername());

        Gson gson = new Gson();
        return gson.toJson(organizationalUnit);
    }

    private SSLConnectionSocketFactory getSSLConnectionSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustAllStrategy());
            sslsf = new SSLConnectionSocketFactory(
                    builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            Assertions.fail("Error in SSL setup", e);
        }

        return sslsf;
    }
}
