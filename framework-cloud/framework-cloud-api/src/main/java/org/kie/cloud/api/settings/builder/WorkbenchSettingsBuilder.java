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

/**
 * Cloud settings builder for Workbench.
 *
 * If any environment variable isn't configured by SettingsBuilder, then default
 * value from application template is used.
 */
public interface WorkbenchSettingsBuilder extends SettingsBuilder<DeploymentSettings> {

    /**
     * Return configured builder with Kie Admin user. This user is used as admin
     * for workbench.
     *
     * @param user
     * @param password
     * @return
     */
    WorkbenchSettingsBuilder withAdminUser(String user, String password);

    /**
     * Return configured builder with application name.
     *
     * @param name Application name.
     * @return Builder
     */
    WorkbenchSettingsBuilder withApplicationName(String name);

    /**
     * Return configured builder with Controller user.
     *
     * @param username Controller username.
     * @param password Controller password.
     * @return Builder
     */
    WorkbenchSettingsBuilder withControllerUser(String username, String password);

    /**
     * Return configured builder with Kie Server user.
     *
     * @param username
     * @param password
     * @return
     */
    WorkbenchSettingsBuilder withKieServerUser(String username, String password);

    /**
     * Return configured builder with set host route. Custom hostname for http
     * service route.
     *
     * @param http
     * @return
     */
    WorkbenchSettingsBuilder withHostame(String http);

    /**
     * Return configured builder with set secured host route. Custom hostname
     * for https service route.
     *
     * @param https
     * @return
     */
    WorkbenchSettingsBuilder withSecuredHostame(String https);

    /**
     * Return configure builder with Maven repo service user.
     *
     * @param workbenchMavenUser
     * @param workbenchMavenPassword
     * @return
     */
    WorkbenchSettingsBuilder withMavenRepoServiceUser(String workbenchMavenUser, String workbenchMavenPassword);
}
