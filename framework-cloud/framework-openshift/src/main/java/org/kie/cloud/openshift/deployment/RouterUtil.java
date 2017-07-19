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

package org.kie.cloud.openshift.deployment;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterUtil {

    private static final int ROUTER_CODE = 503;
    private static final String ROUTER_MESSAGE = "The application is currently not serving requests at this endpoint. It may not have been started or is still starting.";
    private static final long ROUTER_WAIT_ITERATION_TIME = 250;
    private static final Duration ROUTER_WAIT_TIME = Duration.of(5, ChronoUnit.SECONDS);

    private static final Logger logger = LoggerFactory.getLogger(RouterUtil.class);

    public static void waitForRouter(URL url) {
        LocalDateTime endTime = LocalDateTime.now().plus(ROUTER_WAIT_TIME);

        while (LocalDateTime.now().isBefore(endTime)) {
            try {
                logger.info("Waiting for router to expose url: {}", url.toString());
                HttpGet request = new HttpGet(url.toString());
                HttpClient client = HttpClientBuilder.create().build();
                HttpResponse response = client.execute(request);

                if (response.getStatusLine().getStatusCode() != ROUTER_CODE) {
                    return;
                }

                String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                if (!responseContent.contains(ROUTER_MESSAGE)) {
                    return;
                }

                Thread.sleep(ROUTER_WAIT_ITERATION_TIME);
            } catch (Exception e) {
                throw new RuntimeException("Error waiting for router", e);
            }
        }
    }
}
