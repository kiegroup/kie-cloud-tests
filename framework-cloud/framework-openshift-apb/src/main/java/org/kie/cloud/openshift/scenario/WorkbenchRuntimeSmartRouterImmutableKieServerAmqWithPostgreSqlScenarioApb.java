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

package org.kie.cloud.openshift.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.kie.cloud.api.deployment.AmqDeployment;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.deployment.AmqDeploymentImpl;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;
import org.kie.cloud.openshift.deployment.external.ExternalDeploymentApb;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.AmqImageStreamDeployer;
import org.kie.cloud.openshift.util.AmqSecretDeployer;
import org.kie.cloud.openshift.util.ApbImageGetter;
import org.kie.cloud.openshift.util.Git;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioApb extends OpenShiftScenario<WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario> implements
                                                                                       WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario {

    private KieServerDeploymentImpl kieServerDeployment;
    private DatabaseDeploymentImpl databaseDeployment;
    private SsoDeployment ssoDeployment;
    private AmqDeploymentImpl amqDeployment;
    private GitProvider gitProvider;

    private final ScenarioRequest request;

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithExternalDatabaseScenario.class);

    private Map<String, String> extraVars;

    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioApb(Map<String, String> extraVars, ScenarioRequest request) {
        this.extraVars = extraVars;
        this.request = request;
    }

    @Override
    protected void deployKieDeployments() {
        if (request.isDeploySso()) {
            logger.warn("SSO is configured for this test scenario. Kie Server SSO client can be set only for one Kie Server. For more deploymets it mus be configured manually.");
            ssoDeployment = SsoDeployer.deploy(project);

            extraVars.put(OpenShiftApbConstants.SSO_URL, SsoDeployer.createSsoEnvVariable(ssoDeployment.getUrl().toString()));
            extraVars.put(OpenShiftApbConstants.SSO_REALM, DeploymentConstants.getSsoRealm());

            extraVars.put(OpenShiftApbConstants.KIE_SERVER_SSO_CLIENT, "kie-server-client");
            extraVars.put(OpenShiftApbConstants.KIE_SERVER_SSO_SECRET, "kie-server-secret");

            extraVars.put(OpenShiftApbConstants.SSO_PRINCIPAL_ATTRIBUTE, "preferred_username");
        }

        if (request.getGitSettings() != null) {
            gitProvider = Git.getProvider(project, request.getGitSettings());
        }

        logger.info("Creating trusted secret");
        deployCustomTrustedSecret();

        logger.info("Creating AMQ secret");
        AmqSecretDeployer.create(project);
        logger.info("AMQ secret created");
        logger.info("Creating AMQ image stream");
        AmqImageStreamDeployer.deploy(project);
        logger.info("AMQ image stream created");

        logger.info("Processing APB image plan: " + extraVars.get(OpenShiftApbConstants.APB_PLAN_ID));
        extraVars.put(OpenShiftApbConstants.IMAGE_STREAM_NAMESPACE, projectName);
        extraVars.put("namespace", projectName);
        extraVars.put("amq_image_namespace", "openshift");
        extraVars.put("cluster", "openshift");
        project.processApbRun(ApbImageGetter.fromImageStream(), extraVars);

        kieServerDeployment = createKieServerDeployment(project);
        kieServerDeployment.setServiceSuffix("-0");
        kieServerDeployment.setUsername(DeploymentConstants.getAppUser());
        kieServerDeployment.setPassword(DeploymentConstants.getAppPassword());
        databaseDeployment = new DatabaseDeploymentImpl(project);
        databaseDeployment.setServiceSuffix("-0");
        amqDeployment = createAmqDeployment(project);

        logger.info("Waiting for AMQ deployment to become ready.");
        amqDeployment.waitForScale();

        logger.info("Waiting for Database deployment to become ready.");
        databaseDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logNodeNameOfAllInstances();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void configureWithExternalDeployment(ExternalDeployment<?, ?> externalDeployment) {
        ((ExternalDeploymentApb) externalDeployment).configure(extraVars);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void removeConfigurationFromExternalDeployment(ExternalDeployment<?, ?> externalDeployment) {
        ((ExternalDeploymentApb) externalDeployment).removeConfiguration(extraVars);
    }

    private void deployCustomTrustedSecret() {
        project.processTemplateAndCreateResources(OpenShiftTemplate.CUSTOM_TRUSTED_SECRET.getTemplateUrl(), Collections.emptyMap());

        extraVars.put(OpenShiftApbConstants.BUSINESSCENTRAL_SECRET_NAME, DeploymentConstants.getCustomTrustedSecretName());
        extraVars.put(OpenShiftApbConstants.BUSINESSCENTRAL_KEYSTORE_ALIAS, DeploymentConstants.getCustomTrustedKeystoreAlias());
        extraVars.put(OpenShiftApbConstants.BUSINESSCENTRAL_KEYSTORE_PWD, DeploymentConstants.getCustomTrustedKeystorePwd());
        extraVars.put(OpenShiftApbConstants.KIESERVER_SECRET_NAME, DeploymentConstants.getCustomTrustedSecretName());
        extraVars.put(OpenShiftApbConstants.KIESERVER_KEYSTORE_ALIAS, DeploymentConstants.getCustomTrustedKeystoreAlias());
        extraVars.put(OpenShiftApbConstants.KIESERVER_KEYSTORE_PWD, DeploymentConstants.getCustomTrustedKeystorePwd());
        extraVars.put(OpenShiftApbConstants.SMARTROUTER_SECRET_NAME, DeploymentConstants.getCustomTrustedSecretName());
        extraVars.put(OpenShiftApbConstants.SMARTROUTER_KEYSTORE_ALIAS, DeploymentConstants.getCustomTrustedKeystoreAlias());
        extraVars.put(OpenShiftApbConstants.SMARTROUTER_KEYSTORE_PWD, DeploymentConstants.getCustomTrustedKeystorePwd());
    }

    @Override
    public WorkbenchDeployment getWorkbenchRuntimeDeployment() {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public SmartRouterDeployment getSmartRouterDeployment() {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override
    public DatabaseDeployment getDatabaseDeployment() {
        return databaseDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<>(Arrays.asList(kieServerDeployment, databaseDeployment, ssoDeployment, amqDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Collections.emptyList();
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Arrays.asList(kieServerDeployment);
    }

    @Override
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return Collections.emptyList();
    }

    @Override
    public List<ControllerDeployment> getControllerDeployments() {
        return Collections.emptyList();
    }

    @Override
    public SsoDeployment getSsoDeployment() {
        return ssoDeployment;
    }

    @Override
    public AmqDeployment getAmqDeployment() {
        return amqDeployment;
    }

    @Override
    public Optional<GitProvider> getGitProvider() {
        return Optional.ofNullable(gitProvider);
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project) {
        KieServerDeploymentImpl deployment = new KieServerDeploymentImpl(project);
        deployment.setUsername(DeploymentConstants.getAppUser());
        deployment.setPassword(DeploymentConstants.getAppPassword());

        return deployment;
    }

    private AmqDeploymentImpl createAmqDeployment(Project project) {
        AmqDeploymentImpl deployment = new AmqDeploymentImpl(project);
        deployment.setUsername(DeploymentConstants.getAmqUsername());
        deployment.setPassword(DeploymentConstants.getAmqPassword());

        return deployment;
    }
}
