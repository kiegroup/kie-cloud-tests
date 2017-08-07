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

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.tools.ant.filters.StringInputStream;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class KieServerHttpsIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchWithKieServerScenario> {

    private static final String CONTAINER_ID = "cont-id";
    private static final String PROJECT_GROUP_ID = "org.kie.server.testing";
    private static final String PROJECT_NAME = "definition-project-snapshot";
    private static final String PROJECT_VERSION = "1.0.0-SNAPSHOT";

    private static final String KIE_SERVER_INFO_REST_REQUEST_URL = "services/rest/server";
    private static final String KIE_CONTAINER_REQUEST_URL = "services/rest/server/containers";
    private static final String KIE_SERVER_INFO_TEXT = "<kie-server-info>";

    private static final Logger logger = LoggerFactory.getLogger(KieServerHttpsIntegrationTest.class);

    @Override protected WorkbenchWithKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchWithKieServerScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
    }

    @Test
    public void testKieServerInfo() {
        final CredentialsProvider credentialsProvider = HttpsUtils.createCredentialsProvider(deploymentScenario.getKieServerDeployment().getUsername(),
                deploymentScenario.getKieServerDeployment().getPassword());
        try (CloseableHttpClient httpClient = HttpsUtils.createHttpClient(credentialsProvider)) {
            try (CloseableHttpResponse response = httpClient.execute(serverInforRequest())) {
                Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);

                final String responseContent = HttpsUtils.readResponseContent(response);
                Assertions.assertThat(responseContent).contains(KIE_SERVER_INFO_TEXT);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect to KIE server REST API", e);
        }
    }

    @Test
    public void testDeployContainer() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());

        final CredentialsProvider credentialsProvider = HttpsUtils.createCredentialsProvider(deploymentScenario.getKieServerDeployment().getUsername(),
                deploymentScenario.getKieServerDeployment().getPassword());
        try (CloseableHttpClient httpClient = HttpsUtils.createHttpClient(credentialsProvider)) {
            try (CloseableHttpResponse response = httpClient.execute(createContainerRequest(CONTAINER_ID, PROJECT_GROUP_ID, PROJECT_NAME, PROJECT_VERSION))) {
                Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_CREATED);
            }

            try (CloseableHttpResponse response = httpClient.execute(getContainersRequest())) {
                Assertions.assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpsURLConnection.HTTP_OK);

                final String responseContent = HttpsUtils.readResponseContent(response);
                final List<String> containers = parseListContainersResponse(responseContent);
                Assertions.assertThat(containers).contains(CONTAINER_ID);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect to KIE server REST API", e);
        }
    }

    private HttpGet serverInforRequest() {
        try {
            final URL url = new URL(deploymentScenario.getKieServerDeployment().getSecureUrl(), KIE_SERVER_INFO_REST_REQUEST_URL);
            final HttpGet request = new HttpGet(url.toString());

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error creating request for KIE server info", e);
        }
    }

    private HttpPut createContainerRequest(String containerName, String groupId, String artifactId, String version) {
        try {
            final URL url = new URL(deploymentScenario.getKieServerDeployment().getSecureUrl(), KIE_CONTAINER_REQUEST_URL + "/" + containerName);
            final HttpPut request = new HttpPut(url.toString());
            request.setHeader("Content-Type", "application/xml");
            request.setEntity(new StringEntity(createContainerRequestContent(containerName, groupId, artifactId, version)));

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
        }
    }

    private HttpGet getContainersRequest() {
        try {
            final URL url = new URL(deploymentScenario.getKieServerDeployment().getSecureUrl(), KIE_CONTAINER_REQUEST_URL);
            final HttpGet request = new HttpGet(url.toString());

            return request;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating list container request", e);
        }
    }

    private List<String> parseListContainersResponse(String response) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(new StringInputStream(response));
            final XPathFactory xPathfactory = XPathFactory.newInstance();
            final XPath xpath = xPathfactory.newXPath();
            final XPathExpression expr = xpath.compile("/response/kie-containers/kie-container[@status='STARTED']/@container-id");

            final NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            final List<String> containers = IntStream.range(0, nodeList.getLength())
                    .mapToObj(nodeList::item)
                    .map(t -> t.getTextContent())
                    .collect(Collectors.toList());
            return containers;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing list of containers", e);
        }
    }

    private String createContainerRequestContent(String containerName, String groupId, String artifactId, String version) {
        try {
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            final Element rootElement = document.createElement("kie-container");
            rootElement.setAttribute("container-id", containerName);
            document.appendChild(rootElement);

            final Element kieContainerElement = document.createElement("release-id");
            rootElement.appendChild(kieContainerElement);

            final Element artifactIdElement = document.createElement("artifact-id");
            artifactIdElement.setTextContent(artifactId);
            kieContainerElement.appendChild(artifactIdElement);

            final Element groupIdElement = document.createElement("group-id");
            groupIdElement.setTextContent(groupId);
            kieContainerElement.appendChild(groupIdElement);

            final Element versionElement = document.createElement("version");
            versionElement.setTextContent(version);
            kieContainerElement.appendChild(versionElement);

            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();

            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            final String requestContent = writer.getBuffer().toString();
            return requestContent;
        } catch (Exception e) {
            throw new RuntimeException("Error creating request content", e);
        }
    }
}
