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

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.maven.MavenDeployer;
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

import cz.xtf.http.HttpClient;
import cz.xtf.tuple.Tuple.Pair;

public class HttpsKieServerTestProvider {

    private static final Logger logger = LoggerFactory.getLogger(HttpsKieServerTestProvider.class);

    private static final Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JAXB, HttpsKieServerTestProvider.class.getClassLoader());

    private static final String KIE_SERVER_INFO_REST_REQUEST_URL = "services/rest/server";
    private static final String KIE_CONTAINERS_REQUEST_URL = KIE_SERVER_INFO_REST_REQUEST_URL + "/containers";

    static {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/hello-rules-snapshot").getFile());
    }

    public static void testKieServerInfo(KieServerDeployment kieServerDeployment, boolean ssoScenario) {
        try {
            Pair<String, Integer> responseAndCode;
            String url = serverInforRequestUrl(kieServerDeployment, ssoScenario);

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

    public static void testDeployContainer(KieServerDeployment kieServerDeployment, boolean ssoScenario) {
        String containerId = "testDeployContainer";
        try {
            String urlPut = containerRequestUrl(kieServerDeployment, containerId, ssoScenario);

            logger.debug("Test get Kie Server containers on url {}", urlPut);
            Pair<String, Integer> responseAndCodePut = HttpClient.put(urlPut)
                    .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                    .addHeader("Content-Type", "application/xml")
                    .data(createContainerRequestContent(Kjar.HELLO_RULES_SNAPSHOT), ContentType.TEXT_XML)
                    .preemptiveAuth()
                    .responseAndCode();

            assertThat(responseAndCodePut.getSecond()).isEqualTo(HttpsURLConnection.HTTP_CREATED);
            assertThat(responseAndCodePut.getFirst()).contains("Container " + containerId + " successfully created");

            String urlGet = getContainersRequestUrl(kieServerDeployment, ssoScenario);

            logger.debug("Test get Kie Server containers on url {}", urlGet);
            Pair<String, Integer> responseAndCodeGet = HttpClient.get(urlGet)
                    .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                    .preemptiveAuth()
                    .responseAndCode();

            assertThat(responseAndCodeGet.getSecond()).isEqualTo(HttpsURLConnection.HTTP_OK);
            final List<String> containers = parseListContainersResponse(responseAndCodeGet.getFirst());
            assertThat(containers).contains(containerId);

            String urlDisploseContainer = containerRequestUrl(kieServerDeployment, containerId, ssoScenario);

            logger.debug("Test dispose Kie Server containers on url {}", urlDisploseContainer);
            Pair<String, Integer> responseAndCodeDelete = HttpClient.delete(urlDisploseContainer)
                    .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                    .addHeader("Content-Type", "application/xml")
                    .preemptiveAuth()
                    .responseAndCode();

            assertThat(responseAndCodeDelete.getSecond()).isEqualTo(HttpsURLConnection.HTTP_OK);
        } catch (Exception e) {
            logger.error("Unable to connect to KIE server REST API", e);
            throw new RuntimeException("Unable to connect to KIE server REST API", e);
        }
    }

    private static String serverInforRequestUrl(KieServerDeployment kieServerDeployment, boolean ssoScenario) {
        try {
            if (ssoScenario) {
                return createSSOEnvVariable(kieServerDeployment.getSecureUrl().toString()) + "/" + KIE_SERVER_INFO_REST_REQUEST_URL;
            } else {
                final URL url = new URL(kieServerDeployment.getSecureUrl(), KIE_SERVER_INFO_REST_REQUEST_URL);
                return url.toString();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
        }
    }

    private static String containerRequestUrl(KieServerDeployment kieServerDeployment, String containerName, boolean ssoScenario) {
        try {
            if (ssoScenario) {
                return createSSOEnvVariable(kieServerDeployment.getSecureUrl().toString()) + "/" + KIE_CONTAINERS_REQUEST_URL + "/" + containerName;
            } else {
                final URL url = new URL(kieServerDeployment.getSecureUrl(), KIE_CONTAINERS_REQUEST_URL + "/" + containerName);
                return url.toString();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
        }
    }

    private static String getContainersRequestUrl(KieServerDeployment kieServerDeployment, boolean ssoScenario) {
        try {
            if (ssoScenario) {
                return createSSOEnvVariable(kieServerDeployment.getSecureUrl().toString()) + "/" + KIE_CONTAINERS_REQUEST_URL;
            } else {
                final URL url = new URL(kieServerDeployment.getSecureUrl(), KIE_CONTAINERS_REQUEST_URL);
                return url.toString();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating request for creating of KIE server container", e);
        }
    }

    private static String createContainerRequestContent(Kjar kjar) {
        ReleaseId releasedId = new ReleaseId(kjar.getGroupId(), kjar.getName(), kjar.getVersion());
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
