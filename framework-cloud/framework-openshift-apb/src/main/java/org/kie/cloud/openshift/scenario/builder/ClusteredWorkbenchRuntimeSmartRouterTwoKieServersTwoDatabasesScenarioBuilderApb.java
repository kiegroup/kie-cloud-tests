/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.scenario.builder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.ApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioApb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderApb implements ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderApb.class);
    private final Map<String, String> extraVars = new HashMap<>();
    
    private boolean deploySSO = false;

    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderApb() {
        // Required values to create persitence values.
        extraVars.put(OpenShiftApbConstants.APB_PLAN_ID, ApbConstants.Plans.MANAGED);
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_DB_TYPE, ApbConstants.DbType.POSTGRE);
        extraVars.put(OpenShiftApbConstants.APB_IMAGE_STREAM_TAG, "1.0");
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_SETS, "2");
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_REPLICAS, "2");
        extraVars.put(OpenShiftApbConstants.APB_BUSINESSCENTRAL_REPLICAS, "1"); //RHPAM-1662
        extraVars.put(OpenShiftApbConstants.SMARTROUTER_VOLUME_SIZE, "64Mi");
        extraVars.put(OpenShiftApbConstants.BUSINESSCENTRAL_VOLUME_SIZE, "64Mi");
        
        // Users
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_USER, DeploymentConstants.getControllerUser());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_PWD, DeploymentConstants.getControllerPassword());
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario build() {
        return new ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioApb(extraVars,deploySSO);
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_URL, repoUrl);
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_USER, repoUserName);
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_PWD, repoPassword);
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withSmartRouterId(String smartRouterId) {
        throw new UnsupportedOperationException("Not supported for APB.");
        // default not configureable value kie-server-router
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withTimerServiceDataStoreRefreshInterval(Duration timerServiceDataStoreRefreshInterval) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder deploySso() {
        deploySSO = true;
        extraVars.put(OpenShiftApbConstants.SSO_USER, DeploymentConstants.getSsoServiceUser());
        extraVars.put(OpenShiftApbConstants.SSO_PWD, DeploymentConstants.getSsoServicePassword());
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        extraVars.putAll(ldapSettings.getEnvVariables());
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withBusinessCentralMavenUser(String user, String password) {
        extraVars.put(OpenShiftApbConstants.BUSINESS_CENTRAL_MAVEN_USERNAME, user);
        extraVars.put(OpenShiftApbConstants.BUSINESS_CENTRAL_MAVEN_PASSWORD, password);
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        logger.warn("Http route " + hostname + " can't be set to APB scenario. Please configure HTTPS route instead.");
        logger.info("Configuration skipped.");
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        extraVars.put(OpenShiftApbConstants.BUSINESS_CENTRAL_HOSTNAME_HTTPS, hostname);
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer1Hostname(String hostname) {
        logger.warn("Http route " + hostname + " can't be set to APB scenario. Please configure HTTPS route instead.");
        logger.info("Configuration skipped.");
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer1Hostname(String hostname) {
        logger.warn("Hostname can be set only with 1 set of Kie Server deployment.");
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_HOSTNAME_HTTPS, hostname);
        logger.info(hostname + " configured.");
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer2Hostname(String hostname) {
        logger.warn("Http route " + hostname + " can't be set to APB scenario. Please configure HTTPS route instead.");
        logger.info("Configuration skipped.");
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer2Hostname(String hostname) {
        logger.warn("Hostname can be set only with 1 set of Kie Server deployment.");
        logger.info("Configuration skipped.");
        return this;
	}
}
