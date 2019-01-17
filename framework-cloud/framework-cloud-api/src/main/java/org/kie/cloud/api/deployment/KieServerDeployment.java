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

import java.net.URL;
import java.util.Optional;

/**
 * Kie Server deplyoment representation in cloud.
 */
public interface KieServerDeployment extends Deployment {

    /**
     * Get URL for Kie Server service (deployment).
     *
     * @return Kie Server URL
     */
    URL getUrl();

    /**
     * Get HTTP URL for Kie Server service (deployment).
     *
     * @return Kie Server URL
     */
    Optional<URL> getInsecureUrl();

    /**
     * Get HTTPS URL for Kie Server service (deployment).
     *
     * @return Kie Server URL
     */
    Optional<URL> getSecureUrl();

    /**
     * Get Kie Server user name. Kie Server username is set by property
     * org.kie.server.user
     *
     * @return Kie Server user name
     */
    String getUsername();

    /**
     * Get Kie Server user password. Kie Server password is set by property
     * org.kie.server.pwd
     *
     * @return Kie Server user password
     */
    String getPassword();
}
