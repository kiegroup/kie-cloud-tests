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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.api.model.Pod;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.WorkbenchInstance;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;

public class WorkbenchDeploymentImpl implements WorkbenchDeployment {

    private OpenShiftController openShiftController;
    private String namespace;
    private URL url;
    private String username;
    private String password;

    private String serviceName;

    public OpenShiftController getOpenShiftController() {
        return openShiftController;
    }

    public void setOpenShiftController(OpenShiftController openShiftController) {
        this.openShiftController = openShiftController;
    }

    @Override public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @Override public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override public String getPassword() {
        return password;
    }

    @Override public List<Instance> getInstances() {
        String deploymentConfigName = openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().getName();
        List<Pod> pods =
                openShiftController.getClient().pods().inNamespace(namespace).withLabel(OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL, deploymentConfigName).list().getItems();

        List<Instance> workbenchInstances = new ArrayList<>();
        for (Pod pod : pods) {
            WorkbenchInstanceImpl workbenchInstance = new WorkbenchInstanceImpl();
            workbenchInstance.setOpenShiftController(openShiftController);
            workbenchInstance.setPodName(pod.getMetadata().getName());
            workbenchInstance.setNamespace(namespace);

            workbenchInstances.add(workbenchInstance);
        }

        return workbenchInstances;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String applicationName) {
        this.serviceName = applicationName + "-buscentr";
    }

    @Override public void scale(int instances) {
        openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().scalePods(instances);
    }

    @Override public void waitForScale() {
        openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().waitUntilAllPodsAreReady();
        if (openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().podsNumber() > 0) {
            RouterUtil.waitForRouter(getUrl());
        }
    }

    @Override public boolean ready() {
        throw new RuntimeException("Not implemented");
    }
}
