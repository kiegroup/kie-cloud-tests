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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import org.kie.cloud.git.constants.GitConstants;
import org.kie.cloud.git.github.GitHubGitProvider;

public class GitProviderFactoryTest {

    @Test
    public void testGetNotDefinedGitProvider() {
        Throwable thrown = catchThrowable(() -> GitProviderFactory.getGitProvider());
        assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessageContaining("No GIT provider defined.");
    }

    @Test
    public void testGetNotFoundGitProvider() {
        System.setProperty(GitConstants.GIT_PROVIDER, "not-existing-provider");

        try {
            Throwable thrown = catchThrowable(() -> GitProviderFactory.getGitProvider());
            assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessageContaining("GIT provider with name not-existing-provider not found");
        } finally {
            System.clearProperty(GitConstants.GIT_PROVIDER);
        }
    }

    @Test
    public void testGetGitHubGitProvider() {
        System.setProperty(GitConstants.GIT_PROVIDER, "GitHub");

        try {
            assertThat(GitProviderFactory.getGitProvider()).isInstanceOf(GitHubGitProvider.class);
        } finally {
            System.clearProperty(GitConstants.GIT_PROVIDER);
        }
    }
}
