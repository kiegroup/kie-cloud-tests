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

package org.kie.cloud.api.scenario;

import java.util.List;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.DeploymentTimeoutException;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.ControllerDeployment;

/**
 * Cloud deployment scenario representation.
 */
public interface DeploymentScenario {

    /**
     * Return deployment scenario namespace.
     *
     * @return deployment scenario name.
     */
    String getNamespace();

    /**
     * Create and deploy deployment scenario.
     *
     * @throws MissingResourceException If scenario is missing any required resource.
     * @throws DeploymentTimeoutException In case scenario deployment isn't started in defined timeout.
     */
    void deploy() throws MissingResourceException, DeploymentTimeoutException;

    /**
     * Undeploy and delete deployment scenario.
     */
    void undeploy();

    /**
     * Return all available deployments.
     *
     * @return All available deployments.
     */
    List<Deployment> getDeployments();

    /**
     * Return List of all Workbench deployments. If there aren't any deployment,
     * then is returned empty list.
     *
     * @return WorkbenchDeployments
     * @see WorkbenchDeployment
     */
    List<WorkbenchDeployment> getWorkbenchDeployments();

    /**
     * Return List of all Kie Server deployments. If there aren't any
     * deployment, then is returned empty list.
     *
     * @return KieServerDeployments
     * @see KieServerDeployment
     */
    List<KieServerDeployment> getKieServerDeployments();

    /**
     * Return List of all Smart Router deployments. If there aren't any
     * deployment, then is returned empty list.
     *
     * @return SmartRouterDeployments
     * @see SmartRouterDeployment
     */
    List<SmartRouterDeployment> getSmartRouterDeployments();

    /**
     * Return List of all Controller deployments. If there aren't any
     * deployment, then is returned empty list.
     *
     * @return ControllerDeployments
     * @see ControllerDeployment
     */
    List<ControllerDeployment> getControllerDeployments();
}
