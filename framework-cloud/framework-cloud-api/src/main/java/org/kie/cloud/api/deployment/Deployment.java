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

package org.kie.cloud.api.deployment;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Deployment representation in cloud environment.
 */
public interface Deployment {

    /**
     * Return deployment namespace. It is an identifier grouping (one or
     * several) deployments into logical group.
     *
     * @return Deployment namespace
     */
    String getNamespace();

    /**
     * Change number of instances available for the deployment.
     *
     * @param instances Number of deployment instances to be available..
     */
    void scale(int instances);

    /**
     * Return deployment replicas count. Replicas count is change as deployment is scale up or down.
     *
     * @return number of replicas
     */
    int getReplicas();

    /**
     * Wait until Deployment is ready to use. This method waits until all
     * instances of deployment are initialized and ready and for router to
     * expose url.
     * @throws DeploymentTimeoutException In case deployment isn't scaled in defined timeout.
     */
    void waitForScale() throws DeploymentTimeoutException;

    /**
     * Wait until Deployment is scheduled. This method waits until all
     * instances of deployment are scheduled.
     * @throws DeploymentTimeoutException In case deployment isn't scheduled in defined timeout.
     */
    void waitForScheduled() throws DeploymentTimeoutException;

    /**
     * Return list of all already running instances of the deployment.
     *
     * @return List of Instances
     * @see Instance
     */
    List<Instance> getInstances();

    /**
     * This method delete all instances for Deployment. Cloud should
     * automaticly start new instances. Number of available instance is same as
     * before.
     */
    void deleteInstances();

    /**
     * @return True if deployment is deployed and ready to be used.
     */
    boolean isReady();

    /**
     * This method set a router timeout for the cloud deployment.
     *
     * @param timeoutValue Timeout value.
     */
    void setRouterTimeout(Duration timeoutValue);

    /**
     * This method resets a router timeout for the cloud deployment back to original value.
     */
    void resetRouterTimeout();

    /**
     * This method sets a router balance strategy for the cloud deployment.
     *
     * @param balance Type of balance (roundrobin, etc.)
     */
    void setRouterBalance(String balance);

    /**
     * This method sets resources quota for the cloud deployment.
     *
     * @param requests map with the CPU, memory, ephemeral storage requests
     * @param requests map with the CPU, memory, ephemeral storage limits
     */
    void setResources(Map<String, String> requests, Map<String, String> limits);

    /**
     * This method waits until Deployment is deployed using the specified versionTag (for example, 7.7.0).
     *
     * @param versionTag to wait for.
     */
    void waitForVersionTag(String versionTag);
}
