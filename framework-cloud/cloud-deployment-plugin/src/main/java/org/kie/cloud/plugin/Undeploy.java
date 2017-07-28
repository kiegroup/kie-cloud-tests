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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;

import static org.kie.cloud.plugin.Constants.CLOUD_API_IMPLEMENTATION_PROPERTY;
import static org.kie.cloud.plugin.Constants.NAMESPACE_PROPERTY;
import static org.kie.cloud.plugin.Constants.PROPERTY_FILE_PATH;

@Mojo( name = "undeploy" )
public class Undeploy extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    @Override public void execute() throws MojoExecutionException, MojoFailureException {
        CloudDeploymentPluginConfiguration cloudDeploymentPluginConfiguration = new CloudDeploymentPluginConfiguration();
        cloudDeploymentPluginConfiguration.loadFromProperties(new File(PROPERTY_FILE_PATH));

        DeploymentScenarioBuilderFactory deploymentScenarioBuilderFactory = DeploymentScenarioBuilderFactoryLoader
                .getInstance(cloudDeploymentPluginConfiguration.getCloudAPIImplementation());
        deploymentScenarioBuilderFactory.deleteNamespace(cloudDeploymentPluginConfiguration.getNamespace());
    }
}
