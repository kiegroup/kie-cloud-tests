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

package org.kie.cloud.openshift.resource;

import java.util.List;

import org.kie.cloud.api.deployment.DeploymentTimeoutException;

/**
 * Deployment config representation.
 */
public interface DeploymentConfig {

    /**
     * @return Deployment config name.
     */
    public String getName();

    /**
     * Return number of pods available for the deployment.
     *
     * @return Number of pods available for the deployment.
     */
    public int podsNumber();

    /**
     * Change number of pods available for the deployment.
     *
     * @param numberOfPods Number of pods to be available.
     */
    public void scalePods(int numberOfPods);

    /**
     * Wait until all pods are initialized and ready.
     * @throws DeploymentTimeoutException In case pods didn't start in defined timeout.
     */
    public void waitUntilAllPodsAreReady() throws DeploymentTimeoutException;

    /**
     * Delete deployment controller.
     */
    public void delete();

    /**
     * Returns all Pods associated with this Deployment config
     *
     * @return List of Pods for this DC.
     */
    public List<Pod> getPods();
}
