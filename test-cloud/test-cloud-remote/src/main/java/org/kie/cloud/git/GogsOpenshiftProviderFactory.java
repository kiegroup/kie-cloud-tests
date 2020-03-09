/*
 * Copyright 2020 JBoss by Red Hat.
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
package org.kie.cloud.git;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.kie.cloud.api.deployment.GogsDeployment;
import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.git.GitProviderFactory;
import org.kie.cloud.git.gogs.GogsGitProvider;
import org.kie.cloud.openshift.deployment.GogsDeploymentImpl;
import org.kie.cloud.openshift.resource.CloudProperties;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.ProjectInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GIT provider factory to deploy a GOGS instance into Openshift.
 */
public class GogsOpenshiftProviderFactory implements GitProviderFactory, ProjectInitializer {

    private static final String GOGS = "Gogs";
    private static final String GOGS_TEMPLATE = "/deployments/gogs.yaml";

    private static final Logger logger = LoggerFactory.getLogger(GogsOpenshiftProviderFactory.class);

    private Project project;

    @Override
    public void load(Project project) {
        this.project = project;
    }

    @Override
    public String providerType() {
        return GOGS;
    }

    @Override
    public GitProvider createGitProvider() {
        if (project == null) {
            throw new RuntimeException("Project not initialized. Call load() before creating GIT provider");
        }

        GogsDeployment deployment = deploy(project);
        return new GogsGitProvider(deployment.getUrl(), deployment.getUsername(), deployment.getPassword());
    }

    private static GogsDeployment deploy(Project project) {
        logger.info("Creating internal GOGS instance.");
        project.runOcCommandAsAdmin("adm", "policy", "add-scc-to-user", "anyuid", "-z", "default");
        project.processTemplateAndCreateResources(getGogsTemplate(), getGogsProperties(project));
        project.runOcCommandAsAdmin("expose", "service", "gogs");

        logger.info("Waiting for Gogs deployment to become ready.");
        GogsDeployment deployment = new GogsDeploymentImpl(project);
        deployment.waitForScale();

        return deployment;
    }

    private static Map<String, String> getGogsProperties(Project project) {
        return Collections.singletonMap("GOGS_DOCKER_IMAGE", CloudProperties.getInstance().getGogsDockerImage());
    }

    private static URL getGogsTemplate() {
        try {
            return new URL("file://" + GogsOpenshiftProviderFactory.class.getResource(GOGS_TEMPLATE).getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Wrong GOGS template location", e);
        }
    }
}
