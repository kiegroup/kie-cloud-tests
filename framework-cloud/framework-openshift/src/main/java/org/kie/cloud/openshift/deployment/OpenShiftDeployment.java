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
package org.kie.cloud.openshift.deployment;

import static java.util.stream.Collectors.toList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.fabric8.kubernetes.api.model.Pod;
import java.time.Duration;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;
import org.kie.cloud.openshift.resource.Service;

public abstract class OpenShiftDeployment implements Deployment {

    protected OpenShiftController openShiftController;
    protected String namespace;

    public OpenShiftController getOpenShiftController() {
        return openShiftController;
    }

    public void setOpenShiftController(OpenShiftController openShiftController) {
        this.openShiftController = openShiftController;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public void deleteInstances(Instance... instance) {
        deleteInstances(Arrays.asList(instance));
    }

    @Override
    public void deleteInstances(List<Instance> instances) {
        for (Instance instance : instances) {
            openShiftController.getClient().pods().inNamespace(namespace).withName(instance.getName()).withGracePeriod(0).delete();
        }
    }

    public abstract String getServiceName();

    @Override
    public void scale(int instances) {
        openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().scalePods(instances);
    }

    @Override
    public boolean isReady() {
        try {
            Service service = openShiftController.getProject(namespace).getService(getServiceName());
            return service != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Instance> getInstances() {
        if (isReady()) {
            String deploymentConfigName = openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().getName();
            List<Pod> pods = openShiftController.getClient().pods().inNamespace(namespace).withLabel(OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL, deploymentConfigName).list().getItems();

            List<Instance> instances = pods.stream().map((pod) -> {
                return createInstance(pod);
            }).collect(toList());

            return instances;
        }

        return Collections.emptyList();
    }

    @Override
    public void setRouterTimeout(Duration timeoutValue) {
        openShiftController.getClient()
                .routes()
                .inNamespace(getNamespace())
                .withName(getServiceName())
                .edit()
                .editMetadata()
                .addToAnnotations(OpenShiftConstants.HAPROXY_ROUTER_TIMEOUT, timeoutValue.getSeconds()+"s")
                .endMetadata()
                .done();
    }

    private Instance createInstance(Pod pod) {
        OpenShiftInstance instance = new OpenShiftInstance();
        instance.setOpenShiftController(openShiftController);
        instance.setNamespace(namespace);
        instance.setName(pod.getMetadata().getName());
        return instance;
    }

    protected URL getHttpRouteUrl(String serviceName) {
        return getRouteUrl("http", serviceName);
    }

    protected URL getHttpsRouteUrl(String serviceName) {
        return getRouteUrl("https", serviceName);
    }

    private URL getRouteUrl(String protocol, String serviceName) {
        URL url;
        Service service = openShiftController.getProject(namespace).getService(serviceName);

        String routeHost = null;
        if(service == null) {
            // Service doesn't exist, create URL using default subdomain
            String defaultRoutingSubdomain = openShiftController.getProject(namespace).getDefaultRoutingSubdomain();
            routeHost = getServiceName() + "-" + namespace + defaultRoutingSubdomain;
        } else {
            routeHost = service.getRoute().getRouteHost();
        }
        String urlValue = protocol + "://" + routeHost + ":" + retrievePort(protocol);

        try {
            url = new URL(urlValue.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return url;
    }

    private String retrievePort(String protocol) {
        switch (protocol) {
            case "http":
                return "80";
            case "https":
                return "443";
            default:
                throw new IllegalArgumentException("Unrecognized protocol '" + protocol + "'");
        }
    }
}
