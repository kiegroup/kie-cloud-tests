/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

public interface DeploymentScenarioListener<T extends DeploymentScenario<T>> {

    /**
     * Invoked before the deployment scenario is installed. 
     *
     * @param deploymentScenario Deployment scenario which is going to be deployed.
     */
    default void beforeDeploymentStarted(T deploymentScenario) {};
    
    /**
     * Invoked after the scenario has run. 
     *
     * @param deploymentScenario Deployment scenario which has been run.
     */
    default void afterScenarioFinished(T deploymentScenario) {};
}
