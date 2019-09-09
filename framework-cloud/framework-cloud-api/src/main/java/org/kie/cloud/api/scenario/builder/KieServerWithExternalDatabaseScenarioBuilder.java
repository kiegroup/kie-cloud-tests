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

package org.kie.cloud.api.scenario.builder;

import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;

public interface KieServerWithExternalDatabaseScenarioBuilder extends DeploymentScenarioBuilder<KieServerWithExternalDatabaseScenario> {

    /**
     * Return setup builder with additional configuration of internal maven repo.
     * 
     * Parameters will be used automatically
     * 
     * @param waitForRunning By default, the deployment will not wait for Maven Repository to be running. Set to true if you want to wait for the Maven Repository to be fully running
     * 
     * @return Builder with configured internal maven repo.
     */
    KieServerWithExternalDatabaseScenarioBuilder withInternalMavenRepo(boolean waitForRunning);

    KieServerWithExternalDatabaseScenarioBuilder withKieServerId(String kieServerId);
}
