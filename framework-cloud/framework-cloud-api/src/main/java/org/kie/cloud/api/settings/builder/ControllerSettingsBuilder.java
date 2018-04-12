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
package org.kie.cloud.api.settings.builder;

import org.kie.cloud.api.settings.DeploymentSettings;

/**
 * Cloud settings builder for Controller.
 *
 * If any environment variable isn't configured by SettingsBuilder, then default
 * value from application template is used.
 */
public interface ControllerSettingsBuilder extends SettingsBuilder<DeploymentSettings> {

    /**
     * Return configured builder with application name.
     *
     * @param name Application name.
     * @return Builder
     */
    ControllerSettingsBuilder withApplicationName(String name);

    /**
     * Return configured builder with Controller user.
     *
     * @param username Controller username.
     * @param password Controller password.
     * @return Builder
     */
    ControllerSettingsBuilder withControllerUser(String username, String password);

    /**
     * Return configured builder with Kie Server user.
     *
     * @param username Kie Server username.
     * @param password Kie Server password.
     * @return Builder
     */
    ControllerSettingsBuilder withKieServerUser(String username, String password);

    /**
     * Return configured builder with set host route. Custom hostname for http
     * service route.
     *
     * @param http HTTP Hostname.
     * @return Builder
     */
    ControllerSettingsBuilder withHostame(String http);

}
