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

/**
 * Cloud settings builder. Create settings for Deployment scenario.
 *
 * @param <DeploymentSettings> Setup to be built e.g. DeploymentSettings
 * @see org.kie.cloud.api.settings.DeploymentSettings
 */
public interface SettingsBuilder<DeploymentSettings> {

    /**
     * Return built cloud settings.
     *
     * @return Returns configured for deployment.
     */
    DeploymentSettings build();
}
