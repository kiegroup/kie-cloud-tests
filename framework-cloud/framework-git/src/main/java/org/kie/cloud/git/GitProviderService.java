/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.kie.cloud.api.constants.ConfigurationInitializer;
import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.git.GitProviderFactory;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.git.constants.GitConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitProviderService {

    private final List<GitProviderFactory> providerFactories;

    private static final Logger logger = LoggerFactory.getLogger(GitProviderService.class);

    private boolean isConfigurationInitialized = false;

    public GitProviderService() {
        List<GitProviderFactory> factories = new ArrayList<>();
        ServiceLoader.load(GitProviderFactory.class).forEach(factories::add);
        this.providerFactories = Collections.unmodifiableList(factories);
    }

    public GitProvider createGitProvider() {
        if (!isConfigurationInitialized) {
            ConfigurationInitializer.initConfigProperties();
            isConfigurationInitialized = true;
        }
        return getGitProviderFactory().createGitProvider();
    }

    public void initGitProviderConfigurationProperties() {
        getGitProviderFactory().initGitConfigurationProperties();
    }

    private GitProviderFactory getGitProviderFactory() {
        final String gitProviderName = GitConstants.readMandatoryParameter(GitSettings.GIT_PROVIDER);
        logger.debug("Initializing Git provider {}", gitProviderName);
        return this.providerFactories.stream().filter(factory -> factory.providerType().equalsIgnoreCase(gitProviderName))
                                     .findFirst()
                                     .orElseThrow(() -> new RuntimeException("Unknown type of Git provider " + gitProviderName));
    }
}
