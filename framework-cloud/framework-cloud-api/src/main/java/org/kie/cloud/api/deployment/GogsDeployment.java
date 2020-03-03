/*
 * Copyright 2020 JBoss by Red Hat.
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

/**
 * Gogs deployment representation in cloud.
 */
public interface GogsDeployment extends Deployment {

    /**
     * Get URL for Gogs service (deployment).
     *
     * @return Docker URL
     */
    String getUrl();

    /**
     * @return get admin user to connect with Gogs instance.
     */
    String getUsername();

    /**
     * @return get admin password to connect with Gogs instance.
     */
    String getPassword();
}
