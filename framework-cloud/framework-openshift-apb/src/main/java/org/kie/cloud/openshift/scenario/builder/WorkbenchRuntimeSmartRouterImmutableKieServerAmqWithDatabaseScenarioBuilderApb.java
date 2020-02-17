/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.DeploymentScenarioListener;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.ApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioApb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilderApb extends AbstractOpenshiftScenarioBuilderApb<WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario> implements
                                                                                            WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder {

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilderApb.class);
    private final Map<String, String> extraVars = new HashMap<>();

    private boolean deploySSO = false;

    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilderApb() {
        // Required values to create persistence values.
        extraVars.put(OpenShiftApbConstants.APB_PLAN_ID, ApbConstants.Plans.IMMUTABLE_KIE);
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_DB_TYPE, ApbConstants.DbType.POSTGRE);
        extraVars.put(OpenShiftApbConstants.APB_IMAGE_STREAM_TAG, OpenShiftConstants.getApbKieImageStreamTag());
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_SETS, "1");
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_REPLICAS, "1");
        extraVars.put(OpenShiftApbConstants.APB_BUSINESSCENTRAL_REPLICAS, "1"); // RHPAM-1662
        extraVars.put(OpenShiftApbConstants.SMARTROUTER_VOLUME_SIZE, "64Mi");
        extraVars.put(OpenShiftApbConstants.BUSINESSCENTRAL_VOLUME_SIZE, "64Mi");

        // Users
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_USER, DeploymentConstants.getAppUser());
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_PWD, DeploymentConstants.getAppPassword());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_USER, DeploymentConstants.getAppUser());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_PWD, DeploymentConstants.getAppPassword());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_USER, DeploymentConstants.getAppUser());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_PWD, DeploymentConstants.getAppPassword());

        // AMQ
        extraVars.put(OpenShiftApbConstants.AMQ_INTEGRATION_ENABLE, "true");
        extraVars.put(OpenShiftApbConstants.AMQ_USERNAME, DeploymentConstants.getAmqUsername());
        extraVars.put(OpenShiftApbConstants.AMQ_PASSWORD, DeploymentConstants.getAmqPassword());
        // These values are defined in pom.xml where keystore and truststore are generated
        extraVars.put(OpenShiftApbConstants.AMQ_SECRET, "amq-app-secret");
        extraVars.put(OpenShiftApbConstants.AMQ_TRUSTSTORE, "broker.ts");
        extraVars.put(OpenShiftApbConstants.AMQ_TRUSTSTORE_PASSWORD, "changeit");
        extraVars.put(OpenShiftApbConstants.AMQ_KEYSTORE, "broker.ks");
        extraVars.put(OpenShiftApbConstants.AMQ_KEYSTORE_PASSWORD, "changeit");
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario getDeploymentScenarioInstance() {
        return new WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioApb(extraVars, deploySSO);
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder deploySso() {
        deploySSO = true;
        extraVars.put(OpenShiftApbConstants.SSO_USER, DeploymentConstants.getSsoServiceUser());
        extraVars.put(OpenShiftApbConstants.SSO_PWD, DeploymentConstants.getSsoServicePassword());
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withKieServerId(String kieServerId) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withContainerDeployment(String kieContainerDeployment) {
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir) {
        extraVars.put(OpenShiftApbConstants.SOURCE_REPOSITORY_URL, gitRepoUrl);
        extraVars.put(OpenShiftApbConstants.SOURCE_REPOSITORY_REF, gitReference);
        extraVars.put(OpenShiftApbConstants.CONTEXT_DIR, gitContextDir);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir, String artifactDirs) {
        withSourceLocation(gitRepoUrl, gitReference, gitContextDir);
        extraVars.put(OpenShiftApbConstants.ARTIFACT_DIR, artifactDirs);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withHttpsKieServerHostname(String hostname) {
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_HOSTNAME_HTTPS, hostname);
        // need to set protocol and port
        // extraVars.put(OpenShiftApbConstants, hostname)
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withDeploymentScenarioListener(DeploymentScenarioListener<WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario> deploymentScenarioListener) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withHttpKieServerHostname(String hostname) {
        logger.warn("Http route " + hostname + " can't be set to APB scenario. Please configure HTTPS route instead.");
        logger.info("Configuration skipped.");
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        extraVars.putAll(ldapSettings.getEnvVariables());
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder enableExternalJmsSignalQueue(String queueName) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }
}
