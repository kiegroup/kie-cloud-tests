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
package org.kie.cloud.openshift.scenario.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.ApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.ProjectApbSpecificPropertyNames;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.WorkbenchKieServerPersistentScenarioApb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchKieServerPersistentScenarioBuilderApb extends AbstractOpenshiftScenarioBuilderApb<WorkbenchKieServerPersistentScenario> implements WorkbenchKieServerPersistentScenarioBuilder {

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerPersistentScenarioBuilderApb.class);
    private final Map<String, String> extraVars = new HashMap<>();
    private boolean deploySSO = false;
    private final ProjectApbSpecificPropertyNames propertyNames = ProjectApbSpecificPropertyNames.create();

    public WorkbenchKieServerPersistentScenarioBuilderApb() {
        extraVars.put(OpenShiftApbConstants.APB_PLAN_ID, ApbConstants.Plans.AUTHORING);
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_DB_TYPE, ApbConstants.DbType.MYSQL);
        extraVars.put(OpenShiftApbConstants.APB_IMAGE_STREAM_TAG, OpenShiftConstants.getApbKieImageStreamTag());
        extraVars.put(OpenShiftApbConstants.BUSINESSCENTRAL_VOLUME_SIZE, "1Gi");

        extraVars.put(OpenShiftApbConstants.KIE_SERVER_USER, DeploymentConstants.getAppUser());
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_PWD, DeploymentConstants.getAppPassword());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_USER, DeploymentConstants.getAppUser());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_PWD, DeploymentConstants.getAppPassword());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_USER, DeploymentConstants.getAppUser());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_PWD, DeploymentConstants.getAppPassword());
    }

    @Override
    public WorkbenchKieServerPersistentScenario getDeploymentScenarioInstance() {
        return new WorkbenchKieServerPersistentScenarioApb(extraVars, deploySSO);
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder deploySso() {
        deploySSO = true;
        extraVars.put(OpenShiftApbConstants.SSO_USER, DeploymentConstants.getSsoServiceUser());
        extraVars.put(OpenShiftApbConstants.SSO_PWD, DeploymentConstants.getSsoServicePassword());
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withKieServerId(String kieServerId) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        logger.warn("Http route " + hostname + " can't be set to APB scenario. Please configure HTTPS route instead.");
        logger.info("Configuration skipped.");
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        extraVars.put(propertyNames.workbenchHostnameHttps(), hostname);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpKieServerHostname(String hostname) {
        logger.warn("Http route " + hostname + " can't be set to APB scenario. Please configure HTTPS route instead.");
        logger.info("Configuration skipped.");
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpsKieServerHostname(String hostname) {
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_HOSTNAME_HTTPS, hostname);
        //need to set protocol and port
        //extraVars.put(OpenShiftApbConstants, hostname)
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withExternalLdap(LdapSettings ldapSettings) {
        extraVars.putAll(ldapSettings.getEnvVariables());
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withInternalLdap(LdapSettings ldapSettings) {
        setAsyncExternalDeployment(ExternalDeploymentID.LDAP);
        extraVars.putAll(ldapSettings.getEnvVariables());
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withGitHooksDir(String dir) {
        extraVars.put(OpenShiftApbConstants.GIT_HOOKS_DIR, dir);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder usePublicIpAddress() {
        logger.warn("Use public IP cannot configure in this scenario. Configuration skipped.");
        return this;
    }

}
