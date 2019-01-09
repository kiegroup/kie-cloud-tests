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
package org.kie.cloud.api.deployment;

import java.net.URL;
import java.util.Optional;

/**
 * Database deplyoment representation in cloud.
 */
public interface DatabaseDeployment extends Deployment {

    /**
     * Get Database URL.
     *
     * @return Database URL.
     */
    Optional<URL> getUrl();

    /**
     * Get Database user name.
     *
     * @return Database user name.
     */
    String getUsername();

    /**
     * Get Database user passowd.
     *
     * @return Database user password.
     */
    String getPassword();

    /**
     * Return Database name.
     *
     * @return Database name.
     */
    String getDatabaseName();

}
