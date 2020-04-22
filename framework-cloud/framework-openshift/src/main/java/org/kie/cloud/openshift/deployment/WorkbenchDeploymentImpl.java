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

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import cz.xtf.core.waiting.SimpleWaiter;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchDeploymentImpl extends OpenShiftDeployment implements WorkbenchDeployment {

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchDeploymentImpl.class);

    private Optional<URL> insecureUrl;
    private Optional<URL> secureUrl;
    private Optional<URI> webSocketUri;
    private String username;
    private String password;

    private String serviceName;

    public WorkbenchDeploymentImpl(Project project) {
        super(project);
    }

    @Override
    public URL getUrl() {
        return getInsecureUrl().orElseGet(() -> getSecureUrl().orElseThrow(() -> new RuntimeException("No Workbench URL is available.")));
    }

    @Override
    public Optional<URL> getInsecureUrl() {
        if (insecureUrl == null) {
            insecureUrl = getHttpRouteUrl(getServiceName());
        }
        return insecureUrl;
    }

    @Override
    public Optional<URL> getSecureUrl() {
        if (secureUrl == null) {
            secureUrl = getHttpsRouteUrl(getServiceName());
        }
        return secureUrl;
    }

    @Override public Optional<URI> getWebSocketUri() {
        if (webSocketUri == null) {
            webSocketUri = getWebSocketRouteUri(getServiceName());
        }
        return webSocketUri;
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

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getWorkbenchServiceName(getOpenShift());
        }
        return serviceName;
    }

    @Override public void waitForScale() {
        super.waitForScale();
        if (!getInstances().isEmpty()) {
            getInsecureUrl().ifPresent(RouterUtil::waitForRouter);
            getSecureUrl().ifPresent(RouterUtil::waitForRouter);
        }
    }

    @Override
    public void changePassword(String newPassword) {
        deploySecretAppUser(DeploymentConstants.getAppUser(), newPassword);
    }

    @Override
    public void changeUsernameAndPassword(String newUsername, String newPassword) {
        deploySecretAppUser(newUsername, newPassword);
    }

    private void deploySecretAppUser(String user, String password) {
        logger.info("Delete old secret '{}'", DeploymentConstants.getAppCredentialsSecretName());
        getOpenShift().secrets().withName(DeploymentConstants.getAppCredentialsSecretName()).delete();
        new SimpleWaiter(() -> getOpenShift().getSecret(DeploymentConstants.getAppCredentialsSecretName()) == null).timeout(TimeUnit.MINUTES, 2)
                                                                                                                   .reason("Waiting for old secret to be deleted.")
                                                                                                                   .waitFor();

        logger.info("Creating user secret '{}'", DeploymentConstants.getAppCredentialsSecretName());
        Map<String, String> data = new HashMap<>();
        data.put(OpenShiftConstants.KIE_ADMIN_USER, user);
        data.put(OpenShiftConstants.KIE_ADMIN_PWD, password);
        
        getProject().createSecret(DeploymentConstants.getAppCredentialsSecretName(), data);
        new SimpleWaiter(() -> getOpenShift().getSecret(DeploymentConstants.getAppCredentialsSecretName()) != null).timeout(TimeUnit.MINUTES, 2)
                                                                                                                   .reason("Waiting for new secret to be created.")
                                                                                                                   .waitFor();
        logger.info("Restart the environment to update Workbench deployment.");
        scaleToZeroAndBackToReplicas();
    }

    private void scaleToZeroAndBackToReplicas() {
        int replicas = getInstances().size();
        scale(0);
        waitForScale();
        scale(replicas);
        waitForScale();
    }
}
