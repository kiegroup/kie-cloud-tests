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
import java.util.function.BooleanSupplier;

import org.kie.cloud.tests.common.time.TimeUtils;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;

public class KieServerUtils {

    /**
     * Create a container in Kie server.
     * In case of timeout caused by external router the waiting loop is used to verify that the container is created and running.
     *
     * @param kieServerClient Kie server client.
     * @param resource Container resource to be deployed.
     * @param timeout Overall deployment timeout.
     * @return
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

    private static BooleanSupplier createContainerStartedBooleanSupplier(KieServicesClient kieServerClient, String containerId) {
        return () -> {
            ServiceResponse<KieContainerResource> containerInfo = kieServerClient.getContainerInfo(containerId);
            boolean isSuccessfulRequest = containerInfo.getType().equals(ResponseType.SUCCESS);
            boolean isContainerStarted = containerInfo.getResult().getStatus().equals(KieContainerStatus.STARTED);
            return isSuccessfulRequest && isContainerStarted;
        };
    }
}
