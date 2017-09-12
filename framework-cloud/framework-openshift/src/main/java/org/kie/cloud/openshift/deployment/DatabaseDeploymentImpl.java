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
import java.util.regex.Pattern;

import io.fabric8.kubernetes.api.model.Pod;
import static java.util.stream.Collectors.toList;
import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.DatabaseInstance;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;
import org.kie.cloud.openshift.resource.Service;

public class DatabaseDeploymentImpl extends OpenShiftDeployment implements DatabaseDeployment {

    private static final Pattern DATABASE_REGEXP = Pattern.compile("(.*-mysql|.*-postgresql)");

    private String username;
    private String password;
    private String databaseName;
    private URL url;
    private String applicationName;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabaseName(String database) {
        this.databaseName = database;
    }

    public void setUrl(URL url) {
        this.url = url;
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
    public String getServiceName() {
        if (databaseName == null) {
            // Database name not set, try to guess it from all available services
            List<Service> services = openShiftController.getProject(namespace).getServices();
            for (Service service : services) {
                if (DATABASE_REGEXP.matcher(service.getName()).matches()) {
                    return service.getName();
                }
            }
            throw new RuntimeException("No available database found among services.");
        }
        return applicationName + "-" + databaseName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
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
}
