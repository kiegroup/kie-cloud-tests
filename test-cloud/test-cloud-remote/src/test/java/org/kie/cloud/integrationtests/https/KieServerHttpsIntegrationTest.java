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

import cz.xtf.http.HttpClient;
import cz.xtf.tuple.Tuple.Pair;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.scenario.KieServerWithDatabaseScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
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
public class KieServerHttpsIntegrationTest extends AbstractCloudHttpsIntegrationTest<DeploymentScenario> {

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public DeploymentScenario kieServerScenario;

    private static final Marshaller marshaller
            = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(), MarshallingFormat.JAXB, KieServerHttpsIntegrationTest.class.getClassLoader());

    private static final Logger logger = LoggerFactory.getLogger(KieServerHttpsIntegrationTest.class);

    private static final String SECURED_URL_PREFIX = "secured-";
    private static final String RANDOM_URL_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";

    private static final String BUSINESS_CENTRAL_NAME = "rhpamcentr";
    private static final String KIE_SERVER_NAME = "kieserver";

    private static final String BUSINESS_CENTRAL_HOSTNAME = BUSINESS_CENTRAL_NAME + DeploymentConstants.getDefaultDomainSuffix();
    private static final String KIE_SERVER_HOSTNAME = KIE_SERVER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario workbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario = deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();

        KieServerWithDatabaseScenario kieServerMySqlScenario = deploymentScenarioFactory.getKieServerWithMySqlScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();

        KieServerWithDatabaseScenario kieServerPostgreSqlScenario = deploymentScenarioFactory.getKieServerWithPostgreSqlScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();

        ClusteredWorkbenchKieServerDatabasePersistentScenario clusteredWorkbenchKieServerDatabasePersistentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();

        DeploymentSettings kieServerSettings = deploymentScenarioFactory.getKieServerSettingsBuilder()
                .withMavenRepoUrl(MavenConstants.getMavenRepoUrl())
                .withMavenRepoUser(MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
        GenericScenario kieServerScenario = deploymentScenarioFactory.getGenericScenarioBuilder()
                .withKieServer(kieServerSettings)
                .build();

        WorkbenchKieServerPersistentScenario workbenchKieServerPersistentSSOScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                .deploySSO(true)
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withHttpWorkbenchHostname(RANDOM_URL_PREFIX + BUSINESS_CENTRAL_HOSTNAME)
                .withHttpsWorkbenchHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + BUSINESS_CENTRAL_HOSTNAME)
                .withHttpKieServerHostname(RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                .withHttpsKieServerHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                .build();

        WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario ssoWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario = deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                .deploySSO(true)
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withHttpWorkbenchHostname(RANDOM_URL_PREFIX + "mon-" + BUSINESS_CENTRAL_HOSTNAME)
                .withHttpsWorkbenchHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + "mon-" + BUSINESS_CENTRAL_HOSTNAME)
                .withHttpKieServer1Hostname(RANDOM_URL_PREFIX + "mon-1-" + KIE_SERVER_HOSTNAME)
                .withHttpsKieServer1Hostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + "mon-1-" + KIE_SERVER_HOSTNAME)
                .withHttpKieServer2Hostname(RANDOM_URL_PREFIX + "mon-2-" + KIE_SERVER_HOSTNAME)
                .withHttpsKieServer2Hostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + "mon-2-" + KIE_SERVER_HOSTNAME)
                .build();

        return Arrays.asList(new Object[][]{
            {"Workbench + Smart router + 2 KIE Servers + 2 Databases", workbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario},
            {"Clustered Workbench + KIE Server + Database - Persistent", clusteredWorkbenchKieServerDatabasePersistentScenario},
            {"KIE Server", kieServerScenario},
            {"KIE Server + MySQL", kieServerMySqlScenario},
            {"KIE Server + PostgreSQL", kieServerPostgreSqlScenario},
            {"[SSO] Workbench + KIE Server - Persistent", workbenchKieServerPersistentSSOScenario},
            {"[SSO] Workbench + Smart router + 2 KIE Servers + 2 Databases", ssoWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario}
        });
    }

    @Override
    protected DeploymentScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return kieServerScenario;
    }

    @BeforeClass
    public static void buildKjar() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());
    }

    @Test
    public void testKieServerInfo() {
        for (final KieServerDeployment kieServerDeployment : deploymentScenario.getKieServerDeployments()) {
            try {
                Pair<String, Integer> responseAndCode;
                String url = serverInforRequestUrl(kieServerDeployment);

                logger.debug("Test Kie Server info on url {}", url);
                responseAndCode = HttpClient.get(url)
                        .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                        .preemptiveAuth()
                        .responseAndCode();

                assertThat(responseAndCode.getSecond()).isEqualTo(HttpsURLConnection.HTTP_OK);
                ServiceResponse<KieServerInfo> kieServerInfoServiceResponse = marshaller.unmarshall(responseAndCode.getFirst(), ServiceResponse.class);
                KieServerInfo kieServerInfo = kieServerInfoServiceResponse.getResult();
                Assertions.assertThat(kieServerInfo.getCapabilities()).contains(KieServerConstants.CAPABILITY_BRM);
            } catch (Exception e) {
                logger.error("Unable to connect to KIE server REST API", e);
                throw new RuntimeException("Unable to connect to KIE server REST API", e);
            }
        }
    }

    @Test
    public void testDeployContainer() {
        for (final KieServerDeployment kieServerDeployment : deploymentScenario.getKieServerDeployments()) {
            try {
                String urlPut = createContainerRequestUrl(kieServerDeployment, CONTAINER_ID);

                logger.debug("Test get Kie Server containers on url {}", urlPut);
                Pair<String, Integer> responseAndCodePut = HttpClient.put(urlPut)
                        .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                        .addHeader("Content-Type", "application/xml")
                        .data(createContainerRequestContent(PROJECT_GROUP_ID, DEFINITION_PROJECT_SNAPSHOT_NAME, DEFINITION_PROJECT_SNAPSHOT_VERSION), ContentType.TEXT_XML)
                        .preemptiveAuth()
                        .responseAndCode();

                assertThat(responseAndCodePut.getSecond()).isEqualTo(HttpsURLConnection.HTTP_CREATED);
                assertThat(responseAndCodePut.getFirst()).contains("Container " + CONTAINER_ID + " successfully created");

                String urlGet = getContainersRequestUrl(kieServerDeployment);

                logger.debug("Test get Kie Server containers on url {}", urlGet);
                Pair<String, Integer> responseAndCodeGet = HttpClient.get(urlGet)
                        .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                        .preemptiveAuth()
                        .responseAndCode();

                assertThat(responseAndCodeGet.getSecond()).isEqualTo(HttpsURLConnection.HTTP_OK);
                final List<String> containers = parseListContainersResponse(responseAndCodeGet.getFirst());
                Assertions.assertThat(containers).contains(CONTAINER_ID);
            } catch (Exception e) {
                logger.error("Unable to connect to KIE server REST API", e);
                throw new RuntimeException("Unable to connect to KIE server REST API", e);
            }
        }
    }

    //RHPAM-1336
    private String createSSOEnvVariable(String url) {
        String[] urlParts = url.split(":");
        return urlParts[0] + ":" + urlParts[1];
    }

    private String serverInforRequestUrl(KieServerDeployment kieServerDeployment) {
        try {
            if (testScenarioName.contains("SSO")) {
                return createSSOEnvVariable(kieServerDeployment.getSecureUrl().toString()) + "/" + KIE_SERVER_INFO_REST_REQUEST_URL;
            } else {
                final URL url = new URL(kieServerDeployment.getSecureUrl(), KIE_SERVER_INFO_REST_REQUEST_URL);
                return url.toString();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
        }
    }

    private String createContainerRequestUrl(KieServerDeployment kieServerDeployment, String containerName) {
        try {
            if (testScenarioName.contains("SSO")) {
                return createSSOEnvVariable(kieServerDeployment.getSecureUrl().toString()) + "/" + KIE_CONTAINERS_REQUEST_URL + "/" + containerName;
            } else {
                final URL url = new URL(kieServerDeployment.getSecureUrl(), KIE_CONTAINERS_REQUEST_URL + "/" + containerName);
                return url.toString();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
        }
    }

    private String getContainersRequestUrl(KieServerDeployment kieServerDeployment) {
        try {
            if (testScenarioName.contains("SSO")) {
                return createSSOEnvVariable(kieServerDeployment.getSecureUrl().toString()) + "/" + KIE_CONTAINERS_REQUEST_URL;
            } else {
                final URL url = new URL(kieServerDeployment.getSecureUrl(), KIE_CONTAINERS_REQUEST_URL);
                return url.toString();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
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

    private String createContainerRequestContent(String groupId, String artifactId, String version) {
        KieContainerResource kieContainerResource = new KieContainerResource();
        kieContainerResource.setReleaseId(new ReleaseId(groupId, artifactId, version));
        String requestContent = marshaller.marshall(kieContainerResource);

        return requestContent;
    }
}
