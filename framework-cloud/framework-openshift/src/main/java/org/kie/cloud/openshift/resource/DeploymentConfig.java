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

/**
 * Deployment config representation.
 */
public interface DeploymentConfig {

    /**
     * @return Deployment config name.
     */
    public String getName();

    /**
     * Change number of pods available for the deployment.
     *
     * @param numberOfPods Number of pods to be available.
     */
    public void scalePods(int numberOfPods);

    /**
     * Wait until all pods are initialized and ready.
     */
    public void waitUntilAllPodsAreReady();

    /**
     * Delete deployment controller.
     */
    public void delete();
}
