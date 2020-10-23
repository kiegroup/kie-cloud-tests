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

package org.kie.cloud.integrationtests.testproviders;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import cz.xtf.client.Http;
import cz.xtf.client.HttpResponseParser;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.KjarDeployer;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.tests.common.client.util.Kjar;
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

import static org.assertj.core.api.Assertions.assertThat;

public class HttpsKieServerTestProvider {

    private static final Logger logger = LoggerFactory.getLogger(HttpsKieServerTestProvider.class);

    private static final Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JAXB, HttpsKieServerTestProvider.class.getClassLoader());

    private static final String KIE_SERVER_INFO_REST_REQUEST_URL = "services/rest/server";
    private static final String KIE_CONTAINERS_REQUEST_URL = KIE_SERVER_INFO_REST_REQUEST_URL + "/containers";

    private HttpsKieServerTestProvider() {}

    /**
     * Create provider instance
     *
     * @return provider instance
     */
    public static HttpsKieServerTestProvider create() {
        return create(null);
    }

    /**
     * Create provider instance and init it with given environment
     *
     * @param environment if not null, initialize this provider with the environment
     *
     * @return provider instance
     */
    public static HttpsKieServerTestProvider create(DeploymentScenario<?> deploymentScenario) {
        HttpsKieServerTestProvider provider = new HttpsKieServerTestProvider();
        if (Objects.nonNull(deploymentScenario)) {
            provider.init(deploymentScenario);
        }
        return provider;
    }

    private void init(DeploymentScenario<?> deploymentScenario) {
        KjarDeployer.create(Kjar.HELLO_RULES_SNAPSHOT).deploy(deploymentScenario.getMavenRepositoryDeployment());
    }

    @SuppressWarnings("unchecked")
    public void testKieServerInfo(KieServerDeployment kieServerDeployment, boolean ssoScenario) {
        try {
            String url = serverInforRequestUrl(kieServerDeployment, ssoScenario);

            logger.debug("Test Kie Server info on url {}", url);
            HttpResponseParser responseAndCode = Http.get(url)
                    .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                    .preemptiveAuth()
                    .trustAll()
                    .execute();

            assertThat(responseAndCode.code()).isEqualTo(HttpsURLConnection.HTTP_OK);
            ServiceResponse<KieServerInfo> kieServerInfoServiceResponse = marshaller.unmarshall(responseAndCode.response(), ServiceResponse.class);
            KieServerInfo kieServerInfo = kieServerInfoServiceResponse.getResult();
            Assertions.assertThat(kieServerInfo.getCapabilities()).contains(KieServerConstants.CAPABILITY_BRM);
        } catch (Exception e) {
            logger.error("Unable to connect to KIE server REST API", e);
            throw new RuntimeException("Unable to connect to KIE server REST API", e);
        }
    }

    public void testDeployContainer(KieServerDeployment kieServerDeployment, boolean ssoScenario) {
        String containerId = "testDeployContainer";
        try {
            String urlPut = containerRequestUrl(kieServerDeployment, containerId, ssoScenario);

            logger.debug("Test get Kie Server containers on url {}", urlPut);
            HttpResponseParser responseAndCodePut = Http.put(urlPut)
                    .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                    .header("Content-Type", "application/xml")
                    .data(createContainerRequestContent(Kjar.HELLO_RULES_SNAPSHOT), ContentType.TEXT_XML)
                    .preemptiveAuth()
                    .trustAll()
                    .execute();

            assertThat(responseAndCodePut.code()).isEqualTo(HttpsURLConnection.HTTP_CREATED);
            assertThat(responseAndCodePut.response()).contains("Container " + containerId + " successfully created");

            kieServerDeployment.waitForContainerRespin();

            String urlGet = getContainersRequestUrl(kieServerDeployment, ssoScenario);

            logger.debug("Test get Kie Server containers on url {}", urlGet);
            HttpResponseParser responseAndCodeGet = Http.get(urlGet)
                    .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                    .preemptiveAuth()
                    .trustAll()
                    .execute();

            assertThat(responseAndCodeGet.code()).isEqualTo(HttpsURLConnection.HTTP_OK);
            final List<String> containers = parseListContainersResponse(responseAndCodeGet.response());
            assertThat(containers).contains(containerId);

            String urlDisploseContainer = containerRequestUrl(kieServerDeployment, containerId, ssoScenario);

            logger.debug("Test dispose Kie Server containers on url {}", urlDisploseContainer);
            HttpResponseParser responseAndCodeDelete = Http.delete(urlDisploseContainer)
                    .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                    .header("Content-Type", "application/xml")
                    .preemptiveAuth()
                    .trustAll()
                    .execute();

            assertThat(responseAndCodeDelete.code()).isEqualTo(HttpsURLConnection.HTTP_OK);

            kieServerDeployment.waitForContainerRespin();
        } catch (Exception e) {
            logger.error("Unable to connect to KIE server REST API", e);
            throw new RuntimeException("Unable to connect to KIE server REST API", e);
        }
    }

    private String serverInforRequestUrl(KieServerDeployment kieServerDeployment, boolean ssoScenario) {
        try {
            if (ssoScenario) {
                return createSSOEnvVariable(kieServerDeployment.getSecureUrl().get().toString()) + "/" + KIE_SERVER_INFO_REST_REQUEST_URL;
            } else {
                final URL url = new URL(kieServerDeployment.getSecureUrl().get(), KIE_SERVER_INFO_REST_REQUEST_URL);
                return url.toString();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
        }
    }

    private static String containerRequestUrl(KieServerDeployment kieServerDeployment, String containerName, boolean ssoScenario) {
        try {
            if (ssoScenario) {
                return createSSOEnvVariable(kieServerDeployment.getSecureUrl().get().toString()) + "/" + KIE_CONTAINERS_REQUEST_URL + "/" + containerName;
            } else {
                final URL url = new URL(kieServerDeployment.getSecureUrl().get(), KIE_CONTAINERS_REQUEST_URL + "/" + containerName);
                return url.toString();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
        }
    }

    private static String getContainersRequestUrl(KieServerDeployment kieServerDeployment, boolean ssoScenario) {
        try {
            if (ssoScenario) {
                return createSSOEnvVariable(kieServerDeployment.getSecureUrl().get().toString()) + "/" + KIE_CONTAINERS_REQUEST_URL;
            } else {
                final URL url = new URL(kieServerDeployment.getSecureUrl().get(), KIE_CONTAINERS_REQUEST_URL);
                return url.toString();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
        }
    }

    private static String createContainerRequestContent(Kjar kjar) {
        ReleaseId releasedId = new ReleaseId(kjar.getGroupId(), kjar.getArtifactName(), kjar.getVersion());
        KieContainerResource kieContainerResource = new KieContainerResource(releasedId);
        String requestContent = marshaller.marshall(kieContainerResource);

        return requestContent;
    }

    private static List<String> parseListContainersResponse(String response) {
        ServiceResponse<KieContainerResourceList> serviceResponse = marshaller.unmarshall(response, ServiceResponse.class);
        List<KieContainerResource> kieContainerResourceList = serviceResponse.getResult().getContainers();
        return kieContainerResourceList.stream()
                .filter(c -> c.getStatus() == KieContainerStatus.STARTED)
                .map(c -> c.getContainerId())
                .collect(Collectors.toList());
    }

    //RHPAM-1336
    private static String createSSOEnvVariable(String url) {
        String[] urlParts = url.split(":");
        return urlParts[0] + ":" + urlParts[1];
    }
}
