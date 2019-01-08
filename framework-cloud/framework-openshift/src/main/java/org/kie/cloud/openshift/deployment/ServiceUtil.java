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

import cz.xtf.openshift.OpenShiftUtil;
import io.fabric8.kubernetes.api.model.Service;

public class ServiceUtil {

    private static final Pattern CONTROLLER_REGEXP = Pattern.compile("(?!secure-).*-controller");
    private static final Pattern WORKBENCH_REGEXP = Pattern.compile("(?!secure-).*(-rhpamcentr|-rhdmcentr)");
    private static final Pattern WORKBENCH_MONITORING_REGEXP = Pattern.compile("(?!secure-).*-rhpamcentrmon");
    private static final Pattern KIE_SERVER_REGEXP = Pattern.compile("(?!secure-).*(-execserv|-kieserver)");
    private static final Pattern SMART_ROUTER_REGEXP = Pattern.compile("(?!secure-).*-smartrouter");
    private static final Pattern DATABASE_REGEXP = Pattern.compile("(.*-mysql|.*-postgresql)");
    private static final Pattern SSO_REGEXP = Pattern.compile("(?!secure-).*sso");
    private static final Pattern DOCKER_REGEXP = Pattern.compile("registry");

    public static String getControllerServiceName(OpenShiftUtil util) {
        return getServiceName(util, CONTROLLER_REGEXP);
    }

    public static String getSsoServiceName(OpenShiftUtil util) {
        return getServiceName(util, SSO_REGEXP);
    }

    public static String getWorkbenchServiceName(OpenShiftUtil util) {
        return getServiceName(util, WORKBENCH_REGEXP);
    }

    public static String getWorkbenchMonitoringServiceName(OpenShiftUtil util) {
        return getServiceName(util, WORKBENCH_MONITORING_REGEXP);
    }

    public static String getKieServerServiceName(OpenShiftUtil util, String suffix) {
        return getServiceName(util, Pattern.compile(KIE_SERVER_REGEXP.pattern() + suffix));
    }

    public static String getSmartRouterServiceName(OpenShiftUtil util) {
        return getServiceName(util, SMART_ROUTER_REGEXP);
    }

    public static String getDatabaseServiceName(OpenShiftUtil util, String suffix) {
        return getServiceName(util, Pattern.compile(DATABASE_REGEXP.pattern() + suffix));
    }

    public static String getDockerServiceName(OpenShiftUtil util) {
        return getServiceName(util, DOCKER_REGEXP);
    }

    private static String getServiceName(OpenShiftUtil util, Pattern regexp) {
        // Try to find service name from all available services
        List<Service> services = util.getServices();
        for (Service service : services) {
            if (regexp.matcher(service.getMetadata().getName()).matches()) {
                return service.getMetadata().getName();
            }
        }
        String serviceNames = services.stream().map(s -> s.getMetadata().getName()).collect(Collectors.joining(", "));
        throw new RuntimeException("Service defined by regexp " + regexp.toString() + " not found. Available services: " + serviceNames);
    }
}
