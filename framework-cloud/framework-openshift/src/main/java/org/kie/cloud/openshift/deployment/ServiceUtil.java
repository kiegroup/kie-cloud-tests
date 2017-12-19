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

import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.resource.Service;

public class ServiceUtil {

    private static final Pattern WORKBENCH_REGEXP = Pattern.compile("(?!secure-).*(-buscentr|-rhdmcentr)");
    private static final Pattern SECURE_WORKBENCH_REGEXP = Pattern.compile("secure-.*(-buscentr|-rhdmcentr)");
    private static final Pattern KIE_SERVER_REGEXP = Pattern.compile("(?!secure-).*(-execserv|-kieserver)");
    private static final Pattern SECURE_KIE_SERVER_REGEXP = Pattern.compile("secure-.*(-execserv|-kieserver)");
    private static final Pattern DATABASE_REGEXP = Pattern.compile("(.*-mysql|.*-postgresql)");

    public static String getWorkbenchServiceName(OpenShiftController openShiftController, String namespace) {
        return getServiceName(openShiftController, namespace, WORKBENCH_REGEXP);
    }

    public static String getWorkbenchSecureServiceName(OpenShiftController openShiftController, String namespace) {
        return getServiceName(openShiftController, namespace, SECURE_WORKBENCH_REGEXP);
    }

    public static String getKieServerServiceName(OpenShiftController openShiftController, String namespace) {
        return getServiceName(openShiftController, namespace, KIE_SERVER_REGEXP);
    }

    public static String getKieServerSecureServiceName(OpenShiftController openShiftController, String namespace) {
        return getServiceName(openShiftController, namespace, SECURE_KIE_SERVER_REGEXP);
    }

    public static String getDatabaseServiceName(OpenShiftController openShiftController, String namespace) {
        return getServiceName(openShiftController, namespace, DATABASE_REGEXP);
    }

    private static String getServiceName(OpenShiftController openShiftController, String namespace, Pattern regexp) {
        // Try to find service name from all available services
        List<Service> services = openShiftController.getProject(namespace).getServices();
        for (Service service : services) {
            if (regexp.matcher(service.getName()).matches()) {
                return service.getName();
            }
        }
        String serviceNames = services.stream().map(Service::getName).collect(Collectors.joining(", "));
        throw new RuntimeException("Service defined by regexp " + regexp.toString() + " not found. Available services: " + serviceNames);
    }
}
