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

import java.util.Optional;

import org.kie.cloud.api.constants.Constants;

public class OpenShiftOperatorConstants implements Constants {

    /**
     * URL pointing to Operator custom resource definition file.
     */
    public static final String KIE_APP_OPERATOR_DEPLOYMENTS_CRD = "kie.app.operator.deployments.crd";

    /**
     * URL pointing to Operator service account.
     */
    public static final String KIE_APP_OPERATOR_DEPLOYMENTS_SERVICE_ACCOUNT = "kie.app.operator.deployments.service-account";

    /**
     * URL pointing to Operator role.
     */
    public static final String KIE_APP_OPERATOR_DEPLOYMENTS_ROLE = "kie.app.operator.deployments.role";

    /**
     * URL pointing to Operator role bindings file.
     */
    public static final String KIE_APP_OPERATOR_DEPLOYMENTS_ROLE_BINDING = "kie.app.operator.deployments.role-binding";

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

    /**
     * Custom registry for Kie deployments.
     */
    public static final String KIE_IMAGE_REGISTRY_CUSTOM = "kie.image.registry.custom";

    /**
     * Image tag for Kie Operator.
     */
    public static final String KIE_OPERATOR_IMAGE_TAG = "kie.operator.image.tag";

    /**
     * Image tag for Kie Operator to upgrade from.
     */
    public static final String KIE_OPERATOR_UPGRADE_FROM_IMAGE_TAG = "kie.operator.upgrade.from.image.tag";

    /**
     * Set to true if you don't need Kie Operator UI up and running. Skipping of this check can save around 30 seconds.
     */
    public static final String KIE_OPERATOR_CONSOLE_CHECK_SKIP = "kie.operator.console.check.skip";

    public static Optional<String> getKieImageRegistryCustom() {
        String kieOperatorImageTag = System.getProperty(KIE_IMAGE_REGISTRY_CUSTOM);
        if (kieOperatorImageTag != null && !kieOperatorImageTag.isEmpty()) {
            return Optional.of(kieOperatorImageTag);
        }
        return Optional.empty();
    }

    public static String getKieOperatorImageTag() {
        String kieOperatorImageTag = System.getProperty(KIE_OPERATOR_IMAGE_TAG);
        if (kieOperatorImageTag == null || kieOperatorImageTag.isEmpty()) {
            throw new RuntimeException("System property " + KIE_OPERATOR_IMAGE_TAG + " has to be defined so specific Kie Operator version is deployed.");
        }
        return kieOperatorImageTag;
    }

    public static String getKieOperatorUpgradeFromImageTag() {
        String imageTag = System.getProperty(KIE_OPERATOR_UPGRADE_FROM_IMAGE_TAG);
        if (imageTag == null || imageTag.isEmpty()) {
            throw new RuntimeException("System property " + KIE_OPERATOR_UPGRADE_FROM_IMAGE_TAG + " has to be defined to upgrade the Kie Operator from this version.");
        }
        return imageTag;
    }

    public static boolean skipKieOperatorConsoleCheck() {
        String skipKieOperatorConsoleCheck = System.getProperty(KIE_OPERATOR_CONSOLE_CHECK_SKIP, "false");
        return Boolean.valueOf(skipKieOperatorConsoleCheck);
    }

    @Override
    public void initConfigProperties() {
        // nothing to init here
    }
}
