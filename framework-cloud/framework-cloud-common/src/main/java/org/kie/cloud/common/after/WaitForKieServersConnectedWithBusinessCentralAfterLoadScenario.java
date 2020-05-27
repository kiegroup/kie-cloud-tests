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

import java.util.List;
import java.util.stream.Collectors;

import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action will wait until all the kie server instances are connected to all the Business Centrals.
 */
public class WaitForKieServersConnectedWithBusinessCentralAfterLoadScenario implements AfterLoadScenario {

    private static final Logger logger = LoggerFactory.getLogger(WaitForKieServersConnectedWithBusinessCentralAfterLoadScenario.class);

    @Override
    public void after(DeploymentScenario<?> scenario) {
        logger.info("Waiting for Kie server to register itself to the Workbench.");
        int totalKieServers = getTotalKieServers(scenario);
        getBusinessCentrals(scenario).forEach(businessCentral -> {
            KieServerControllerClientProvider.waitForServerTemplateCreation(businessCentral, totalKieServers);
        });
    }

    private int getTotalKieServers(DeploymentScenario<?> scenario) {
        return getKieServers(scenario).size() + getSmartRouters(scenario).size();
    }

    private List<WorkbenchDeployment> getBusinessCentrals(DeploymentScenario<?> scenario) {
        return getDeploymentOfType(scenario, WorkbenchDeployment.class);
    }

    private List<KieServerDeployment> getKieServers(DeploymentScenario<?> scenario) {
        return getDeploymentOfType(scenario, KieServerDeployment.class);
    }

    private List<SmartRouterDeployment> getSmartRouters(DeploymentScenario<?> scenario) {
        return getDeploymentOfType(scenario, SmartRouterDeployment.class);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getDeploymentOfType(DeploymentScenario<?> scenario, Class<T> clazz) {
        return scenario.getDeployments().stream().filter(d -> clazz.isInstance(d)).map(d -> (T) d).collect(Collectors.toList());
    }

}
