/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.tests.common.client.util;

import java.time.Duration;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.tests.common.time.TimeUtils;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;

import static org.assertj.core.api.Assertions.assertThat;

public class KieServerUtils {

    private static final String WEBSOCKET_CONNECTION = "Connection to Kie Controller over Web Socket is now open";
    private static final String STARTED_CONTAINER = "Container %s (for release id %s) successfully started";

    /**
     * Create a container in Kie server.
     * In case of timeout caused by external router the waiting loop is used to verify that the container is created and running.
     *
     * @param kieServerClient Kie server client.
     * @param resource Container resource to be deployed.
     * @param timeout Overall deployment timeout.
     * @return ServiceResponse of Kie Container
     */
    public static ServiceResponse<KieContainerResource> createContainer(KieServicesClient kieServerClient, KieContainerResource resource, Duration timeout) {
        String containerId = resource.getContainerId();
        try {
             return kieServerClient.createContainer(containerId, resource);
        } catch (KieServicesHttpException e) {
            // In case of gateway timeout
            if (e.getHttpCode() == 504) {
                TimeUtils.wait(timeout, Duration.ofSeconds(1), createContainerStartedBooleanSupplier(kieServerClient, containerId));
                return kieServerClient.getContainerInfo(containerId);
            }
            throw e;
        }
    }

    /**
     * Wait until kie server is connected to the controller via websocket. It will check the kie server logs until a message about connection is traced.
     * If the kie server is not reconnected within 1 min, it will fail.
     *
     * @param kieServer kie server deployment to monitor.
     * @param action action to be run after starting to monitor logs.
     */
    public static void waitUntilKieServerIsConnectedAfterAction(KieServerDeployment kieServer, Runnable action) {
        Map<String, Integer> instanceStartAt = kieServer.getInstances().stream().collect(Collectors.toMap(Instance::getName, i -> i.getLogs().length()));

        action.run();

        waitUntilKieServerLogsContain(kieServer, containsMessageFromIndex(WEBSOCKET_CONNECTION, instanceStartAt));
    }

    /**
     * Wait until kie server is connected to the controller via websocket. It will check the kie server logs until a message about connection is traced.
     * If the kie server is not reconnected within 1 min, it will fail.
     *
     * @param kieServer kie server deployment to monitor.
     */
    public static void waitUntilKieServerIsConnected(KieServerDeployment kieServer) {
        waitUntilKieServerLogsContain(kieServer, containsMessage(WEBSOCKET_CONNECTION));
    }

    /**
     * Wailt until the container is started in the kie server. It will check the kie server logs until a message about container started is traced.
     * If the container is not started within 1 min, it will fail.
     *
     * @param kieServer kie server deployment to monitor.
     * @param containerId container ID to monitor.
     * @param container container kjar to monitor.
     */
    public static void waitUntilKieServerContainerIsStarted(KieServerDeployment kieServer, String containerId, Kjar container) {
        String message = String.format(STARTED_CONTAINER, containerId, container.toString());
        waitUntilKieServerLogsContain(kieServer, containsMessage(message));
    }

    private static void waitUntilKieServerLogsContain(KieServerDeployment kieServer, Predicate<Instance> predicate) {
        for (Instance kieServerInstance : kieServer.getInstances()) {
            TimeUtils.wait(Duration.ofMinutes(1), Duration.ofSeconds(1), () -> predicate.test(kieServerInstance));

            assertThat(predicate.test(kieServerInstance)).isTrue();
        }
    }

    private static final Predicate<Instance> containsMessage(String message) {
        return instance -> instance.getLogs().contains(message);
    }

    private static final Predicate<Instance> containsMessageFromIndex(String message, Map<String, Integer> startLogs) {
        return instance -> {
            Integer index = startLogs.get(instance.getName());

            String logs = instance.getLogs();
            if (index != null) {
                logs = logs.substring(index);
            }

            return logs.contains(message);
        };
    }

    private static BooleanSupplier createContainerStartedBooleanSupplier(KieServicesClient kieServerClient, String containerId) {
        return () -> {
            ServiceResponse<KieContainerResource> containerInfo = kieServerClient.getContainerInfo(containerId);
            boolean isSuccessfulRequest = containerInfo.getType().equals(ResponseType.SUCCESS);
            boolean isContainerStarted = containerInfo.getResult().getStatus().equals(KieContainerStatus.STARTED);
            return isSuccessfulRequest && isContainerStarted;
        };
    }
}
