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
import java.util.Optional;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.deployment.external.AbstractExternalDeployment;
import org.kie.cloud.openshift.deployment.external.MavenRepositoryExternalDeployment;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.MavenRepositoryDeployer;

public abstract class AbstractMavenRepositoryExternalDeployment<U> extends AbstractExternalDeployment<MavenRepositoryDeployment, U> implements MavenRepositoryExternalDeployment<U> {

    protected static final String SYSTEM_MAVEN_REPO_URL = "maven.repo.url";
    protected static final String SYSTEM_MAVEN_REPO_USERNAME = "maven.repo.username";
    protected static final String SYSTEM_MAVEN_REPO_PASSWORD = "maven.repo.password";

    protected Map<String, String> oldValues = new HashMap<String, String>();

    public AbstractMavenRepositoryExternalDeployment(Map<String, String> config) {
        super(config);
    }

    @Override
    protected MavenRepositoryDeployment deployToProject(Project project) {
        return MavenRepositoryDeployer.deploy(project, false);
    }

    @Override
    public void configure(U obj) {
        // TODO to change once not using system properties anymore for kjars ...
        // Save old configuration
        saveSystemProperty(SYSTEM_MAVEN_REPO_URL);
        saveSystemProperty(SYSTEM_MAVEN_REPO_USERNAME);
        saveSystemProperty(SYSTEM_MAVEN_REPO_PASSWORD);

        // Setup new system properties
        MavenRepositoryDeployment deployment = getDeploymentInformation();
        System.setProperty(SYSTEM_MAVEN_REPO_URL, deployment.getSnapshotsRepositoryUrl().toString());
        System.setProperty(SYSTEM_MAVEN_REPO_USERNAME, deployment.getUsername());
        System.setProperty(SYSTEM_MAVEN_REPO_PASSWORD, deployment.getPassword());
    }

    @Override
    public void removeConfiguration(U object) {
        // Restore system properties
        restoreSystemProperty(SYSTEM_MAVEN_REPO_URL);
        restoreSystemProperty(SYSTEM_MAVEN_REPO_USERNAME);
        restoreSystemProperty(SYSTEM_MAVEN_REPO_PASSWORD);
    }

    private void saveSystemProperty(String key) {
        oldValues.put(key, System.getProperty(key));
    }

    private void restoreSystemProperty(String key) {
        Optional.ofNullable(oldValues.get(key)).ifPresent(value -> System.setProperty(key, value));
    }
}
