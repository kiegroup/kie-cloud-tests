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

package org.kie.cloud.plugin;

import static org.kie.cloud.plugin.Constants.CLOUD_API_IMPLEMENTATION_PROPERTY;
import static org.kie.cloud.plugin.Constants.NAMESPACE_PROPERTY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.kie.cloud.api.scenario.DeploymentScenario;

public class CloudDeploymentPluginConfiguration {

    private String cloudAPIImplementation;
    private String namespace;
    private DeploymentScenario deploymentScenario;

    public String getCloudAPIImplementation() {
        return cloudAPIImplementation;
    }

    public void setCloudAPIImplementation(String cloudAPIImplementation) {
        this.cloudAPIImplementation = cloudAPIImplementation;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public DeploymentScenario getDeploymentScenario() {
        return deploymentScenario;
    }

    public void setDeploymentScenario(DeploymentScenario deploymentScenario) {
        this.deploymentScenario = deploymentScenario;
    }

    public void loadFromProperties(File propertiesFile) {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(propertiesFile)) {
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error loading cloud plugin configuration from file");
        }

        cloudAPIImplementation = loadFromProperty(CLOUD_API_IMPLEMENTATION_PROPERTY, properties);
        namespace = loadFromProperty(NAMESPACE_PROPERTY, properties);
    }

    public void saveAsProperties(File propertiesFile) {
        Properties properties = new Properties();
        properties.setProperty(CLOUD_API_IMPLEMENTATION_PROPERTY, cloudAPIImplementation);
        properties.setProperty(NAMESPACE_PROPERTY, namespace);

        try (OutputStream outputStream = new FileOutputStream(propertiesFile)) {
            properties.store(outputStream, null);
        } catch (Exception e) {
            throw new RuntimeException("Error saving cloud plugin configuration to file");
        }
    }

    private String loadFromProperty(String name, Properties properties) {
        if (properties.containsKey(name)) {
            return properties.getProperty(name);
        } else {
            throw new RuntimeException("Property file with plugin configuration does not contain key - " + name);
        }
    }
}
