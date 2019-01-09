/*
 * Copyright 2018 JBoss by Red Hat.
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
 * Controller deplyoment representation in cloud.
 */
public interface ControllerDeployment extends Deployment {

    /**
     * Get URL for Controller service (deployment).
     *
     * @return Workbench URL
     */
    Optional<URL> getUrl();

    /**
     * Get Controller user name. Controller username is set by env. variable
     * org.kie.controller.user
     *
     * @return Workbench user name
     */
    String getUsername();

    /**
     * Get Controller user password. Controller password is set by env. variable
     * org.kie.controller.pwd
     *
     * @return Workbench user password
     */
    String getPassword();
}
