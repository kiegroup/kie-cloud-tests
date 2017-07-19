/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.git.constants.GitConstants;
import org.kie.cloud.git.github.GitHubGitProvider;
import org.kie.cloud.git.gitlab.GitLabGitProvider;

public class GitProviderFactory {

    private static Map<String, GitProvider> gitProviders = new HashMap<>();

    static {
        gitProviders.put("GitLab", new GitLabGitProvider());
        gitProviders.put("GitHub", new GitHubGitProvider());
    }

    private GitProviderFactory() {}

    public static GitProvider getGitProvider() {
        String gitProviderName = GitConstants.getGitProvider();

        if(gitProviderName == null) {
            throw new RuntimeException("No GIT provider defined. Please define provider using " + GitConstants.GIT_PROVIDER + " system property.");
        }

        if(!gitProviders.containsKey(gitProviderName)) {
            throw new RuntimeException("GIT provider with name " + gitProviderName + " not found in the list of available providers.");
        }

        GitProvider gitProvider = gitProviders.get(gitProviderName);
        gitProvider.init();
        return gitProvider;
    }
}
