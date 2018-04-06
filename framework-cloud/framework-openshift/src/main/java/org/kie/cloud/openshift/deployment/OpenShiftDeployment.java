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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.xtf.openshift.OpenShiftUtil;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;

import java.time.Duration;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.DeploymentTimeoutException;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;
import org.kie.cloud.openshift.resource.Project;

public abstract class OpenShiftDeployment implements Deployment {

    private OpenShiftUtil util;
    private Project project;

    public OpenShiftDeployment(Project project) {
        this.project = project;
        this.util = project.getOpenShiftUtil();
    }

    public OpenShiftUtil getOpenShiftUtil() {
        return util;
    }

    public void setOpenShiftUtil(OpenShiftUtil util) {
        this.util = util;
    }

    @Override
    public String getNamespace() {
        return project.getName();
    }

    @Override
    public void deleteInstances(Instance... instance) {
        deleteInstances(Arrays.asList(instance));
    }

    @Override
    public void deleteInstances(List<Instance> instances) {
        for (Instance instance : instances) {
            Pod pod = util.getPod(instance.getName());
            util.deletePod(pod);
        }
    }

    public abstract String getServiceName();

    @Override
    public void scale(int instances) {
        util.client().deploymentConfigs().inNamespace(getNamespace()).withName(getServiceName()).scale(instances, true);
        // Wait flag while scaling of deployment config doesn't seem to work correctly, use own waiting functionality
        waitForScale();
    }

    @Override
    public boolean isReady() {
        try {
            Service service = util.getService(getServiceName());
            return service != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Instance> getInstances() {
        if (isReady()) {
            // Deployment config has a same name as its service.
            String deploymentConfigName = getServiceName();

            List<Instance> instances = util.getPods().stream()
                    .filter(pod -> {
                        String podsDeploymentConfigName = pod.getMetadata().getLabels().get(OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL);
                        return deploymentConfigName.equals(podsDeploymentConfigName);
                    })
                    .map(pod -> createInstance(pod))
                    .collect(toList());

            return instances;
        }

        return Collections.emptyList();
    }

    @Override
    public void waitForScale() {
        waitUntilAllPodsAreReadyAndRunning();
    }

    private void waitUntilAllPodsAreReadyAndRunning() {
        int expectedPods = util.getDeploymentConfig(getServiceName()).getSpec().getReplicas().intValue();

        try {
            util.waiters()
                    .areExactlyNPodsReady(expectedPods, OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL, getServiceName())
                    .timeout(OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT)
                    .assertEventually("Pods for service " + getServiceName() + " are not ready.");

            util.waiters()
                    .areExactlyNPodsRunning(expectedPods, OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL, getServiceName())
                    .timeout(OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT)
                    .assertEventually("Pods for service " + getServiceName() + " are not runnning.");
        } catch (AssertionError e) {
            throw new DeploymentTimeoutException("Timeout while waiting for pods to start.");
        }
    }

    @Override
    public void setRouterTimeout(Duration timeoutValue) {
        // Route has a same name as its service.
        String routeName = getServiceName();
        util.client()
            .routes()
            .withName(routeName)
            .edit()
            .editMetadata()
            .addToAnnotations(OpenShiftConstants.HAPROXY_ROUTER_TIMEOUT, timeoutValue.getSeconds()+"s")
            .endMetadata()
            .done();
    }

    private Instance createInstance(Pod pod) {
        String instanceName = pod.getMetadata().getName();

        return new OpenShiftInstance(util, getNamespace(), instanceName);
    }

    protected URL getHttpRouteUrl(String serviceName) {
        try {
            return getRouteUri("http", serviceName).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected URL getHttpsRouteUrl(String serviceName) {
        try {
            return getRouteUri("https", serviceName).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected URI getWebSocketRouteUri(String serviceName) {
        return getRouteUri("ws", serviceName);
    }

    private URI getRouteUri(String protocol, String serviceName) {
        URI uri;
        Service service = util.getService(serviceName);

        String routeHost = null;
        if(service == null) {
            // Service doesn't exist, create URL using default subdomain
            String defaultRoutingSubdomain = DeploymentConstants.getDefaultDomainSuffix();
            routeHost = getServiceName() + "-" + getNamespace() + defaultRoutingSubdomain;
        } else {
            routeHost = util.getRoute(serviceName).getSpec().getHost();
        }
        String uriValue = protocol + "://" + routeHost + ":" + retrievePort(protocol);

        try {
            uri = new URI(uriValue.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return uri;
    }

    private String retrievePort(String protocol) {
        switch (protocol) {
            case "http":
            case "ws":
                return "80";
            case "https":
                return "443";
            default:
                throw new IllegalArgumentException("Unrecognized protocol '" + protocol + "'");
        }
    }
}
