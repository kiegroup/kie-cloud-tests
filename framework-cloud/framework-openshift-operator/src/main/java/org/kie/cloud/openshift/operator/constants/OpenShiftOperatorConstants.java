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

package org.kie.cloud.openshift.operator.constants;

import org.kie.cloud.api.constants.Constants;

public class OpenShiftOperatorConstants implements Constants {

    /**
     * URL pointing to Operator custom resource definition file.
     */
    public static final String KIE_APP_OPERATOR_DEPLOYMENTS_CRD = "kie.app.operator.deployments.crd";

    /**
     * URL pointing to Operator role bindings file.
     */
    public static final String KIE_APP_OPERATOR_DEPLOYMENTS_RBAC = "kie.app.operator.deployments.rbac";

    /**
     * URL pointing to Operator definition file.
     */
    public static final String KIE_APP_OPERATOR_DEPLOYMENTS_OPERATOR = "kie.app.operator.deployments.operator";

    /**
     * URL pointing to Operator deployment file containing Workbench and Kie server.
     */
    public static final String KIE_APP_OPERATOR_DEPLOYMENTS_WORKBENCH_KIE_SERVER = "kie.app.operator.deployments.workbench.kie-server";

    /**
     * URL pointing to Operator deployment file containing clustered Monitoring console with clustered Smart router and two Kie servers with two databases.
     */
    public static final String KIE_APP_OPERATOR_DEPLOYMENTS_CLUSTERED_WORKBENCH_KIE_SERVER_DATABASE_PERSISTENT = "kie.app.operator.deployments.clustered-workbench-monitoring.smartrouter.two-kieservers.two-databases";

    @Override
    public void initConfigProperties() {
        // nothing to init here
    }
}
