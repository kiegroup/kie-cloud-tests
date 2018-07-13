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

package org.kie.cloud.git.gitlab;

import org.kie.cloud.git.GitProvider;
import org.kie.cloud.git.GitProviderFactory;
import org.kie.cloud.git.constants.GitConstants;

public class GitLabGitProviderFactory implements GitProviderFactory {

    @Override public String providerType() {
        return "GitLab";
    }

    @Override public GitProvider createGitProvider() {
        return new GitLabGitProvider();
    }

    @Override
    public void initGitConfigurationProperties() {
        System.setProperty("xtf.config.gitlab.url", GitConstants.readMandatoryParameter(GitConstants.GITLAB_URL));
        System.setProperty("xtf.config.gitlab.username", GitConstants.readMandatoryParameter(GitConstants.GITLAB_USER));
        System.setProperty("xtf.config.gitlab.password", GitConstants.readMandatoryParameter(GitConstants.GITLAB_PASSWORD));
        System.setProperty("xtf.config.gitlab.group.enabled", "false");
        System.setProperty("xtf.config.gitlab.token", "disabled");
    }
}
