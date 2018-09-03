/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.api.scenario.builder;

import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;

public interface ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder extends DeploymentScenarioBuilder<ClusteredWorkbenchKieServerDatabasePersistentScenario> {
    /**
     * Return setup builder with additional configuration of external maven
     * repo.
     *
     * @param repoUrl Maven repo URL.
     * @param repoUserName Maven repo username.
     * @param repoPassword Maven repo password.
     * @return Builder with configured external maven repo.
     */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword);

    /**
     * Return setup builder with configured Git hooks dir.
     * @param dir GIT_HOOKS_DIR
     * @return Builder
     */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withGitHooksDir(String dir);
}
