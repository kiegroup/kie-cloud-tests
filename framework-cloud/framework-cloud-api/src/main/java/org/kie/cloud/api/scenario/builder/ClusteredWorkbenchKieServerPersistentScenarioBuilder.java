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

import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.settings.GitSettings;

public interface ClusteredWorkbenchKieServerPersistentScenarioBuilder extends DeploymentScenarioBuilder<ClusteredWorkbenchKieServerPersistentScenario> {

    /**
     * Return setup builder with additional configuration of internal maven repo.
     *
     * Parameters will be used automatically
     *
     * @return Builder with configured internal maven repo.
     */
    ClusteredWorkbenchKieServerPersistentScenarioBuilder withInternalMavenRepo();

    /**
     * Return setup builder with configured Git hooks dir.
     * @param dir GIT_HOOKS_DIR
     * @return Builder
     */
    ClusteredWorkbenchKieServerPersistentScenarioBuilder withGitHooksDir(String dir);

    /**
     * Return setup builder with additional configuration for SSO deployment.
     *
     * @return Builder
     */
    ClusteredWorkbenchKieServerPersistentScenarioBuilder deploySso();

    /**
     * Return setup builder with additional GIT settings.
     *
     * @param gitSettings settings configuration of GIT
     * @return Builder
     */
    ClusteredWorkbenchKieServerPersistentScenarioBuilder withGitSettings(GitSettings gitSettings);

    /**
     * Return setup builder with specified memory limit.
     *
     * @param limit memory limit (e.g.: 4Gi, etc).
     * @return Builder with configured memory limit.
     */
    ClusteredWorkbenchKieServerPersistentScenarioBuilder withWorkbenchMemoryLimit(String limit);
}
