/*
 * Copyright 2018 JBoss by Red Hat.
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
package org.kie.cloud.openshift.util;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.builder.PodBuilder;
import cz.xtf.openshift.builder.RouteBuilder;
import cz.xtf.openshift.builder.ServiceBuilder;
import org.kie.cloud.api.deployment.LdapDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.LdapDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapDeployer {

    private static final Logger logger = LoggerFactory.getLogger(LdapDeployer.class);
    private static final String LDAP_LABEL = "test-ldap";
    private static final String LDAP_IMAGE = "rhba-qe-openldap";
    private static final String LDAP_SERVICE = "service-test-ldap";
    private static final String LDAP_ROUTE = "route-test-ldap";

    public static LdapDeployment deploy(Project project) {
        logger.info("Creating LDAP image streams from " + OpenShiftConstants.getLdapImageStreams());
        project.createResources(OpenShiftConstants.getLdapImageStreams());
        OpenShiftUtil util = project.getOpenShiftUtil();

        util.createPod(
                new PodBuilder(LDAP_IMAGE)
                        .addLabel("name", LDAP_LABEL)
                        .container()
                        .fromImage(util.getImageStream(LDAP_IMAGE).getSpec().getTags().get(0).getFrom().getName())
                        //                        .fromImage("bxms-binaries.usersys.redhat.com:5000/openldap:v3")
                        .port(389)
                        .port(636)
                        .pod()
                        .build());
        util.createService(
                new ServiceBuilder(LDAP_SERVICE)
                        .addLabel("name", LDAP_LABEL)
                        .addContainerSelector("name", LDAP_LABEL)
                        .setContainerPort(389)
                        .setPort(389)
                        .build());
        final String hostname = LDAP_LABEL + "-" + project.getName() + DeploymentConstants.getDefaultDomainSuffix();
        util.createRoute(
                new RouteBuilder(LDAP_ROUTE)
                        .addLabel("name", LDAP_LABEL)
                        .exposedAsHost(hostname)
                        .forService(LDAP_SERVICE)
                        .build());

        LdapDeployment ldapDeployment = createLdapDeployment(project);

//        logger.info("Waiting for LDAP deployment to become ready.");
//        ldapDeployment.waitForScale();
        return ldapDeployment;
    }

    private static LdapDeployment createLdapDeployment(Project project) {
        LdapDeploymentImpl ldapDeploymnet = new LdapDeploymentImpl(project);

        return ldapDeploymnet;
    }

}
