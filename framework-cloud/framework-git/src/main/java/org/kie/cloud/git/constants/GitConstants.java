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
import org.kie.cloud.git.GitProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitConstants implements Constants {

    public static final String GIT_PROVIDER = "git.provider";

    public static final String GITHUB_USER = "github.username";
    public static final String GITHUB_PASSWORD = "github.password";

    public static final String GOGS_URL = "gogs.url";
    public static final String GOGS_USER = "gogs.username";
    public static final String GOGS_PASSWORD = "gogs.password";

    private static final Logger logger = LoggerFactory.getLogger(GitConstants.class);

    public static String getGitProvider() {
        return System.getProperty(GIT_PROVIDER);
    }

    public static String getGitHubUser() {
        return System.getProperty(GITHUB_USER);
    }

    public static String getGitHubPassword() {
        return System.getProperty(GITHUB_PASSWORD);
    }

    public static String getGogsUrl() {
        return System.getProperty(GOGS_URL);
    }

    public static String getGogsUser() {
        return System.getProperty(GOGS_USER);
    }

    public static String getGogsPassword() {
        return System.getProperty(GOGS_PASSWORD);
    }

    public static String readMandatoryParameter(String systemPropertyName) {
        verifySystemPropertyIsSet(systemPropertyName);
        return System.getProperty(systemPropertyName);
    }

    public static void verifySystemPropertyIsSet(String systemPropertyName) {
        String systemPropertyValue = System.getProperty(systemPropertyName);
        if (systemPropertyValue == null || systemPropertyValue.isEmpty()) {
            logger.error("Parameter {} must be specified", systemPropertyName);
            throw new RuntimeException("Parameter " + systemPropertyName + " must be specified");
        }
    }

    @Override
    public void initConfigProperties() {
        GitProviderService gitProviderService = new GitProviderService();
        gitProviderService.initGitProviderConfigurationProperties();
    }
}
