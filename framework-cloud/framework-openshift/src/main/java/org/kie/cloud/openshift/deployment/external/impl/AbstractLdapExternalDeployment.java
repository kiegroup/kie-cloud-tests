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
package org.kie.cloud.openshift.deployment.external.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.LdapDeployment;
import org.kie.cloud.openshift.deployment.LdapDeploymentImpl;
import org.kie.cloud.openshift.deployment.external.AbstractExternalDeployment;
import org.kie.cloud.openshift.resource.CloudProperties;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLdapExternalDeployment<U> extends AbstractExternalDeployment<LdapDeployment, U> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLdapExternalDeployment.class);
    private static final String LDAP_TEMPLATE = "/deployments/ldap.yaml";

    public AbstractLdapExternalDeployment(Map<String, String> deploymentConfig) {
        super(deploymentConfig);
    }

    @Override
    public ExternalDeploymentID getKey() {
        return ExternalDeploymentID.LDAP;
    }

    @Override
    protected LdapDeployment deployToProject(Project project) {
        logger.info("Creating internal LDAP instance.");
        project.processTemplateAndCreateResources(getLdapTemplate(), getLdapProperties(project));

        logger.info("Waiting for LDAP deployment to become ready.");
        return new LdapDeploymentImpl(project);
    }

    private URL getLdapTemplate() {
        try {
            return new URL("file://" + getClass().getResource(LDAP_TEMPLATE).getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Wrong LDAP template location", e);
        }
    }

    private Map<String, String> getLdapProperties(Project project) {
        Map<String, String> properties = new HashMap<>();
        properties.put("APPLICATION_NAME", project.getName());
        properties.put("LDAP_DOCKER_IMAGE", CloudProperties.getInstance().getLdapDockerImage());
        return properties;
    }

    @Override
    public void removeConfiguration(U object) {
        // Nothing done
    }

}
