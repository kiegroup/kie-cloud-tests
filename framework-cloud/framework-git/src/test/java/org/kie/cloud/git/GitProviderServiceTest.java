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

import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.git.constants.GitConstants;
import org.kie.cloud.git.github.GitHubGitProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class GitProviderServiceTest {

    private GitProviderService gitProviderService;

    @Before
    public void initService() {
        gitProviderService = new GitProviderService();
    }

    @Test
    public void testGetNotDefinedGitProvider() {
        Throwable thrown = catchThrowable(() -> gitProviderService.createGitProvider());
        assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessageContaining("Parameter git.provider must be specified");
    }

    @Test
    public void testGetNotFoundGitProvider() {
        System.setProperty(GitSettings.GIT_PROVIDER, "not-existing-provider");

        try {
            Throwable thrown = catchThrowable(() -> gitProviderService.createGitProvider());
            assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessageContaining("Unknown type of Git provider not-existing-provider");
        } finally {
            System.clearProperty(GitSettings.GIT_PROVIDER);
        }
    }

    @Test
    public void testGetGitHubGitProvider() {
        System.setProperty(GitSettings.GIT_PROVIDER, "GitHub");
        System.setProperty(GitConstants.GITHUB_USER, "GitHubUser");
        System.setProperty(GitConstants.GITHUB_PASSWORD, "GitHubPass");

        try {
            assertThat(gitProviderService.createGitProvider()).isInstanceOf(GitHubGitProvider.class);
        } finally {
            System.clearProperty(GitSettings.GIT_PROVIDER);
            System.clearProperty(GitConstants.GITHUB_USER);
            System.clearProperty(GitConstants.GITHUB_PASSWORD);
        }
    }
}
