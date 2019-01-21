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
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.http.entity.ContentType;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.xtf.http.HttpClient;
import cz.xtf.tuple.Tuple.Pair;

public class HttpsWorkbenchTestProvider {

    private static final Logger logger = LoggerFactory.getLogger(HttpsWorkbenchTestProvider.class);

    private static final String WORKBENCH_LOGIN_SCREEN_TEXT = "Sign In";

    private static final String SERVER_ID_PARAMETER = "server-id";
    private static final String SERVER_NAME_PARAMETER = "server-name";
    private static final String SERVER_TEMPLATE_PARAMETER = "server-template";

    private static final String WORKBENCH_CONTROLLER_MANAGEMENT_REST_REQUEST_URL = "rest/controller/management/servers";

    public static void testLoginScreen(WorkbenchDeployment workbenchDeployment, boolean ssoScenario) {
        final URL url = workbenchDeployment.getSecureUrl().get();
        logger.debug("Test login screen on url {}", url.toString());

        try {
            if (ssoScenario) {
                String[] urlParts = url.toString().split(":");
                String urlString = urlParts[0] + ":" + urlParts[1];

                logger.debug("Test login screen on url {}", urlString);
                Pair<String, Integer> responseAndCode = HttpClient.get(urlString).responseAndCode();
                assertThat(responseAndCode.getSecond()).isEqualTo(HttpsURLConnection.HTTP_OK);
                assertThat(responseAndCode.getFirst()).contains(DeploymentConstants.getSsoRealm());

            } else {
                logger.debug("Test login screen on url {}", url.toString());
                Pair<String, Integer> responseAndCode = HttpClient.get(url.toString()).responseAndCode();
                assertThat(responseAndCode.getSecond()).isEqualTo(HttpsURLConnection.HTTP_OK);
                assertThat(responseAndCode.getFirst()).contains(WORKBENCH_LOGIN_SCREEN_TEXT);
            }

        } catch (IOException e) {
            logger.error("Error in downloading workbench login screen using secure connection", e);
            fail("Error in downloading workbench login screen using secure connection", e);
        }
    }

    public static void testControllerOperations(WorkbenchDeployment workbenchDeployment, boolean ssoScenario) {
        String serverId = "KieServerId";
        String serverName = "KieServerName";
        try {
            try {
                // Create server template using REST API
                Pair<String, Integer> responseCreateServerTemplate = HttpClient.put(serverTemplateRequestUrl(workbenchDeployment, serverId))
                        .basicAuth(workbenchDeployment.getUsername(), workbenchDeployment.getPassword())
                        .addHeader("Content-Type", "application/json")
                        .data(createServerTemplateJson(serverId, serverName), ContentType.APPLICATION_JSON)
                        .preemptiveAuth()
                        .responseAndCode();
                assertThat(responseCreateServerTemplate.getSecond()).isEqualTo(HttpsURLConnection.HTTP_CREATED);

                // Check that server template exists
                Pair<String, Integer> responseCheckServerTemplate = HttpClient.get(listServerTemplatesRequestUrl(workbenchDeployment))
                        .basicAuth(workbenchDeployment.getUsername(), workbenchDeployment.getPassword())
                        .addHeader("Accept", "application/json")
                        .preemptiveAuth()
                        .responseAndCode();
                assertThat(responseCheckServerTemplate.getSecond()).isEqualTo(HttpsURLConnection.HTTP_OK);
                verifyServerTemplateExists(serverId, responseCheckServerTemplate.getFirst());
            } finally {
                // Delete server template
                HttpClient.delete(serverTemplateRequestUrl(workbenchDeployment, serverId))
                        .basicAuth(workbenchDeployment.getUsername(), workbenchDeployment.getPassword())
                        .preemptiveAuth()
                        .code();
            }
        } catch (IOException e) {
            logger.error("Unable to connect to workbench REST API", e);
            throw new RuntimeException("Unable to connect to workbench REST API", e);
        }
    }

    private static void verifyServerTemplateExists(String serverTemplateId, String responseContent) {
        assertThat(responseContent).isNotEmpty();

        Gson gson = new Gson();
        JsonObject serverTemplatesJson = gson.fromJson(responseContent, JsonObject.class);
        JsonArray serverTemplatesJsonArray = serverTemplatesJson.get(SERVER_TEMPLATE_PARAMETER).getAsJsonArray();
        Stream<String> serverTemplateIds = StreamSupport.stream(serverTemplatesJsonArray.spliterator(), false)
                                                        .map(x -> x.getAsJsonObject())
                                                        .map(o -> o.get(SERVER_ID_PARAMETER).getAsString());
        assertThat(serverTemplateIds).contains(serverTemplateId);
    }

    private static String serverTemplateRequestUrl(WorkbenchDeployment workbenchDeployment, String serverTemplateId) {
        return workbenchDeployment.getSecureUrl().get() + "/" + WORKBENCH_CONTROLLER_MANAGEMENT_REST_REQUEST_URL + "/" + serverTemplateId;
    }

    private static String listServerTemplatesRequestUrl(WorkbenchDeployment workbenchDeployment) {
        return workbenchDeployment.getSecureUrl().get() + "/" + WORKBENCH_CONTROLLER_MANAGEMENT_REST_REQUEST_URL;
    }

    private static String createServerTemplateJson(String serverTemplateId, String serverTemplateName) {
        JsonObject serverTemplate = new JsonObject();
        serverTemplate.addProperty(SERVER_ID_PARAMETER, serverTemplateId);
        serverTemplate.addProperty(SERVER_NAME_PARAMETER, serverTemplateName);

        Gson gson = new Gson();
        return gson.toJson(serverTemplate);
    }
}
