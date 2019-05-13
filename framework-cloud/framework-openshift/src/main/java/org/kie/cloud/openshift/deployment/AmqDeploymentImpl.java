/*
 * Copyright 2019 JBoss by Red Hat.
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
import java.util.Optional;

import org.kie.cloud.api.deployment.AmqDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.resource.Project;

import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;

public class AmqDeploymentImpl extends OpenShiftDeployment implements AmqDeployment {

    private String serviceName, tcpServiceName;
    private Optional<URL> insecureUrl;
    private Optional<URL> secureUrl;
    private Optional<URL> tcpUrl;
    private String username;
    private String password;

    public AmqDeploymentImpl(Project project) {
        super(project);
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getAmqJolokiaServiceName(getOpenShift());
        }
        return serviceName;
    }

    private String getAmqTcpServiceName() {
        if (tcpServiceName == null) {
            tcpServiceName = ServiceUtil.getAmqTcpServiceName(getOpenShift());
        }
        return tcpServiceName;
    }

    private Optional<URL> createSecureTcpUrl() {
        String amqTcpHost = getAmqTcpServiceName() + "-" + getNamespace()
                + DeploymentConstants.getDefaultDomainSuffix();

        Route tcpRoute = new RouteBuilder()
                .withNewMetadata()
                    .withName(getAmqTcpServiceName())
                .endMetadata()
                .withNewSpec()
                    .withNewTo()
                        .withKind("Service")
                        .withName(getAmqTcpServiceName()+"-ssl")
                    .endTo()
                    .withNewPort()
                        .withNewTargetPort("amq-tcp-ssl") // TODO get this port from service?
                    .endPort()
                    .withNewTls()
                        .withTermination("passthrough")
                    .endTls()
                    .withHost(amqTcpHost)
                    .withWildcardPolicy("None")
                .endSpec()
                .build();

        getOpenShift().routes().createOrReplace(tcpRoute);

        return getHttpsRouteUrl(getAmqTcpServiceName()+"-ssl");
    }

    @Override
    public URL getTcpUrl() {
        if (tcpUrl == null) {
            tcpUrl = createSecureTcpUrl();
        }
        return tcpUrl.orElseThrow(()->new RuntimeException("No Amq Tcp URL is available."));
    }

    @Override
    public URL getUrl() {
        return getInsecureUrl().orElseGet(() -> getSecureUrl().orElseThrow(() -> new RuntimeException("No Amq URL is available.")));
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

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void waitForScale() {
        super.waitForScale();
        if (getInstances().size() > 0) {
            RouterUtil.waitForRouter(getUrl());
        }
    }

}
