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

package org.kie.cloud.openshift.resource;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import cz.xtf.core.openshift.OpenShift;

/**
 * Project representation.
 */
public interface Project extends AutoCloseable {

    /**
     * @return Project name.
     */
    public String getName();

    /**
     * Delete OpenShift project.
     */
    public void delete();

    /**
     * @return OpenShift client.
     */
    public OpenShift getOpenShift();

    /**
     * Process template and create all resources defined there.
     *
     * @param templateUrl URL of template to be processed
     * @param envVariables Map of environment variables to override default values from the template
     */
    public void processTemplateAndCreateResources(URL templateUrl, Map<String, String> envVariables);

    /**
     * Process APB and create all resources defined there.
     * @param image APB Image to be provision
     * @param extraVars Map of extra vars to override default values from the APB image
     */
    public void processApbRun(String image, Map<String, String> extraVars);

    /**
     * Create resources from YAML file using command line client.
     * @param yamlUrl Url to yaml file with resources
     */
    public void createResourcesFromYaml(String yamlUrl);

    /**
     * Create resources from YAML files using command line client.
     * @param yamlUrls Urls to yaml files with resources
     */
    public void createResourcesFromYaml(List<String> yamlUrls);

    /**
     * Create resources from YAML files as admin using command line client.
     * @param yamlUrl Url to yaml files with resources
     */
    public void createResourcesFromYamlAsAdmin(String yamlUrl);

    /**
     * Create resources from YAML files as admin using command line client.
     * @param yamlUrls Url to yaml files with resources
     */
    public void createResourcesFromYamlAsAdmin(List<String> yamlUrls);

    /**
     * Create all resources defined in resource URL.
     *
     * @param resourceUrl URL of resource list to be created
     */
    public void createResources(String resourceUrl);

    /**
     * Create all resources defined in resource URL.
     *
     * @param inputStream Input stream with resource list to be created
     */
    public void createResources(InputStream inputStream);

    /**
     * Create image stream in current project.
     *
     * @param imageStreamName Name of image stream
     * @param imageTag Image tag used to resolve image,for example Docker tag.
     */
    public void createImageStream(String imageStreamName, String imageTag);

    /**
     * Run oc command
     * @param args Command parameters
     * @return Output of oc
     */
    public String runOcCommand(String... args);

    /**
     * Run oc command as admin
     * @param args Command parameters
     * @return Output of oc
     */
    public String runOcCommandAsAdmin(String... args);
}
