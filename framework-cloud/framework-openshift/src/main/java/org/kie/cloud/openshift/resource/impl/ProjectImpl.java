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

package org.kie.cloud.openshift.resource.impl;

import static java.util.stream.Collectors.toList;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.client.OpenShiftClient;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.Route;
import org.kie.cloud.openshift.resource.Service;

public class ProjectImpl implements Project {

    private OpenShiftClient client;
    private String projectName;

    private static String defaultRoutingSubdomain = null;

    public ProjectImpl(OpenShiftClient client, String projectName) {
        this.client = client;
        this.projectName = projectName;
    }

    @Override
    public String getName() {
        return projectName;
    }

    @Override
    public void delete() {
        client.projects().withName(projectName).delete();
    }

    @Override
    public Service createService(String service) {
        return createService(service, OpenShiftResourceConstants.EAP_DEFAULT_PROTOCOL, OpenShiftResourceConstants.EAP_DEFAULT_HTTP_PORT);
    }

    @Override
    public Service createService(String service, String protocol, int... ports) {
        List<ServicePort> servicePorts = Arrays.stream(ports).mapToObj(n -> createServicePort(n, protocol)).collect(toList());

        client.services().inNamespace(projectName).create(
                new ServiceBuilder()
                    .withNewMetadata()
                        .withName(service)
                        .withNamespace(projectName)
                    .endMetadata()
                    .withNewSpec()
                        .withPorts(servicePorts)
                        // Pair service with deployment config
                        .withSelector(Collections.singletonMap("deploymentconfig", service))
                    .endSpec()
                .build());

        return new ServiceImpl(client, projectName, service);
    }

    private ServicePort createServicePort(int port, String protocol) {
        String portName = String.valueOf(port) + "-" + protocol.toLowerCase();
        ServicePort servicePort = new ServicePort(portName, null, port, protocol, new IntOrString(port));
        return servicePort;
    }

    @Override
    public List<Service> getServices() {
        return client.services().inNamespace(projectName).list().getItems().stream()
                .map(n -> new ServiceImpl(client, projectName, n.getMetadata().getName()))
                .collect(Collectors.toList());
    }

    @Override
    public Service getService(String serviceName) {
        io.fabric8.kubernetes.api.model.Service service = client.services().inNamespace(projectName).withName(serviceName).get();
        if(service != null) {
            return new ServiceImpl(client, projectName, serviceName);
        }
        return null;
    }

    @Override
    public void processTemplateAndCreateResources(URL templateUrl, Map<String, String> envVariables) {
        KubernetesList resourceList = client.templates().inNamespace(projectName).load(templateUrl).process(envVariables);
        client.lists().inNamespace(projectName).create(resourceList);
    }

    @Override
    public void processTemplateAndCreateResources(InputStream templateInputStream, Map<String, String> envVariables) {
        KubernetesList resourceList = client.templates().inNamespace(projectName).load(templateInputStream).process(envVariables);
        client.lists().inNamespace(projectName).create(resourceList);
    }

    @Override
    public void createResources(String resourceUrl) {
        try {
            KubernetesList resourceList = client.lists().inNamespace(projectName).load(new URL(resourceUrl)).get();
            client.lists().inNamespace(projectName).create(resourceList);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed resource URL", e);
        }
    }

    @Override
    public void createResources(InputStream inputStream) {
        KubernetesList resourceList = client.lists().inNamespace(projectName).load(inputStream).get();
        client.lists().inNamespace(projectName).create(resourceList);
    }

    @Override
    public String getDefaultRoutingSubdomain() {
        if (defaultRoutingSubdomain == null) {
            final String tempServiceName = "temp-service";
            Service tempService = createService(tempServiceName);
            Route tempRoute = tempService.createRoute();

            String tempRouteHost = tempRoute.getRouteHost();
            // Remove service name and project from the route host, the rest is the subdomain
            defaultRoutingSubdomain = tempRouteHost.replace(tempServiceName + "-" + projectName, "");

            tempRoute.delete();
            tempService.delete();
        }
        return defaultRoutingSubdomain;
    }
}
