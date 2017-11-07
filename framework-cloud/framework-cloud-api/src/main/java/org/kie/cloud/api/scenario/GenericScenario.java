/*
 * Copyright 2017 JBoss by Red Hat.
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
package org.kie.cloud.api.scenario;

import java.util.List;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;

public interface GenericScenario extends DeploymentScenario {

    /**
     * Return List of all Workbench deployments.
     *
     * @return WorkbenchDeployment
     * @see WorkbenchDeployment
     */
    List<WorkbenchDeployment> getWorkbenchDeployments();

    /**
     * Return List of all Kie Server deployments.
     *
     * @return KieServerDeployment
     * @see KieServerDeployment
     */
    List<KieServerDeployment> getKieServerDeployments();

    /**
     * Return List of all Smart Router deployments.
     *
     * @return SmartRouterDeployment
     * @see SmartRouterDeployment
     */
    List<SmartRouterDeployment> getSmartRouterDeployments();
}
