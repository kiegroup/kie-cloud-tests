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
package org.kie.cloud.openshift.database.external;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;

public interface TemplateExternalDatabase extends ExternalDatabase {

    /**
     * @return All environment variables required for connection to this database.
     */
    default Map<String, String> getExternalDatabaseEnvironmentVariables() {
        Map<String, String> envVariables = new HashMap<>();
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_EXTERNALDB_DRIVER, getExternalDriver().getName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_EXTERNALDB_SERVICE_HOST, DeploymentConstants.getDatabaseHost());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_EXTERNALDB_SERVICE_PORT, DeploymentConstants.getDatabasePort());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_EXTERNALDB_DB, DeploymentConstants.getExternalDatabaseName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_EXTERNALDB_USER, DeploymentConstants.getDatabaseUsername());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_EXTERNALDB_PWD, DeploymentConstants.getDatabasePassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_EXTERNALDB_DIALECT, Optional.ofNullable(getHibernateDialect()).orElse(DeploymentConstants.getHibernatePersistenceDialect()));
        if (needsToSetExternalUrl()) {
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_EXTERNALDB_URL, DeploymentConstants.getDatabaseUrl());
        }

        return envVariables;
    }

    /**
     * @return Flag to indicate that the KIE_SERVER_EXTERNALDB_URL argument needs to be populated (default true).
     */
    default boolean needsToSetExternalUrl() {
        return true;
    }
}
