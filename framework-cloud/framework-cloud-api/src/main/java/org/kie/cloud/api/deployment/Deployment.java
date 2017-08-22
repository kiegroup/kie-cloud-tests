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

import java.util.List;

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
     * Wait until Deployment is ready to use. This method waits until all
     * instances of deployment are initialized and ready and for router to
     * expose url.
     */
    void waitForScale();

    /**
     * Return list of all already running instances of the deployment.
     *
     * @return List of Instances
     * @see Instance
     */
    List<Instance> getInstances();

    /**
     * This method delete given cloud instances. Cloud should automaticly start
     * new instances. Number of available instance is same as before.
     *
     * @param instance Instances to be deleted
     */
    void deleteInstances(Instance... instance);

    /**
     * This method delete given list of the cloud instances. Cloud should
     * automaticly start new instances. Number of available instance is same as
     * before.
     *
     * @param instances List of instances to be deleted
     */
    void deleteInstances(List<Instance> instances);

    boolean ready();

}
