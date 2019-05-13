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

package org.kie.cloud.openshift.deployment;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cz.xtf.core.openshift.OpenShift;
import io.fabric8.kubernetes.api.model.Service;

public class ServiceUtil {
    //TODO: regex patterns should be rather moved into the Deployment? classes responsible for individual applications
    private static final Pattern CONTROLLER_REGEXP = Pattern.compile("(?!secure-).*-controller");
    private static final Pattern WORKBENCH_REGEXP = Pattern.compile("(?!secure-).*(-rhpamcentr|-rhdmcentr)");
    private static final Pattern WORKBENCH_MONITORING_REGEXP = Pattern.compile("(?!secure-).*-rhpamcentrmon");
    private static final Pattern KIE_SERVER_REGEXP = Pattern.compile("(?!secure-).*(-execserv|-kieserver)");
    private static final Pattern SMART_ROUTER_REGEXP = Pattern.compile("(?!secure-).*-smartrouter");
    private static final Pattern DATABASE_REGEXP = Pattern.compile("(.*-mysql|.*-postgresql)");
    private static final Pattern SSO_REGEXP = Pattern.compile("(?!secure-).*sso");
    private static final Pattern AMQ_JOLOKIA_REGEXP = Pattern.compile("(?!secure-).*amq-jolokia");
    private static final Pattern AMQ_TCP_REGEXP = Pattern.compile("(?!secure-).*amq-tcp");
    private static final Pattern DOCKER_REGEXP = Pattern.compile("registry");
    private static final Pattern PROMETHEUS_REGEXP = Pattern.compile("prometheus-operated");

    public static String getControllerServiceName(OpenShift openShift) {
        return getServiceName(openShift, CONTROLLER_REGEXP);
    }

    public static String getSsoServiceName(OpenShift openShift) {
        return getServiceName(openShift, SSO_REGEXP);
    }

    public static String getAmqJolokiaServiceName(OpenShift openShift) {
        return getServiceName(openShift, AMQ_JOLOKIA_REGEXP);
    }

    public static String getAmqTcpServiceName(OpenShift openShift) {
        return getServiceName(openShift, AMQ_TCP_REGEXP);
    }

    public static String getWorkbenchServiceName(OpenShift openShift) {
        return getServiceName(openShift, WORKBENCH_REGEXP);
    }

    public static String getWorkbenchMonitoringServiceName(OpenShift openShift) {
        return getServiceName(openShift, WORKBENCH_MONITORING_REGEXP);
    }

    public static String getKieServerServiceName(OpenShift openShift, String suffix) {
        return getServiceName(openShift, Pattern.compile(KIE_SERVER_REGEXP.pattern() + suffix));
    }

    public static String getSmartRouterServiceName(OpenShift openShift) {
        return getServiceName(openShift, SMART_ROUTER_REGEXP);
    }

    public static String getDatabaseServiceName(OpenShift openShift, String suffix) {
        return getServiceName(openShift, Pattern.compile(DATABASE_REGEXP.pattern() + suffix));
    }

    public static String getDockerServiceName(OpenShift openShift) {
        return getServiceName(openShift, DOCKER_REGEXP);
    }

    public static String getPrometheusServiceName(OpenShift openShift) {
        return getServiceName(openShift, PROMETHEUS_REGEXP);
    }

    public static String getServiceName(OpenShift openShift, Pattern regexp) {
        // Try to find service name from all available services
        List<Service> services = openShift.getServices();
        for (Service service : services) {
            if (regexp.matcher(service.getMetadata().getName()).matches()) {
                return service.getMetadata().getName();
            }
        }
        String serviceNames = services.stream().map(s -> s.getMetadata().getName()).collect(Collectors.joining(", "));
        throw new RuntimeException("Service defined by regexp " + regexp.toString() + " not found. Available services: " + serviceNames);
    }
}
