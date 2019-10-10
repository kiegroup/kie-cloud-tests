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

import java.net.URL;
import java.time.Duration;
import java.time.Instant;

import cz.xtf.core.http.Https;
import cz.xtf.core.http.HttpsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterUtil {

    private static final int ROUTER_SERVICE_UNAVAILABLE_CODE = 503;
    private static final long ROUTER_WAIT_ITERATION_TIME = 250;
    private static final Duration ROUTER_WAIT_TIME = Duration.ofMinutes(5);

    private static final Logger logger = LoggerFactory.getLogger(RouterUtil.class);

    public static void waitForRouter(URL url) {
        Instant endTime = Instant.now().plus(ROUTER_WAIT_TIME);

        String urlString = url.toString();
        logger.info("Waiting for router to expose url: {}", urlString);

        while (Instant.now().isBefore(endTime)) {
            try {
                if (Https.getCode(urlString) != ROUTER_SERVICE_UNAVAILABLE_CODE) {
                    return;
                }

                Thread.sleep(ROUTER_WAIT_ITERATION_TIME);
            } catch (HttpsException e) {
                logger.debug("SSLHandshakeException: " + e.getMessage());
                logger.debug("Wait for a while and try to execute request again.");
                try {
                    Thread.sleep(Duration.ofSeconds(1).toMillis());
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for route to become available.", e1);
                }
                continue;
            } catch (Exception e) {
                logger.error("Error waiting for router", e);
                throw new RuntimeException("Error waiting for router", e);
            }
        }

        logger.warn("Timeout while waiting for router to expose url: {}. The URL is unreachable.", urlString);
    }
}
