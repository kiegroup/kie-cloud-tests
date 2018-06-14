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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.git.constants.GitConstants;
import org.kie.cloud.git.github.GitHubGitProviderFactory;
import org.kie.cloud.git.gitlab.GitLabGitProviderFactory;
import org.kie.cloud.git.gogs.GogsGitProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitProviderService {
    private final Map<String, GitProviderFactory> providerFactories;

    private static final Logger logger = LoggerFactory.getLogger(GitProviderService.class);

    public GitProviderService() {
        final Map<String, GitProviderFactory> providerFactories = new HashMap<>();
        GitProviderFactory factory;

        factory = new GitHubGitProviderFactory();
        providerFactories.put(factory.providerType(), factory);

        factory = new GitLabGitProviderFactory();
        providerFactories.put(factory.providerType(), factory);

        factory = new GogsGitProviderFactory();
        providerFactories.put(factory.providerType(), factory);

        this.providerFactories = Collections.unmodifiableMap(providerFactories);
    }

    public GitProvider createGitProvider() {
        final String gitProviderName = GitConstants.readMandatoryParameter(GitConstants.getGitProvider(),
                GitConstants.GIT_PROVIDER);
        logger.debug("Initializing Git provider {}", gitProviderName);
        if (!providerFactories.containsKey(gitProviderName)) {
            logger.error("Unknown type of Git provider {}", gitProviderName);
            throw new RuntimeException("Unknown type of Git provider " + gitProviderName);
        }

        final GitProviderFactory gitProviderFactory = providerFactories.get(gitProviderName);
        return gitProviderFactory.createGitProvider();
    }
}
