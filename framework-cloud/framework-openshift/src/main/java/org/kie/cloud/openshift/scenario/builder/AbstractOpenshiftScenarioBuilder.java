/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.scenario.builder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.kie.cloud.api.scenario.builder.DeploymentScenarioBuilder;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;
import org.kie.cloud.openshift.deployment.external.ExternalDeploymentFactory;
import org.kie.cloud.openshift.scenario.OpenShiftScenario;

/**
 * 
 */
public abstract class AbstractOpenshiftScenarioBuilder<T> implements DeploymentScenarioBuilder<T> {

    private Set<ExternalDeployment.ExternalDeploymentID> asyncExternalDeploymentIds = new HashSet<>();
    private Set<ExternalDeployment.ExternalDeploymentID> syncExternalDeploymentIds = new HashSet<>();
    private Map<ExternalDeployment.ExternalDeploymentID, Map<String, String>> configs = new EnumMap<>(ExternalDeployment.ExternalDeploymentID.class);

    public T build() {
        T scenario = getDeploymentScenarioInstance();
        if (scenario instanceof OpenShiftScenario<?>) {
            syncExternalDeploymentIds.forEach(id -> ((OpenShiftScenario<?>) scenario).addExtraDeploymentSynchronized(getExternalDeploymentFactory().get(id, getConfig(id))));
            asyncExternalDeploymentIds.forEach(id -> ((OpenShiftScenario<?>) scenario).addExtraDeployment(getExternalDeploymentFactory().get(id, getConfig(id))));
        }
        return scenario;
    }

    /**
     * Add unsynchronized external deployment to the current scenario builder, with no config
     * 
     * @param id ID of the external deployment
     */
    protected void setAsyncExternalDeployment(ExternalDeployment.ExternalDeploymentID id) {
        setExternalDeployment(id, false, new HashMap<>());
    }

    /**
     * Add unsynchronized external deployment to the current scenario builder, with the given config
     * 
     * @param id ID of the external deployment
     * @param configMap Configuration map for the external deployment
     */
    protected void setAsyncExternalDeployment(ExternalDeployment.ExternalDeploymentID id, Map<String, String> configMap) {
        setExternalDeployment(id, false, configMap);
    }

    /**
     * Add synchornized external deployment to the current scenario builder, with no config
     * 
     * @param id ID of the external deployment
     */
    protected void setSyncExternalDeployment(ExternalDeployment.ExternalDeploymentID id) {
        setExternalDeployment(id, true, new HashMap<>());
    }

    /**
     * Add synchornized external deployment to the current scenario builder, with the given config
     * 
     * @param id ID of the external deployment
     * @param configMap Configuration map for the external deployment
     */
    protected void setSyncExternalDeployment(ExternalDeployment.ExternalDeploymentID id, Map<String, String> configMap) {
        setExternalDeployment(id, true, configMap);
    }

    /**
     * Add external deployment to the current scenario builder with no defined configuration
     * 
     * @param id ID of the external deployment
     * @param sync whether the deployment should be in sync, means wait for the deployment to be finished before going further
     */
    protected void setExternalDeployment(ExternalDeployment.ExternalDeploymentID id, boolean sync) {
        setExternalDeployment(id, sync, new HashMap<>());
    }

    /**
     * Add external deployment to the current scenario builder
     * 
     * @param id ID of the external deployment
     * @param sync whether the deployment should be in sync, means wait for the deployment to be finished before going further
     * @param configMap Configuration map for the external deployment
     */
    protected void setExternalDeployment(ExternalDeployment.ExternalDeploymentID id, boolean sync, Map<String, String> configMap) {
        if (sync) {
            syncExternalDeploymentIds.add(id);
        } else {
            asyncExternalDeploymentIds.add(id);
        }
        configs.put(id, configMap);
    }

    protected abstract ExternalDeploymentFactory getExternalDeploymentFactory();

    protected abstract T getDeploymentScenarioInstance();

    private Map<String, String> getConfig(ExternalDeployment.ExternalDeploymentID id) {
        return Optional.ofNullable(configs.get(id)).orElse(new HashMap<>());
    }
}
