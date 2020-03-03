/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import io.fabric8.kubernetes.api.model.Service;
import org.apache.commons.lang3.StringUtils;
import org.kie.cloud.api.deployment.LdapDeployment;
import org.kie.cloud.openshift.resource.Project;

public class LdapDeploymentImpl extends OpenShiftDeployment implements LdapDeployment {

    private static final String LDAP = "ldap";

    private final String serviceName;
    private final String host;

    public LdapDeploymentImpl(Project project) {
        super(project);
        Service ldapService = ServiceUtil.getLdapService(getOpenShift());
        this.serviceName = ldapService.getMetadata().getName();
        this.host = getHostByService(ldapService);
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getDeploymentConfigName() {
        return LDAP;
    }

    @Override
    public String getHost() {
        return host;
    }

    private static final String getHostByService(Service ldapService) {
        return ldapService.getSpec().getPorts().stream()
                          .filter(route -> StringUtils.equals(LDAP, route.getName()))
                          .map(route -> String.format("%s://%s:%s", LDAP, ldapService.getMetadata().getName(), route.getPort()))
                          .findFirst()
                          .orElseThrow(() -> new RuntimeException("Host LDAP not found"));
    }

}
