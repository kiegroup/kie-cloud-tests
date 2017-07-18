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

import static org.kie.cloud.plugin.Constants.BUILD_PROPERTIES_BUSINESS_CENTRAL_IP;
import static org.kie.cloud.plugin.Constants.BUILD_PROPERTIES_BUSINESS_CENTRAL_PORT;
import static org.kie.cloud.plugin.Constants.NAMESPACE_PROPERTY;
import static org.kie.cloud.plugin.Constants.PROPERTY_FILE_PATH;
import static org.kie.cloud.plugin.Constants.WORKBENCH_PASSWORD_PROPERTY;
import static org.kie.cloud.plugin.Constants.WORKBENCH_URL_PROPERTY;
import static org.kie.cloud.plugin.Constants.WORKBENCH_USERNAME_PROPERTY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;

@Mojo(name = "deploy")
public class Deploy extends AbstractMojo {

    private String BUILD_PROPERTIES_FILENAME = "build.properties";
    private int MAX_LEVEL_BUILD_PROPERTIES = 10;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    @Override public void execute() throws MojoExecutionException, MojoFailureException {

        DeploymentScenarioBuilderFactory deploymentScenarioBuilderFactory = DeploymentScenarioBuilderFactory.getInstance();

        WorkbenchWithKieServerScenario scenario = deploymentScenarioBuilderFactory.getWorkbenchWithKieServerScenarioBuilder().build();
        scenario.deploy();
        WorkbenchDeployment workbenchDeployment = scenario.getWorkbenchDeployment();

        writeOpenshiftProperties(workbenchDeployment);
        writeBuildProperties(workbenchDeployment);
    }

    private void writeOpenshiftProperties(WorkbenchDeployment workbenchDeployment) {
        Properties properties = new Properties();

        properties.put(NAMESPACE_PROPERTY, workbenchDeployment.getNamespace());
        properties.put(WORKBENCH_URL_PROPERTY, workbenchDeployment.getUrl().toString());
        properties.put(WORKBENCH_USERNAME_PROPERTY, workbenchDeployment.getUsername());
        properties.put(WORKBENCH_PASSWORD_PROPERTY, workbenchDeployment.getPassword());

        try (OutputStream outputStream = new FileOutputStream(mavenProject.getModel().getBuild().getOutputDirectory() + PROPERTY_FILE_PATH)) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            throw new RuntimeException("Error saving properties", e);
        }
    }

    private void writeBuildProperties(WorkbenchDeployment workbenchDeployment) {
        File buildPropertiesFile = findBuildProperties();
        Properties buildProperties = new Properties();

        try (InputStream inputStream = new FileInputStream(buildPropertiesFile)) {
            buildProperties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error loading build properties", e);
        }

        buildProperties.setProperty(BUILD_PROPERTIES_BUSINESS_CENTRAL_IP, workbenchDeployment.getUrl().getHost());
        buildProperties.setProperty(BUILD_PROPERTIES_BUSINESS_CENTRAL_PORT, String.valueOf(workbenchDeployment.getUrl().getPort()));
        buildProperties.put(WORKBENCH_USERNAME_PROPERTY, workbenchDeployment.getUsername());
        buildProperties.put(WORKBENCH_PASSWORD_PROPERTY, workbenchDeployment.getPassword());

        try (OutputStream outputStream = new FileOutputStream(buildPropertiesFile)) {
            buildProperties.store(outputStream, null);
        } catch (IOException e) {
            throw new RuntimeException("Error loading build properties", e);
        }
    }

    private File findBuildProperties() {
        File buildProperties = new File(mavenProject.getModel().getBuild().getDirectory() + "/" + BUILD_PROPERTIES_FILENAME);
        for (int level = 0; level < MAX_LEVEL_BUILD_PROPERTIES; level++) {
            if (buildProperties.isFile()) {
                return buildProperties;
            } else {
                String fileName = buildProperties.getName();
                File path = buildProperties.getParentFile().getParentFile();
                buildProperties = new File(path, fileName);
            }
        }

        throw new RuntimeException("Can not find build properties file");
    }
}
