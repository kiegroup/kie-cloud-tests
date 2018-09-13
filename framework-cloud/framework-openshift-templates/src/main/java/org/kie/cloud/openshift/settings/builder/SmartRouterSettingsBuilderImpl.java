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
package org.kie.cloud.openshift.settings.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.SmartRouterSettingsBuilder;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.settings.DeploymentSettingsImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;

public class SmartRouterSettingsBuilderImpl implements SmartRouterSettingsBuilder {

    private Map<String, String> envVariables;
    private final OpenShiftTemplate appTemplate = OpenShiftTemplate.SMARTROUTER;

    public SmartRouterSettingsBuilderImpl() {
        envVariables = new HashMap<>();

        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, DeploymentConstants.getControllerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, DeploymentConstants.getControllerPassword());
    }

    @Override
    public DeploymentSettings build() {
        return new DeploymentSettingsImpl(envVariables, appTemplate);
    }

    @Override
    public SmartRouterSettingsBuilder withApplicationName(String name) {
        envVariables.put(OpenShiftTemplateConstants.APPLICATION_NAME, name);
        return this;
    }

    @Override
    public SmartRouterSettingsBuilder withControllerUser(String username, String password) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, username);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, password);
        return this;
    }

    @Override
    public SmartRouterSettingsBuilder withSmartRouterID(String id) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_ID, id);
        return this;
    }

    @Override
    public SmartRouterSettingsBuilder withSmartRouterName(String name) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_NAME, name);
        return this;
    }

    @Override
    public SmartRouterSettingsBuilder withSmarRouterConfig(String host, String port) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HOST, host);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_PORT, port);
        return this;
    }

    @Override
    public SmartRouterSettingsBuilder withControllerConnection(String host, String port) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_HOST, host);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PORT, port);
        return this;
    }

    @Override
    public SmartRouterSettingsBuilder withControllerConnection(String serviceName) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_SERVICE, serviceName);
        return this;
    }

    @Override
    public SmartRouterSettingsBuilder withSmartRouterExternalUrl(String url) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_URL_EXTERNAL, url);
        return this;
    }

    @Override
    public SmartRouterSettingsBuilder withHostame(String http) {
        envVariables.put(OpenShiftTemplateConstants.SMART_ROUTER_HOSTNAME_HTTP, http);
        return this;
    }

}
