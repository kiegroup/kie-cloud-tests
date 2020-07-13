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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BooleanSupplier;

import cz.xtf.core.waiting.WaiterException;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.tests.common.time.TimeUtils;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieServerUtils.class);

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
     * Wait for container respin after disposing a container.
     * @param kieServer
     * @param containerId
     */
    public static void waitForContainerRespinAfterDisposeContainer(KieServerDeployment kieServer, String containerId) {
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServer);
        waitForContainerRespinAfter(kieServer, () -> kieServerClient.disposeContainer(containerId));
    }

    /**
     * Wait for container respin after doing an action.
     * @param kieServer
     * @param action
     */
    public static void waitForContainerRespinAfter(KieServerDeployment kieServer, Runnable action) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> waiter = executor.submit(kieServer::waitForContainerRespin);

        action.run();
        try {
            waiter.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error waiting for container respin", e);
            throw new WaiterException("Error waiting for container respin");
        }
    }

    private static BooleanSupplier createContainerStartedBooleanSupplier(KieServicesClient kieServerClient, String containerId) {
        return () -> {
            ServiceResponse<KieContainerResource> containerInfo = kieServerClient.getContainerInfo(containerId);
            boolean isSuccessfulRequest = ResponseType.SUCCESS.equals(containerInfo.getType());
            boolean isContainerStarted = containerInfo.getResult() != null && KieContainerStatus.STARTED.equals(containerInfo.getResult().getStatus());
            return isSuccessfulRequest && isContainerStarted;
        };
    }
}
