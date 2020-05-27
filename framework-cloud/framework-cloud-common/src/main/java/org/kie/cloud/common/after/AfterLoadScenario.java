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
package org.kie.cloud.common.after;

import org.kie.cloud.api.scenario.DeploymentScenario;

/**
 * This interface allows to run some actions after the scenario is deployed.
 */
public interface AfterLoadScenario {

    /**
     * Method called after the scenario is deployed.
     * @param scenario scenario deployed.
     */
    void after(DeploymentScenario<?> scenario);
}
