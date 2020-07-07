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
package org.kie.cloud.openshift.operator.database.external;

import java.util.Optional;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.database.external.ExternalDatabase;
import org.kie.cloud.openshift.operator.model.components.Database;
import org.kie.cloud.openshift.operator.model.components.ExternalConfig;

public interface OperatorExternalDatabase extends ExternalDatabase {

    default Database getDatabaseModel() {

        Database database = new Database();
        database.setType("external");

        ExternalConfig config = new ExternalConfig();
        if (needsToSetExternalUrl()) {
            config.setJdbcURL(DeploymentConstants.getDatabaseUrl());
        } else {
            config.setHost(DeploymentConstants.getDatabaseHost());
            config.setPort(DeploymentConstants.getDatabasePort());
        }

        config.setDriver(getExternalDriver().getName());
        config.setDialect(Optional.ofNullable(getHibernateDialect()).orElse(DeploymentConstants.getHibernatePersistenceDialect()));
        config.setUsername(DeploymentConstants.getDatabaseUsername());
        config.setPassword(DeploymentConstants.getDatabasePassword());
        config.setName(DeploymentConstants.getExternalDatabaseName());
        database.setExternalConfig(config);

        return database;
    }

    /**
     * @return Flag to indicate that the jdbcURL field needs to be populated (default true).
     */
    default boolean needsToSetExternalUrl() {
        return true;
    }
}
