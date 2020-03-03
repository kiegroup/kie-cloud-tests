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
package org.kie.cloud.utils;

import org.kie.cloud.api.scenario.KieDeploymentScenario;

/**
 * Utility class with methods to work with the GIT provider (provided in the deployment scenario). *
 */
public final class GitUtils {

    private GitUtils() {

    }

    /**
     * Delete the specified repository name using the git provider in the deploymentScenario.
     *
     * @param repositoryName repository to delete
     * @param deploymentScenario git provider to use
     */
    public static final void deleteGitRepository(String repositoryName, KieDeploymentScenario<?> deploymentScenario) {
        deploymentScenario.getGitProvider().ifPresent(gitProvider -> gitProvider.deleteGitRepository(repositoryName));
    }
}
