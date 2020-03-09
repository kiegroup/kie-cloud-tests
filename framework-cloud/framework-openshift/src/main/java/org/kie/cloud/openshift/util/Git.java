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

import java.util.ServiceLoader;

import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.git.GitProviderFactory;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.openshift.resource.Project;

/**
 * Utility to create a GitProvider instance and configure the GIT repository in a standard approach.
 *
 * Once the GitProvider is created, it will raise a repository loaded event (using the Observer pattern), so external configuration
 * can be updated transparently.
 *
 */
public class Git {

    public static synchronized GitProvider createProvider(Project project, GitSettings settings) {
        ServiceLoader<GitProviderFactory> factories = ServiceLoader.load(GitProviderFactory.class);
        for (GitProviderFactory factory : factories) {
            if (factory.providerType().equals(settings.getProvider())) {
                initFactory(factory, project);

                GitProvider provider = createProvider(settings, factory);

                onRepositoryLoaded(provider, settings);
                return provider;
            }
        }

        throw new RuntimeException("GIT provider not found");
    }

    private static GitProvider createProvider(GitSettings settings, GitProviderFactory factory) {
        GitProvider provider = factory.createGitProvider();

        provider.createGitRepository(settings.getRepositoryName(), settings.getRepositoryPath());
        return provider;
    }

    private static void initFactory(GitProviderFactory factory, Project project) {
        factory.initGitConfigurationProperties();

        if (factory instanceof ProjectInitializer) {
            ((ProjectInitializer) factory).load(project);
        }
    }

    private static void onRepositoryLoaded(GitProvider provider, GitSettings settings) {
        if (settings.getRepositoryLoadedActions() != null) {
            String repoUrl = provider.getRepositoryUrl(settings.getRepositoryName());
            settings.getRepositoryLoadedActions().forEach(action -> action.accept(repoUrl));
        }
    }
}
