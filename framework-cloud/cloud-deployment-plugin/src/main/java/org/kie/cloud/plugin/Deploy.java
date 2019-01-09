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

import static org.kie.cloud.plugin.Constants.BUILD_PROPERTIES_WORKBENCH_CONTEXT_ROOT;
import static org.kie.cloud.plugin.Constants.BUILD_PROPERTIES_WORKBENCH_IP;
import static org.kie.cloud.plugin.Constants.BUILD_PROPERTIES_WORKBENCH_PASSWORD;
import static org.kie.cloud.plugin.Constants.BUILD_PROPERTIES_WORKBENCH_PORT;
import static org.kie.cloud.plugin.Constants.BUILD_PROPERTIES_WORKBENCH_USERNAME;
import static org.kie.cloud.plugin.Constants.PROPERTY_FILE_PATH;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;

@Mojo(name = "deploy")
public class Deploy extends AbstractMojo {

    private final String BUILD_PROPERTIES_FILENAME = "build.properties";
    private final int MAX_LEVEL_BUILD_PROPERTIES = 10;

    @Parameter(property = "cloud-api-implementation", required = true)
    private String implementation;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    @Override public void execute() throws MojoExecutionException, MojoFailureException {

        DeploymentScenarioBuilderFactory deploymentScenarioBuilderFactory = DeploymentScenarioBuilderFactoryLoader.getInstance(implementation);

        WorkbenchKieServerScenario scenario = deploymentScenarioBuilderFactory.getWorkbenchKieServerScenarioBuilder().build();
        scenario.deploy();
        WorkbenchDeployment workbenchDeployment = scenario.getWorkbenchDeployment();

        writeOpenshiftProperties(workbenchDeployment);
        writeBuildProperties(workbenchDeployment);
    }

    private void writeOpenshiftProperties(WorkbenchDeployment workbenchDeployment) {
        CloudDeploymentPluginConfiguration cloudDeploymentPluginConfiguration = new CloudDeploymentPluginConfiguration();
        cloudDeploymentPluginConfiguration.setNamespace(workbenchDeployment.getNamespace());
        cloudDeploymentPluginConfiguration.setCloudAPIImplementation(implementation);

        cloudDeploymentPluginConfiguration.saveAsProperties(new File(PROPERTY_FILE_PATH));
    }

    private void writeBuildProperties(WorkbenchDeployment workbenchDeployment) {
        File buildPropertiesFile = findBuildProperties();
        Properties buildProperties = new Properties();

        try (InputStream inputStream = new FileInputStream(buildPropertiesFile)) {
            buildProperties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error loading build properties", e);
        }

        buildProperties.setProperty(BUILD_PROPERTIES_WORKBENCH_IP, workbenchDeployment.getUrl().get().getHost());
        buildProperties.setProperty(BUILD_PROPERTIES_WORKBENCH_PORT, String.valueOf(extractPort(workbenchDeployment.getUrl().get())));
        buildProperties.setProperty(BUILD_PROPERTIES_WORKBENCH_CONTEXT_ROOT, extractContextRoot(workbenchDeployment.getUrl().get()));
        buildProperties.setProperty(BUILD_PROPERTIES_WORKBENCH_USERNAME, workbenchDeployment.getUsername());
        buildProperties.setProperty(BUILD_PROPERTIES_WORKBENCH_PASSWORD, workbenchDeployment.getPassword());

        try (OutputStream outputStream = new FileOutputStream(buildPropertiesFile)) {
            buildProperties.store(outputStream, null);
        } catch (IOException e) {
            throw new RuntimeException("Error loading build properties", e);
        }
    }

    /**
     * @param url url of workbench deployment
     * @return port of workbench deployment
     */
    private int extractPort(URL url) {
        int port = url.getPort();
        if (port == -1) { //Port is not explicitly specified in the URL
            String protocol = url.getProtocol();
            switch (protocol) {
                case "http":
                    return 80;
                case "https":
                    return 443;
                default:
                    throw new IllegalArgumentException("Unrecognized protocol '" + protocol + "' in workbench URL: " + url);
            }
        } else {
            return port;
        }
    }

    /**
     * @param url url of the workbench deployment
     * @return context root extracted from given url. For example return
     * "kie-wb" for input http://myserver.com:8080/kie-wb/other/path/pieces
     * ""       for input http://myserver.com:8080/
     */
    private String extractContextRoot(URL url) {
        String path = url.getPath();
        if (path.contains("/")) {
            return path.substring(0, path.indexOf('/'));
        } else {
            return path;
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
