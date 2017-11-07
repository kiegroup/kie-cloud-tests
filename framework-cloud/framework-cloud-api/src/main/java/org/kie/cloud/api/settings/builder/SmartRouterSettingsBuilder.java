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
package org.kie.cloud.api.settings.builder;

import org.kie.cloud.api.settings.DeploymentSettings;

public interface SmartRouterSettingsBuilder extends SettingsBuilder<DeploymentSettings> {

    /**
     * Return configured builder with application name.
     *
     * @param name Application name.
     * @return Builder
     */
    SmartRouterSettingsBuilder withApplicationName(String name);

    /**
     * Return configured builder with Controller user.
     *
     * @param username Controller username.
     * @param password Controller password.
     * @return Builder
     */
    SmartRouterSettingsBuilder withControllerUser(String username, String password);

    /**
     * Return configured builder with Smart router ID.
     *
     * @param id Smart router id.
     * @return Builder.
     */
    SmartRouterSettingsBuilder withSmartRouterID(String id);

    /**
     * Return configured builder with Smart router name.
     *
     * @param name Smart router name.
     * @return Builder.
     */
    SmartRouterSettingsBuilder withSmartRouterName(String name);

    /**
     * Return configured builder with connection for Smart router.
     *
     * @param host Smartrouter host URL.
     * @param port Smartrouter port.
     * @return Builder
     */
    SmartRouterSettingsBuilder withSmarRouterConfig(String host, String port);

    /**
     * Return configured builder with connection  to Controller.
     *
     * @param host Controller host URL
     * @param port Controller port.
     * @return Builder
     */
    SmartRouterSettingsBuilder withControllerConnection(String host, String port);

    /**
     * Return configured builder with connection  to Controller.
     *
     * @param serviceName Controller service name.
     * @return Builder
     */
    SmartRouterSettingsBuilder withControllerConnection(String serviceName);

    /**
     * Return configured builder with set external URL. Public URL where the
     * router can be found. (router property org.kie.server.router.url.external)
     *
     * @param url Smart router external URL
     * @return Builder
     */
    SmartRouterSettingsBuilder withSmartRouterExternalUrl(String url);

    /**
     * Return configured builder with set host route. Custom hostname for http
     * service route.
     *
     * @param http
     * @return
     */
    SmartRouterSettingsBuilder withHostame(String http);

}
