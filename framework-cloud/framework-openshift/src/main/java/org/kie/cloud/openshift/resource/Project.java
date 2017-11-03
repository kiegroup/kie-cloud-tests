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

import org.kie.cloud.openshift.image.ImageStream;

/**
 * Project representation.
 */
public interface Project {

    /**
     * @return Project name.
     */
    public String getName();

    /**
     * Delete OpenShift project.
     */
    public void delete();

    /**
     * Create new service.
     *
     * @param service Service name.
     * @return Created service.
     */
    public Service createService(String service);

    /**
     * Create new service.
     *
     * @param service Service name.
     * @param protocol Service communication protocol, usually TCP.
     * @param ports Ports available from outside.
     * @return Created service.
     */
    public Service createService(String service, String protocol, int... ports);

    /**
     * @return List of services available in this project.
     */
    public List<Service> getServices();

    /**
     * @param serviceName Service name.
     * @return Service object corresponding to this name.
     */
    public Service getService(String serviceName);

    /**
     * Process template and create all resources defined there.
     *
     * @param templateUrl URL of template to be processed
     * @param envVariables Map of environment variables to override default values from the template
     */
    public void processTemplateAndCreateResources(URL templateUrl, Map<String, String> envVariables);

    /**
     * Process template and create all resources defined there.
     *
     * @param templateInputStream Input stream with template to be processed
     * @param envVariables Map of environment variables to override default values from the template
     */
    public void processTemplateAndCreateResources(InputStream templateInputStream, Map<String, String> envVariables);

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
     * Get default subdomain configured for OpenShift instance.
     * This value is used for assuming route URL before the route is created.
     *
     * @return Default routing subdomain.
     */
    public String getDefaultRoutingSubdomain();

    /**
     * Get image associated with image stream.
     *
     * @param imageStream Image stream.
     * @return Image referenced by image stream available in this project.
     */
    public Image getImage(ImageStream imageStream);
}
