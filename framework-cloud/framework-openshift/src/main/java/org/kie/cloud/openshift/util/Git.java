/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.openshift.util;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.kie.cloud.api.deployment.GogsDeployment;
import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.git.GitProviderService;
import org.kie.cloud.git.gogs.GogsGitProvider;
import org.kie.cloud.openshift.resource.Project;

/**
 * Utility to create a GitProvider instance and configure the GIT repository in a standard approach.
 *
 * Once the GitProvider is created, it will raise a repository loaded event (using the Observer pattern), so external configuration
 * can be updated transparently.
 *
 */
public class Git {

    private static final String GOGS = "Gogs";
    private static final Map<String, Function<Project, GitProvider>> SCENARIO_PROVIDERS = Collections.singletonMap(GOGS, deployGogs());

    public static synchronized GitProvider getProvider(Project project, GitSettings settings) {
        GitProvider provider = Optional.ofNullable(SCENARIO_PROVIDERS.get(settings.getProvider()))
                                       .map(f -> f.apply(project))
                                       .orElseGet(() -> new GitProviderService().createGitProvider());

        provider.createGitRepository(settings.getRepositoryName(), settings.getRepositoryPath());

        onRepositoryLoaded(provider, settings);

        return provider;
    }

    private static void onRepositoryLoaded(GitProvider provider, GitSettings settings) {
        if (settings.getRepositoryLoadedActions() != null) {
            String repoUrl = provider.getRepositoryUrl(settings.getRepositoryName());
            settings.getRepositoryLoadedActions().forEach(action -> action.accept(repoUrl));
        }
    }

    private static Function<Project, GitProvider> deployGogs() {
        return project -> {
            GogsDeployment deployment = GogsDeployer.deploy(project);
            return new GogsGitProvider(deployment.getUrl(), deployment.getUsername(), deployment.getPassword());
        };
    }
}
