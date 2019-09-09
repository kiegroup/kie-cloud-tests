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
package org.kie.cloud.openshift.deployment.external.impl;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.deployment.external.AbstractExternalDeployment;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.MavenRepositoryDeployer;

public abstract class AbstractMavenRepositoryExternalDeployment<U> extends AbstractExternalDeployment<MavenRepositoryDeployment, U> {

    protected static final String MAVEN_DEPLOYER_REPO_URL = "MAVEN_DEPLOYER_REPO_URL";
    protected static final String MAVEN_DEPLOYER_REPO_USERNAME = "MAVEN_DEPLOYER_REPO_USERNAME";
    protected static final String MAVEN_DEPLOYER_REPO_PASSWORD = "MAVEN_DEPLOYER_REPO_PASSWORD";

    protected Map<String, String> oldValues = new HashMap<>();

    public AbstractMavenRepositoryExternalDeployment(Map<String, String> config) {
        super(config);
    }

    @Override
    public ExternalDeploymentID getKey() {
        return ExternalDeploymentID.MAVEN_REPOSITORY;
    }

    @Override
    protected MavenRepositoryDeployment deployToProject(Project project) {
        return MavenRepositoryDeployer.deploy(project, false);
    }

    @Override
    public void configure(U obj) {
        // Add maven properties to be reused by the maven deployer
        MavenRepositoryDeployment deployment = getDeploymentInformation();
        addEnvVar(obj, MAVEN_DEPLOYER_REPO_URL, deployment.getSnapshotsRepositoryUrl().toString());
        addEnvVar(obj, MAVEN_DEPLOYER_REPO_USERNAME, deployment.getUsername());
        addEnvVar(obj, MAVEN_DEPLOYER_REPO_PASSWORD, deployment.getPassword());
    }

    protected abstract void addEnvVar(U obj, String key, String value);

    @Override
    public void removeConfiguration(U object) {
        // Nothing done
    }
}
