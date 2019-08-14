/*
 * Copyright 2019 JBoss by Red Hat.
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
 * Amq deployment representation in cloud.
 */
public interface AmqDeployment extends Deployment {

    /**
     * Get URL for Amq service (deployment).
     *
     * @return Amq URL
     */
    URL getUrl();

    /**
     * Get URL for Amq tcp service (deployment).
     *
     * @return Amq tcp URL
     */
    URL getTcpUrl();

    /**
     * Get HTTPS URL for Amq service (deployment).
     *
     * @return Amq URL
     */
    Optional<URL> getSecureUrl();

    /**
     * Get Amq user name.
     *
     * @return Amq user name
     */
    String getUsername();

    /**
     * Get Amq user password.
     *
     * @return Amq user password
     */
    String getPassword();
}
