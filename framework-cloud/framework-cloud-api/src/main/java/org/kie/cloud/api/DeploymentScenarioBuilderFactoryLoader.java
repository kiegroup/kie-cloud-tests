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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class DeploymentScenarioBuilderFactoryLoader {
    public static DeploymentScenarioBuilderFactory getInstance() {
        List<DeploymentScenarioBuilderFactory> deploymentScenarioBuilderFactories = getFactories();
        if (deploymentScenarioBuilderFactories.size() == 1) {
            return deploymentScenarioBuilderFactories.get(0);
        } else if (deploymentScenarioBuilderFactories.size() == 0) {
            throw new RuntimeException("No cloud API implementation was found");
        } else {
            String cloudImplementations = deploymentScenarioBuilderFactories.stream().map(x -> x.getCloudAPIImplementationName()).collect(Collectors.joining(", "));
            throw new RuntimeException("Multiple cloud API implementations detected - please select one from: " + cloudImplementations);
        }
    }

    public static DeploymentScenarioBuilderFactory getInstance(String environmentName) {
        List<DeploymentScenarioBuilderFactory> deploymentScenarioBuilderFactories = getFactories();
        Map<String, DeploymentScenarioBuilderFactory> factoryMap = new HashMap<>();
        deploymentScenarioBuilderFactories.forEach(x -> factoryMap.put(x.getCloudAPIImplementationName(), x));

        if (factoryMap.containsKey(environmentName)) {
            return factoryMap.get(environmentName);
        } else {
            String cloudImplementations = deploymentScenarioBuilderFactories.stream().map(x -> x.getCloudAPIImplementationName()).collect(Collectors.joining(", "));
            throw new RuntimeException("No implementantion of cloud API with name " + environmentName + " was found. Possible options are: " + cloudImplementations);
        }
    }

    private static List<DeploymentScenarioBuilderFactory> getFactories() {
        ServiceLoader<DeploymentScenarioBuilderFactory> serviceLoader = ServiceLoader.load(DeploymentScenarioBuilderFactory.class);
        Iterator<DeploymentScenarioBuilderFactory> deploymentBuilderFactoryIterator = serviceLoader.iterator();
        List<DeploymentScenarioBuilderFactory> factories = new ArrayList<>();
        deploymentBuilderFactoryIterator.forEachRemaining(factories::add);

        return factories;
    }
}
