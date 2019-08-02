/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.openshift.resource.Project;

/**
 * Definition of a deployment which should be launched parallel or not to scenario
 */
public interface ExternalDeployment<T extends Deployment, U> {

    /** 
     * @return Key used to identity the extra deployment
     */
    String getKey();

    /**
     * Launch deployment into the given project
     * 
     * @return Deployment entity
     */
    T deploy(Project project);

    /**
     * Retrieve the deployment variables
     */
    Map<String, String> getDeploymentVariables();

    /**
     * Configure the given object with this external deployment information
     * 
     * @param object This object should be specific for deployment process (templates, operator, apb ...)
     */
    void configure(U object);
    
    /**
     * Remove configuration from the given object with this external deployment information
     * 
     * @param object This object should be specific for deployment process (templates, operator, apb ...)
     */
    void removeConfiguration(U object);
}
