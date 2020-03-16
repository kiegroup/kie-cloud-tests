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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;

public class ExternalDatabaseProvider<T extends ExternalDatabase> {

    private final Collection<T> availableExternalDatabases;

    public ExternalDatabaseProvider(Collection<T> databases) {
        this.availableExternalDatabases = Collections.unmodifiableCollection(databases);
    }

    public T getExternalDatabase() {
        String databaseDriver = DeploymentConstants.getDatabaseDriver();
        if (databaseDriver == null || databaseDriver.isEmpty()) {
            throw new RuntimeException("System property " + DeploymentConstants.DATABASE_DRIVER + " is not defined. Cannot retrieve external database instance.");
        }

        return availableExternalDatabases.stream().filter(d -> d.getDriverName().equals(databaseDriver))
                                         .findAny()
                                         .orElseThrow(() -> {
                                             List<String> driverNames = availableExternalDatabases.stream().map(d -> d.getDriverName()).collect(Collectors.toList());
                                             return new RuntimeException("External database with driver identifier " + databaseDriver + " is not found. Available driver identifiers: " + driverNames);
                                         });
    }

}
