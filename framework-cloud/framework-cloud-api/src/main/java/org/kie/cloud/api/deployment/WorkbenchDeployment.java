/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.api.deployment;

import java.net.URI;
import java.net.URL;
import java.util.Optional;

/**
 * Kie Workbench deplyoment representation in cloud.
 */
public interface WorkbenchDeployment extends Deployment {

    /**
     * Get URL for Kie Workbench service (deployment).
     *
     * @return Kie Workbench URL
     */
    URL getUrl();

    /**
     * Get HTTP URL for Kie Workbench service (deployment).
     *
     * @return Kie Workbench URL
     */
    Optional<URL> getInsecureUrl();

    /**
     * Get HTTPS URL for Kie Workbench service (deployment).
     *
     * @return Kie Workbench URL
     */
    Optional<URL> getSecureUrl();

    /**
     * Get WebSocket URI for Kie Workbench service (deployment).
     *
     * @return Workbench URI
     */
    Optional<URI> getWebSocketUri();

    /**
     * Get Kie Workbench user name. Workbench username is set by env. variable
     * org.kie.workbench.user
     *
     * @return Workbench user name
     */
    String getUsername();

    /**
     * Set Kie Workbench user name. This will override Workbench username 
     * set by env. variable org.kie.server.user
     * 
     * @param username Workbench user name
     * @return
     */
    void setUsername(String username);

    /**
     * Get Kie Workbench user password. Workbench password is set by env.
     * variable org.kie.workbench.user
     *
     * @return Workbench user password
     */
    String getPassword();
}
