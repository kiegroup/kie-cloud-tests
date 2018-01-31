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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerDatabaseScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class KieServerHttpsIntegrationTest extends AbstractCloudIntegrationTest<DeploymentScenario> {

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public DeploymentScenario kieServerScenario;

    private static final Marshaller marshaller
            = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(), MarshallingFormat.JAXB, KieServerHttpsIntegrationTest.class.getClassLoader());

    private static final Logger logger = LoggerFactory.getLogger(KieServerHttpsIntegrationTest.class);

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        WorkbenchKieServerScenario workbenchKieServerScenario = deploymentScenarioFactory.getWorkbenchKieServerScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
        WorkbenchKieServerDatabaseScenario workbenchKieServerDatabaseScenario = deploymentScenarioFactory.getWorkbenchKieServerDatabaseScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();

        DeploymentSettings kieServerSettings = deploymentScenarioFactory.getKieServerSettingsBuilder()
                .withMavenRepoUrl(MavenConstants.getMavenRepoUrl())
                .withMavenRepoUser(MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
        GenericScenario kieServerScenario = deploymentScenarioFactory.getGenericScenarioBuilder()
                .withKieServer(kieServerSettings)
                .build();

        DeploymentSettings kieServerS2ISettings = deploymentScenarioFactory.getKieServerHttpsS2ISettingsBuilder()
                .withMavenRepoUrl(MavenConstants.getMavenRepoUrl())
                .withMavenRepoUser(MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
        GenericScenario kieServerS2Iscenario = deploymentScenarioFactory.getGenericScenarioBuilder()
                .withKieServer(kieServerS2ISettings)
                .build();

        return Arrays.asList(new Object[][]{
            {"Workbench + KIE Server", workbenchKieServerScenario},
            {"Workbench + KIE Server + Database", workbenchKieServerDatabaseScenario},
            {"KIE Server", kieServerScenario},
            {"KIE Server HTTPS S2I", kieServerS2Iscenario}
        });
    }

    @Override
    protected DeploymentScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return kieServerScenario;
    }

    @Test
    public void testKieServerInfo() {
        final KieServerDeployment kieServerDeployment = deploymentScenario.getKieServerDeployments().get(0);
        final CredentialsProvider credentialsProvider = HttpsUtils.createCredentialsProvider(kieServerDeployment.getUsername(),
                kieServerDeployment.getPassword());
        try (CloseableHttpClient httpClient = HttpsUtils.createHttpClient(credentialsProvider)) {
            try (CloseableHttpResponse response = httpClient.execute(serverInforRequest(kieServerDeployment))) {
                Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);

                final String responseContent = HttpsUtils.readResponseContent(response);
                ServiceResponse<KieServerInfo> kieServerInfoServiceResponse = marshaller.unmarshall(responseContent, ServiceResponse.class);
                KieServerInfo kieServerInfo = kieServerInfoServiceResponse.getResult();
                Assertions.assertThat(kieServerInfo.getCapabilities()).contains(KieServerConstants.CAPABILITY_BRM);
            }
        } catch (Exception e) {
            logger.error("Unable to connect to KIE server REST API", e);
            throw new RuntimeException("Unable to connect to KIE server REST API", e);
        }
    }

    @Test
    public void testDeployContainer() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());

        final KieServerDeployment kieServerDeployment = deploymentScenario.getKieServerDeployments().get(0);
        final CredentialsProvider credentialsProvider = HttpsUtils.createCredentialsProvider(kieServerDeployment.getUsername(),
                kieServerDeployment.getPassword());
        try (CloseableHttpClient httpClient = HttpsUtils.createHttpClient(credentialsProvider)) {
            try (CloseableHttpResponse response = httpClient.execute(createContainerRequest(kieServerDeployment, CONTAINER_ID, PROJECT_GROUP_ID, DEFINITION_PROJECT_SNAPSHOT_NAME, DEFINITION_PROJECT_SNAPSHOT_VERSION))) {
                Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_CREATED);
            }

            try (CloseableHttpResponse response = httpClient.execute(getContainersRequest(kieServerDeployment))) {
                Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);

                final String responseContent = HttpsUtils.readResponseContent(response);
                final List<String> containers = parseListContainersResponse(responseContent);
                Assertions.assertThat(containers).contains(CONTAINER_ID);
            }
        } catch (Exception e) {
            logger.error("Unable to connect to KIE server REST API", e);
            throw new RuntimeException("Unable to connect to KIE server REST API", e);
        }
    }

    private HttpGet serverInforRequest(KieServerDeployment kieServerDeployment) {
        try {
            final URL url = new URL(kieServerDeployment.getSecureUrl(), KIE_SERVER_INFO_REST_REQUEST_URL);
            final HttpGet request = new HttpGet(url.toString());

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error creating request for KIE server info", e);
        }
    }

    private HttpPut createContainerRequest(KieServerDeployment kieServerDeployment, String containerName, String groupId, String artifactId, String version) {
        try {
            final URL url = new URL(kieServerDeployment.getSecureUrl(), KIE_CONTAINER_REQUEST_URL + "/" + containerName);
            final HttpPut request = new HttpPut(url.toString());
            request.setHeader("Content-Type", "application/xml");
            request.setEntity(new StringEntity(createContainerRequestContent(containerName, groupId, artifactId, version)));

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
        }
    }

    private HttpGet getContainersRequest(KieServerDeployment kieServerDeployment) {
        try {
            final URL url = new URL(kieServerDeployment.getSecureUrl(), KIE_CONTAINER_REQUEST_URL);
            final HttpGet request = new HttpGet(url.toString());

            return request;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating list container request", e);
        }
    }

    private List<String> parseListContainersResponse(String response) {
        ServiceResponse<KieContainerResourceList> serviceResponse = marshaller.unmarshall(response, ServiceResponse.class);
        List<KieContainerResource> kieContainerResourceList = serviceResponse.getResult().getContainers();
        return kieContainerResourceList.stream()
                .filter(c -> c.getStatus() == KieContainerStatus.STARTED)
                .map(c -> c.getContainerId())
                .collect(Collectors.toList());
    }

    private String createContainerRequestContent(String containerName, String groupId, String artifactId, String version) {
        KieContainerResource kieContainerResource = new KieContainerResource();
        kieContainerResource.setReleaseId(new ReleaseId(PROJECT_GROUP_ID, DEFINITION_PROJECT_SNAPSHOT_NAME, DEFINITION_PROJECT_SNAPSHOT_VERSION));
        String requestContent = marshaller.marshall(kieContainerResource);

        return requestContent;
    }
}
