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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentTriggerPolicyBuilder;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;
import org.kie.cloud.openshift.resource.Service;

public class ServiceImpl implements Service {

    private OpenShiftClient client;
    private String projectName;
    private String serviceName;

    public ServiceImpl(OpenShiftClient client, String projectName, String serviceName) {
        this.client = client;
        this.projectName = projectName;
        this.serviceName = serviceName;
    }

    @Override
    public String getName() {
        return serviceName;
    }

    @Override
    public void delete() {
        client.services().inNamespace(projectName).withName(serviceName).delete();
    }

    @Override
    public DeploymentConfigImpl createDeploymentConfig(String image, Map<String, String> envVariables) {
        return createDeploymentConfig(image, envVariables, 1);
    }

    @Override
    public DeploymentConfigImpl createDeploymentConfig(String image, Map<String, String> envVariables, int pods) {
        Map<String, String> selector = new HashMap<String, String>();
        // To be paired with service
        selector.put("deploymentconfig", serviceName);

        List<EnvVar> envVar = convertEnvVariables(envVariables);

        client.deploymentConfigs().inNamespace(projectName).create(
                new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withName(serviceName)
                    .withNamespace(projectName)
                .endMetadata()
                .withNewSpec()
                    // Start with one pod
                    .withReplicas(pods)
                    .withNewTemplate()
                        .withNewMetadata()
                            .withLabels(selector)
                        .endMetadata()
                        .withNewSpec()
                            .withContainers(
                                    new ContainerBuilder()
                                    .withName(serviceName)
                                    .withImage(image)
                                    .withEnv(envVar)
                                    .build())
                        .endSpec()
                    .endTemplate()
                    .withTriggers(
                            new DeploymentTriggerPolicyBuilder()
                            .withType(OpenShiftResourceConstants.DEPLOYMENT_TRIGGER_CONFIG_CHANGE)
                            .build())
                    .withSelector(selector)
                .endSpec()
                .build());

        try {
            client.deploymentConfigs().inNamespace(projectName).withName(serviceName).waitUntilReady(OpenShiftResourceConstants.DEPLOYMENT_CONFIG_CREATION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for deployment to become ready.", e);
        }

        return new DeploymentConfigImpl(client, projectName, serviceName);
    }

    private List<EnvVar> convertEnvVariables(Map<String, String> envVariables) {
        List<EnvVar> envVar = new ArrayList<>();

        for (Entry<String, String> env : envVariables.entrySet()) {
            envVar.add(new EnvVar(env.getKey(), env.getValue(), null));
        }
        return envVar;
    }

    @Override
    public DeploymentConfigImpl getDeploymentConfig() {
        io.fabric8.openshift.api.model.DeploymentConfig deploymentConfig = client.deploymentConfigs().inNamespace(projectName).withName(serviceName).get();
        if(deploymentConfig != null) {
            return new DeploymentConfigImpl(client, projectName, serviceName);
        }
        return null;
    }

    @Override
    public RouteImpl createRoute() {
        String route = serviceName + OpenShiftResourceConstants.CENTRAL_CI_ROUTE_SUFFIX;
        return createRoute(route);
    }

    @Override
    public RouteImpl createRoute(String route) {
        client.routes().inNamespace(projectName).create(
                new RouteBuilder()
                .withNewMetadata()
                    .withName(serviceName)
                    .withNamespace(projectName)
                .endMetadata()
                .withNewSpec()
                    .withNewTo()
                        .withKind(OpenShiftResourceConstants.ROUTE_REDIRECT_COMPONENT_TYPE)
                        .withName(serviceName)
                        .withWeight(OpenShiftResourceConstants.ROUTE_REDIRECT_DEFAULT_WEIGHT)
                    .endTo()
                    .withHost(route)
                .endSpec()
                .build());

        return new RouteImpl(client, projectName, serviceName);
    }

    @Override
    public RouteImpl getRoute() {
        io.fabric8.openshift.api.model.Route route = client.routes().inNamespace(projectName).withName(serviceName).get();
        if(route != null) {
            return new RouteImpl(client, projectName, serviceName);
        }
        return null;
    }
}
