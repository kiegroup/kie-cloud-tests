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
package org.kie.cloud.openshift.settings;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.openshift.template.OpenShiftTemplate;

public class DeploymentSettingsImpl implements DeploymentSettings {
    private Map<String, String> envVariables;
    private OpenShiftTemplate appTemplate;

    public DeploymentSettingsImpl() {
        envVariables = new HashMap<>();
    }

    public DeploymentSettingsImpl(Map<String, String> envVariables, OpenShiftTemplate appTemplate) {
        this.envVariables = envVariables;
        this.appTemplate = appTemplate;
    }

    @Override
    public Map<String, String> getEnvVariables() {
        return envVariables;
    }

    @Override
    public URL getDeploymentScriptUrl() {
        return appTemplate.getTemplateUrl();
    }
}
