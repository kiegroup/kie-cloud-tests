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

package org.kie.cloud.api;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.kie.cloud.api.scenario.builder.WorkbenchWithKieServerScenarioBuilder;

public interface DeploymentScenarioBuilderFactory {
    WorkbenchWithKieServerScenarioBuilder getWorkbenchWithKieServerScenarioBuilder();
    void deleteNamespace(String namespace);

    static DeploymentScenarioBuilderFactory getInstance() {
        ServiceLoader<DeploymentScenarioBuilderFactory> serviceLoader = ServiceLoader.load(DeploymentScenarioBuilderFactory.class);
        Iterator<DeploymentScenarioBuilderFactory> deploymentBuilderFactoryIterator = serviceLoader.iterator();
        if (!deploymentBuilderFactoryIterator.hasNext()) {
            throw new RuntimeException("Can not find DeploymentBuilderFactory implementation");
        }
        DeploymentScenarioBuilderFactory deploymentScenarioBuilderFactory = serviceLoader.iterator().next();

        return deploymentScenarioBuilderFactory;
    }
}
