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

package org.kie.cloud.git.constants;

import org.kie.cloud.api.constants.Constants;

public class GitConstants implements Constants {

    public static final String GIT_PROVIDER = "git.provider";

    public static final String GITLAB_URL = "gitlab.url";
    public static final String GITLAB_USER = "gitlab.username";
    public static final String GITLAB_PASSWORD = "gitlab.password";

    public static final String GITHUB_USER = "github.username";
    public static final String GITHUB_PASSWORD = "github.password";

    public static String getGitProvider() {
        return System.getProperty(GIT_PROVIDER);
    }

    public static String getGitLabUrl() {
        return System.getProperty(GITLAB_URL);
    }

    public static String getGitLabUser() {
        return System.getProperty(GITLAB_USER);
    }

    public static String getGitLabPassword() {
        return System.getProperty(GITLAB_PASSWORD);
    }

    public static String getGitHubUser() {
        return System.getProperty(GITHUB_USER);
    }

    public static String getGitHubPassword() {
        return System.getProperty(GITHUB_PASSWORD);
    }

    @Override
    public void initConfigProperties() {
        // init XTF configuration for GitLab
        System.setProperty("xtf.config.gitlab.url", getGitLabUrl());
        System.setProperty("xtf.config.gitlab.username", getGitLabUser());
        System.setProperty("xtf.config.gitlab.password", getGitLabPassword());
        System.setProperty("xtf.config.gitlab.group.enabled", "false");
        System.setProperty("xtf.config.gitlab.token", "disabled");
    }
}
