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

import java.net.URL;
import java.util.List;

import io.fabric8.kubernetes.api.model.Pod;
import static java.util.stream.Collectors.toList;
import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.DatabaseInstance;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;

public class DatabaseDeploymentImpl implements DatabaseDeployment {

    private OpenShiftController openShiftController;
    private String username;
    private String password;
    private String namespace;
    private String databaseName;
    private URL url;
    private String applicationName;

    public void setOpenShiftController(OpenShiftController openShiftController) {
        this.openShiftController = openShiftController;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setDatabaseName(String database) {
        this.databaseName = database;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public OpenShiftController getOpenShiftController() {
        return openShiftController;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public String getServiceName() {
        return applicationName + "-" + databaseName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public void scale(int instances) {
        openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().scalePods(instances);
    }

    @Override
    public void waitForScale() {
        openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().waitUntilAllPodsAreReady();
    }

    @Override
    public List<Instance> getInstances() {
        String deploymentConfigName = openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().getName();
        List<Pod> pods = openShiftController.getClient().pods().inNamespace(namespace).withLabel(OpenShiftResourceConstants.DEPLOYMENT_CONFIG_LABEL, deploymentConfigName).list().getItems();

        List<Instance> databaseInstances = pods.stream().map((pod) -> {
            return createDatabaseInstance(pod);
        }).collect(toList());

        return databaseInstances;
    }

    private DatabaseInstance createDatabaseInstance(Pod pod) {
        DatabaseInstanceImpl databaseInstance = new DatabaseInstanceImpl();
        databaseInstance.setOpenShiftController(openShiftController);
        databaseInstance.setNamespace(namespace);
        databaseInstance.setName(pod.getMetadata().getName());
        return databaseInstance;
    }

    @Override
    public boolean ready() {
        return getInstances().size() > 0;
    }

}
