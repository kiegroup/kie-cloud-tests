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
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.DeploymentTimeoutException;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.protocol.Protocol;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.xtf.openshift.OpenShiftUtil;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;

public abstract class OpenShiftDeployment implements Deployment {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftDeployment.class);

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

    @Override
    public void resetRouterTimeout() {
        // Route has a same name as its service.
        String routeName = getServiceName();
        util.client()
            .routes()
            .withName(routeName)
            .edit()
            .editMetadata()
            .removeFromAnnotations(OpenShiftConstants.HAPROXY_ROUTER_TIMEOUT)
            .endMetadata()
            .done();
    }

    private Instance createInstance(Pod pod) {
        String instanceName = pod.getMetadata().getName();

        return new OpenShiftInstance(util, getNamespace(), instanceName);
    }

    protected URL getHttpRouteUrl(String serviceName) {
        try {
            return getRouteUri(Protocol.http, serviceName).toURL();
        } catch (RuntimeException e) {
            if (e.getMessage().contains(Protocol.http + " route leading to service " + serviceName + " not found")) {
                logger.debug("Http route not found. Try to find Https route instead.");
                return getHttpsRouteUrl(serviceName);
            } else {
                throw new RuntimeException(e);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected URL getHttpsRouteUrl(String serviceName) {
        try {
            return getRouteUri(Protocol.https, serviceName).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected URI getWebSocketRouteUri(String serviceName) {
        return getRouteUri(Protocol.ws, serviceName);
    }

    private URI getRouteUri(Protocol protocol, String serviceName) {
        URI uri;
        Service service = util.getService(serviceName);
        Predicate<Route> httpsPredicate = n -> n.getSpec().getTls() != null;
        Predicate<Route> httpPredicate = n -> n.getSpec().getTls() == null;

        String routeHost = null;
        if(service == null) {
            // Service doesn't exist, create URL using default subdomain
            String defaultRoutingSubdomain = DeploymentConstants.getDefaultDomainSuffix();
            routeHost = getServiceName() + "-" + getNamespace() + defaultRoutingSubdomain;
        } else {
            List<Route> routes = util.getRoutes();
            Optional<Route> route = routes.stream()
                                          .filter(protocol == Protocol.https ? httpsPredicate : httpPredicate)
                                          .filter(n -> n.getSpec().getTo().getName().equals(serviceName))
                                          .findAny();
            if (route.isPresent()) {
                routeHost = route.get().getSpec().getHost();
            } else {
                String routeNames = routes.stream()
                                          .map(n -> n.getMetadata().getName())
                                          .collect(Collectors.joining(", "));
                throw new RuntimeException(protocol + " route leading to service " + serviceName + " not found. Available routes " + routeNames);
            }
        }
        String uriValue = protocol.name() + "://" + routeHost + ":" + retrievePort(protocol);

        try {
            uri = new URI(uriValue.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return uri;
    }

    private String retrievePort(Protocol protocol) {
        switch (protocol) {
            case http:
            case ws:
                return "80";
            case https:
                return "443";
            default:
                throw new IllegalArgumentException("Unrecognized protocol '" + protocol + "'");
        }
    }
}
