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

import java.util.Map;

/**
 * Service representation.
 */
public interface Service {

    /**
     * @return Service name.
     */
    public String getName();

    /**
     * Delete service.
     */
    public void delete();

    /**
     * Create new Deployment config for this service.
     *
     * @param image Image to be deployed to pods.
     * @param envVariables Environment variables to be set in pods started by this deployment.
     * @return Created Deployment config.
     */
    public DeploymentConfig createDeploymentConfig(Image image, Map<String, String> envVariables);

    /**
     * Create deployment config - responsible for deploying and controlling of pods.
     * This method returns when the pods are started, but it doesn't guarantee that application is fully started.
     * For simplification the deployment config name is same as service name.
     *
     * @param image Image to be deployed to pods.
     * @param envVariables Environment variables to be set in pods started by this deployment.
     * @param pods Number of pods to be started.
     * @return Created Deployment config.
     */
    public DeploymentConfig createDeploymentConfig(Image image, Map<String, String> envVariables, int pods);

    /**
     * @return Deployment config associated to this service.
     */
    public DeploymentConfig getDeploymentConfig();

    /**
     * Create a route for the service placed in OpenShift.
     * Route URL is created in specific format: &lt;service&gt;.&lt;route subdomain&gt;
     *
     * @return Created Route.
     */
    public Route createRoute();

    /**
     * Create a route for the service.
     *
     * @param route Route URL host.
     * @return Created Route.
     */
    public Route createRoute(String route);

    /**
     * @return Route associated to this service.
     */
    public Route getRoute();
}
