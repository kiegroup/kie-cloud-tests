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
import java.util.stream.StreamSupport;
import javax.net.ssl.HttpsURLConnection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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

    private static final DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();
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
        final URL url = workbenchWithKieServerScenario.getWorkbenchDeployment().getSecureUrl();
        logger.debug("Test login screen on url {}", url.toString());
        final SSLConnectionSocketFactory sslsf = getSSLConnectionSocketFactory();

        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            final HttpGet httpGet = new HttpGet(url.toString());
            try (final CloseableHttpResponse response = httpClient.execute(httpGet)) {
                // Test that login screen is available
                Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);
                try (final InputStream inputStream = response.getEntity().getContent()) {
                    final String responseContent = IOUtils.toString(inputStream, "UTF-8");
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
            SSLConnectionSocketFactory sslsf = getSSLConnectionSocketFactory();
            CredentialsProvider credentialsProvider = getWorkbenchCredentialsProvider();
            try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCredentialsProvider(credentialsProvider).build()) {
                // Create organizational unit using REST API
                Assertions.assertThat(createOrganizationalUnit(ORGANIZATION_UNIT_NAME, httpClient)).isTrue();

                // Check that organizational unit exists
                Assertions.assertThat(organizationalUnitExists(ORGANIZATION_UNIT_NAME, httpClient)).isTrue();
            }
        } catch (Exception e) {
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
            try (final InputStream inputStream = response.getEntity().getContent()) {
                String responseContent = IOUtils.toString(inputStream, "UTF-8");
                Assertions.assertThat(responseContent).isNotEmpty();

                Gson gson = new Gson();
                JsonArray orgUnitsJson = gson.fromJson(responseContent, JsonArray.class);
                return StreamSupport.stream(orgUnitsJson.spliterator(), false)
                        .map(x -> x.getAsJsonObject())
                        .map(o -> o.get("name").getAsString())
                        .anyMatch(s -> s.equals(name));
            }
        } catch (Exception e) {
            throw new RuntimeException("Can not obtain list of organizational units", e);
        }
    }

    private HttpPost organizationalUnitCreateRequest(String name) {
        try {
            final URL url = new URL(workbenchWithKieServerScenario.getWorkbenchDeployment().getSecureUrl(), organizationalUnitRestRequest);
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
            final URL url = new URL(workbenchWithKieServerScenario.getWorkbenchDeployment().getSecureUrl(), organizationalUnitRestRequest);
            final HttpGet request = new HttpGet(url.toString());
            request.setHeader("Content-Type", "application/json");

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error in creating request for list of organizational units", e);
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

            return sslsf;
        } catch (Exception e) {
            throw new RuntimeException("Error in SSL setup", e);
        }
    }
}
