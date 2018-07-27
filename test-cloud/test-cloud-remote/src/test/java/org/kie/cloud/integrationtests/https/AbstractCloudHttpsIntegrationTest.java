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
package org.kie.cloud.integrationtests.https;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.common.util.HttpsUtils;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCloudHttpsIntegrationTest<T extends DeploymentScenario> extends AbstractMethodIsolatedCloudIntegrationTest<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCloudHttpsIntegrationTest.class);

    @Before
    public void setUpHttpsRouter() {
        deploymentScenario.getKieServerDeployments().stream().forEach((kieServerDeployment) -> {
            waitForSecureRouter(kieServerDeployment.getSecureUrl(),
                    kieServerDeployment.getUsername(),
                    kieServerDeployment.getPassword());
        });
    }

    private void waitForSecureRouter(URL secureUrl, String username, String password) {
        Instant endTime = Instant.now().plus(Duration.of(15, ChronoUnit.SECONDS));
        logger.info("Waiting for router to expose secure url: {}", secureUrl.toString());

        while (Instant.now().isBefore(endTime)) {
            final CredentialsProvider credentialsProvider = HttpsUtils.createCredentialsProvider(username, password);

            try (CloseableHttpClient httpClient = HttpsUtils.createHttpClient(credentialsProvider);
                    CloseableHttpResponse response = httpClient.execute(createSecureInforRequest(secureUrl))) {
                if (response.getStatusLine().getStatusCode() == HttpsURLConnection.HTTP_OK) {
                    return;
                }
            } catch (SSLHandshakeException e) {
                logger.warn("Caught SSLHandshakeException. Try to connection again.");
            } catch (Exception e) {
                logger.error("Error waiting for router");
                throw new RuntimeException("Error waiting for router", e);
            }
            try {
                Thread.sleep(250L);
            } catch (InterruptedException ex) {
                logger.error("Error waiting for router");
                throw new RuntimeException("Error waiting for router", ex);
            }
        }
    }

    private static HttpGet createSecureInforRequest(URL secureUrl) {
        try {
            final URL requestUrl = new URL(secureUrl, KIE_SERVER_INFO_REST_REQUEST_URL);
            final HttpGet request = new HttpGet(requestUrl.toString());

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Error creating request for KIE server info", e);
        }
    }
}
