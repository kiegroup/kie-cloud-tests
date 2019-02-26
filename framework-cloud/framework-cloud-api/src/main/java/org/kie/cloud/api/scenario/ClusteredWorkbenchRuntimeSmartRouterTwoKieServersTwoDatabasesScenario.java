/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.api.scenario;

import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;

/**
 * Representation of deployment scenario with clustered Workbench Runtime, Smart router, two Kie servers and two databases.
 */
public interface ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario extends KieDeploymentScenario<ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario> {

    /**
     * Return Workbench deployment.
     *
     * @return WorkbenchDeployment
     * @see WorkbenchDeployment
     */
    WorkbenchDeployment getWorkbenchRuntimeDeployment();

    /**
     * Return Smart router deployment.
     *
     * @return SmartRouterDeployment
     * @see SmartRouterDeployment
     */
    SmartRouterDeployment getSmartRouterDeployment();

    /**
     * Return Kie Server deployment.
     *
     * @return KieServerDeployment
     * @see KieServerDeployment
     */
    KieServerDeployment getKieServerOneDeployment();

    /**
     * Return Kie Server deployment.
     *
     * @return KieServerDeployment
     * @see KieServerDeployment
     */
    KieServerDeployment getKieServerTwoDeployment();

    /**
     * Return Db server deployment.
     *
     * @return DatabaseDeployment
     * @see DatabaseDeployment
     */
    DatabaseDeployment getDatabaseOneDeployment();

    /**
     * Return Db server deployment.
     *
     * @return DatabaseDeployment
     * @see DatabaseDeployment
     */
    DatabaseDeployment getDatabaseTwoDeployment();

    /**
     * Return SSO deployment.
     *
     * @return SsoDeployment
     * @see SsoDeployment
     */
    SsoDeployment getSsoDeployment();
}
