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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteList;
import org.apache.commons.lang3.StringUtils;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.DeploymentTimeoutException;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.protocol.Protocol;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.OpenShiftCaller;
import org.kie.cloud.openshift.util.OpenshiftInstanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

public abstract class OpenShiftDeployment implements Deployment {

    private static final String SHA_TAG = "@sha256:";
    private static final Logger logger = LoggerFactory.getLogger(OpenShiftDeployment.class);

    private OpenShift openShift;
    private Project project;

    public OpenShiftDeployment(Project project) {
        this.project = project;
        this.openShift = project.getOpenShift();
    }

    public OpenShift getOpenShift() {
        return openShift;
    }

    public void setOpenShift(OpenShift openShift) {
        this.openShift = openShift;
    }

    @Override
    public String getNamespace() {
        return project.getName();
    }

    @Override
    public void deleteInstances() {
        getInstances().forEach(this::deleteInstance);
    }

    public abstract String getServiceName();

    public String getDeploymentConfigName() {
        return getServiceName();
    }

    protected String getDeploymentConfigName(OpenShift openShift, Pattern regexp) {
        // Try to find deployment config name from all available deployment configs
        List<DeploymentConfig> foundDeploymentConfigs = openShift.getDeploymentConfigs()
                                                                 .stream()
                                                                 .filter(deploymentConfig -> regexp.matcher(deploymentConfig.getMetadata().getName()).matches())
                                                                 .collect(Collectors.toList());
        if (foundDeploymentConfigs.isEmpty()) {
            String deploymentConfigNames = openShift.getDeploymentConfigs().stream().map(s -> s.getMetadata().getName()).collect(Collectors.joining(", "));
            throw new RuntimeException("Deployment config defined by regexp " + regexp.toString() + " not found. Available deployment configs: " + deploymentConfigNames);
        } else if (foundDeploymentConfigs.size() > 1) {
            String deploymentConfigNames = foundDeploymentConfigs.stream().map(s -> s.getMetadata().getName()).collect(Collectors.joining(", "));
            throw new RuntimeException("Found multiple deployment configs defined by regexp " + regexp.toString() + " . Found deployment configs are : " + deploymentConfigNames);
        }
        return foundDeploymentConfigs.get(0).getMetadata().getName();
    }

    @Override
    public void scale(int instances) {
        openShift.deploymentConfigs().inNamespace(getNamespace()).withName(getDeploymentConfigName()).scale(instances, true);
    }

    @Override
    public boolean isReady() {
        try {
            Service service = openShift.getService(getServiceName());
            DeploymentConfig deploymentConfig = openShift.getDeploymentConfig(getDeploymentConfigName());
            return service != null && deploymentConfig != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Instance> getInstances() {
        if (isReady() && getReplicas() > 0) {
            String deploymentConfigName = getDeploymentConfigName();

            return OpenShiftCaller.repeatableCall(() -> openShift.getPods()
                                                                 .stream()
                                                                 .filter(pod -> {
                                                                     String podsDeploymentConfigName = pod.getMetadata().getLabels().get(OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL);
                                                                     return deploymentConfigName.equals(podsDeploymentConfigName);
                                                                 })
                                                                 .map(pod -> OpenshiftInstanceUtil.createInstance(openShift, getNamespace(), pod))
                                                                 .collect(toList()));
        }

        return Collections.emptyList();
    }

    @Override
    public void waitForScale() {
        waitUntilAllPodsAreReadyAndRunning(getReplicas());
    }

    @Override
    public void waitForScheduled() {
        waitUntilAllPodsAreReady(getReplicas());
    }

    @Override
    public void waitForVersionTag(String versionTag) {
        try {
            Supplier<Boolean> checkNewVersionTag = () -> deploymentConfig().getSpec().getTemplate().getSpec().getContainers().stream().anyMatch(c -> checkImageVersion(c.getImage(), versionTag));

            new SimpleWaiter(() -> OpenShiftCaller.repeatableCall(checkNewVersionTag)).timeout(OpenShiftResourceConstants.DEPLOYMENT_NEW_VERSION_TIMEOUT)
                                                                                      .reason("The deployment " + getDeploymentConfigName() + " was not restarted using the version tag " + versionTag)
                                                                                      .waitFor();

        } catch (AssertionError e) {
            throw new DeploymentTimeoutException("Timeout while waiting for pods to be ready.");
        }
    }

    @Override
    public int getReplicas() {
        return deploymentConfig().getSpec().getReplicas().intValue();
    }

    protected void waitUntilAllPodsAreReadyAndRunning(int expectedPods) {
        waitUntilAllPodsAreReady(expectedPods);
        waitUntilAllPodsAreRunning(expectedPods);
    }

    protected void waitUntilAllPodsAreReadyAndRunning(int expectedPods, long timeout) {
        waitUntilAllPodsAreReady(expectedPods, timeout);
        waitUntilAllPodsAreRunning(expectedPods, timeout);
    }

    protected void waitUntilAllPodsAreReady(int expectedPods, long timeout) {
        try {
            OpenShiftCaller.repeatableCall(() -> openShift.waiters()
                                                          .areExactlyNPodsReady(expectedPods, OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL, getDeploymentConfigName())
                                                          .timeout(timeout)
                                                          .reason("Waiting for " + expectedPods + " pods of deployment config " + getDeploymentConfigName() + " to become ready.")
                                                          .waitFor());
        } catch (AssertionError e) {
            throw new DeploymentTimeoutException("Timeout while waiting for pods to be ready.");
        }
    }

    protected void waitUntilAllPodsAreRunning(int expectedPods, long timeout) {
        try {
            OpenShiftCaller.repeatableCall(() -> openShift.waiters()
                                                          .areExactlyNPodsRunning(expectedPods, OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL, getDeploymentConfigName())
                                                          .timeout(timeout)
                                                          .reason("Waiting for " + expectedPods + " pods of deployment config " + getDeploymentConfigName() + " to become runnning.")
                                                          .waitFor());
        } catch (AssertionError e) {
            throw new DeploymentTimeoutException("Timeout while waiting for pods to start.");
        }
    }

    protected void waitUntilAllPodsAreReady(int expectedPods) {
        try {
            OpenShiftCaller.repeatableCall(() -> openShift.waiters()
                                                          .areExactlyNPodsReady(expectedPods, OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL, getDeploymentConfigName())
                                                          .timeout(OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT)
                                                          .reason("Waiting for " + expectedPods + " pods of deployment config " + getDeploymentConfigName() + " to become ready.")
                                                          .waitFor());
        } catch (AssertionError e) {
            throw new DeploymentTimeoutException("Timeout while waiting for pods to be ready.");
        }
    }

    protected void waitUntilAllPodsAreRunning(int expectedPods) {
        try {
            OpenShiftCaller.repeatableCall(() -> openShift.waiters()
                                                          .areExactlyNPodsRunning(expectedPods, OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL, getDeploymentConfigName())
                                                          .timeout(OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT)
                                                          .reason("Waiting for " + expectedPods + " pods of deployment config " + getDeploymentConfigName() + " to become runnning.")
                                                          .waitFor());
        } catch (AssertionError e) {
            throw new DeploymentTimeoutException("Timeout while waiting for pods to start.");
        }
    }

    @Override
    public void setRouterTimeout(Duration timeoutValue) {
        RouteList list = getRoutes();
        for (Route r : list.getItems()) {
            openShift
                     .routes()
                     .withName(r.getMetadata().getName())
                     .edit()
                     .editMetadata()
                     .addToAnnotations(OpenShiftConstants.HAPROXY_ROUTER_TIMEOUT, timeoutValue.getSeconds() + "s")
                     .endMetadata()
                     .done();
        }
    }

    @Override
    public void resetRouterTimeout() {
        RouteList list = getRoutes();
        for (Route r : list.getItems()) {
            openShift
                     .routes()
                     .withName(r.getMetadata().getName())
                     .edit()
                     .editMetadata()
                     .removeFromAnnotations(OpenShiftConstants.HAPROXY_ROUTER_TIMEOUT)
                     .endMetadata()
                     .done();
        }
    }

    @Override
    public void setRouterBalance(String balance) {
        RouteList list = getRoutes();
        for (Route r : list.getItems()) {
            openShift
                     .routes()
                     .withName(r.getMetadata().getName())
                     .edit()
                     .editMetadata()
                     .addToAnnotations(OpenShiftConstants.HAPROXY_ROUTER_BALANCE, balance)
                     .endMetadata()
                     .done();
        }
    }

    @Override
    public void setResources(Map<String, String> requests, Map<String, String> limits) {
        openShift
                 .deploymentConfigs()
                 .withName(getDeploymentConfigName())
                 .edit()
                 .editOrNewSpec()
                 .editTemplate()
                 .editOrNewSpec()
                 .editContainer(0)
                 .editResources()
                 .addToRequests(transformMap(requests))
                 .addToLimits(transformMap(limits))
                 .endResources()
                 .endContainer()
                 .endSpec()
                 .endTemplate()
                 .endSpec()
                 .done();
    }

    protected Optional<URL> getHttpRouteUrl(String serviceName) {
        return getRoute(Protocol.http, serviceName).map(toURL());
    }

    protected Optional<URL> getHttpsRouteUrl(String serviceName) {
        return getRoute(Protocol.https, serviceName).map(toURL());
    }

    protected Optional<URI> getWebSocketRouteUri(String serviceName) {
        return getRoute(Protocol.ws, serviceName).map(toURI());
    }

    protected RouteList getRoutes() {
        return openShift
                        .routes()
                        .withLabel("service", getServiceName())
                        .list();
    }

    private DeploymentConfig deploymentConfig() {
        return openShift.getDeploymentConfig(getDeploymentConfigName());
    }

    private Optional<String> getRoute(Protocol protocol, String serviceName) {
        Service service = openShift.getService(serviceName);
        Predicate<Route> httpsPredicate = n -> n.getSpec().getTls() != null;
        Predicate<Route> httpPredicate = n -> n.getSpec().getTls() == null;

        String routeHost = null;
        if (service == null) {
            // Service doesn't exist, create URL using default subdomain
            String defaultRoutingSubdomain = DeploymentConstants.getDefaultDomainSuffix();
            routeHost = getServiceName() + "-" + getNamespace() + defaultRoutingSubdomain;
        } else {
            List<Route> routes = openShift.getRoutes();
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
                logger.warn(protocol + " route leading to service " + serviceName + " not found. Available routes " + routeNames);
                return Optional.empty();
                //throw new RuntimeException(protocol + " route leading to service " + serviceName + " not found. Available routes " + routeNames);
            }
        }

        return Optional.ofNullable(protocol.name() + "://" + routeHost + ":" + retrievePort(protocol));
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

    private boolean checkImageVersion(String image, String versionTag) {
        if (StringUtils.contains(image, SHA_TAG)) {
            String actualReference = StringUtils.substringAfterLast(image, SHA_TAG);

            String imageName = image.substring(image.lastIndexOf("/") + 1, image.lastIndexOf(SHA_TAG));
            ImageStreamTag imageTag = openShift.imageStreamTags().withName(String.format("%s:%s", imageName, versionTag)).get();

            return StringUtils.endsWith(imageTag.getImage().getDockerImageReference(), actualReference);
        }

        return StringUtils.endsWith(image, versionTag);
    }

    private void deleteInstance(Instance instance) {
        Pod pod = openShift.getPod(instance.getName());
        openShift.deletePod(pod);
    }

    private Map<String, Quantity> transformMap(Map<String, String> x) {
        return x.entrySet().stream()
                .collect(Collectors.toMap(
                                          e -> e.getKey(),
                                          e -> new Quantity(e.getValue())));
    }

    private static Function<String, URI> toURI() {
        return route -> {
            try {
                return new URI(route);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static Function<String, URL> toURL() {
        return route -> {
            try {
                return new URL(route);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        };
    }

}
