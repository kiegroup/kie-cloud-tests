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
import java.util.Objects;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.openshift.resource.Project;

public abstract class AbstractExternalDeployment<T extends Deployment, U> implements ExternalDeployment<T, U> {

    protected Map<String, String> deploymentConfig;

    private T deployment;

    public AbstractExternalDeployment(Map<String, String> deploymentConfig) {
        super();
        this.deploymentConfig = deploymentConfig;
    }

    @Override
    public T deploy(Project project) {
        this.deployment = deployToProject(project);
        return this.deployment;
    }

    protected abstract T deployToProject(Project project);

    protected T getDeploymentInformation() {
        if (Objects.isNull(this.deployment)) {
            throw new RuntimeException("Trying to access deployment informaiton whereas the deployment has not been done ...");
        }
        return this.deployment;
    }

}
