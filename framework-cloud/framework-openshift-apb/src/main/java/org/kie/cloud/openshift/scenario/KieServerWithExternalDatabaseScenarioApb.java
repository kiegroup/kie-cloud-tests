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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.DockerDeployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.database.driver.ExternalDriver;
import org.kie.cloud.openshift.database.external.ApbExternalDatabaseProvider;
import org.kie.cloud.openshift.database.external.ExternalDatabase;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;
import org.kie.cloud.openshift.deployment.external.ExternalDeploymentApb;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.ApbImageGetter;
import org.kie.cloud.openshift.util.DockerRegistryDeployer;
import org.kie.cloud.openshift.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerWithExternalDatabaseScenarioApb extends OpenShiftScenario<KieServerWithExternalDatabaseScenario> implements KieServerWithExternalDatabaseScenario {

    private KieServerDeploymentImpl kieServerDeployment;
    private DockerDeployment dockerDeployment;
    private Map<String, String> extraVars;

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithExternalDatabaseScenario.class);

    public KieServerWithExternalDatabaseScenarioApb(Map<String, String> extraVars) {
        this.extraVars = extraVars;
    }

    @Override
    public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override
    protected void deployKieDeployments() {
        ExternalDatabase externalDatabase = ApbExternalDatabaseProvider.getExternalDatabase();
        extraVars.putAll(externalDatabase.getExternalDatabaseEnvironmentVariables());

        dockerDeployment = DockerRegistryDeployer.deploy(project);

        // Create image stream from external image with driver and reference it for template
        installDriverImageToRegistry(dockerDeployment, externalDatabase.getExternalDriver());
        createDriverImageStreams(dockerDeployment, externalDatabase.getExternalDriver());

        logger.info("Creating trusted secret");
        deployCustomTrustedSecret();

        logger.info("Processesin APB image plan: " + extraVars.get(OpenShiftApbConstants.APB_PLAN_ID));
        extraVars.put(OpenShiftApbConstants.IMAGE_STREAM_NAMESPACE, projectName);
        extraVars.put("namespace", projectName);
        extraVars.put("cluster", "openshift");
        project.processApbRun(ApbImageGetter.fromImageStream(), extraVars);

        kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());

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

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(kieServerDeployment, dockerDeployment));
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

    private void installDriverImageToRegistry(DockerDeployment dockerDeployment, ExternalDriver externalDriver) {
        File kieJdbcDriverScriptsFolder = OpenShiftConstants.getKieJdbcDriverScriptsFolder();
        String buildCommand = externalDriver.getCekitImageBuildCommand();
        String sourceDockerTag = externalDriver.getSourceDockerTag();
        String targetDockerTag = externalDriver.getTargetDockerTag(dockerDeployment.getUrl());

        try (ProcessExecutor processExecutor = new ProcessExecutor()) {
            logger.info("Building JDBC driver image.");
            processExecutor.executeProcessCommand(buildCommand, kieJdbcDriverScriptsFolder.toPath());

            logger.info("Pushing JDBC driver image to Docker registry.");
            processExecutor.executeProcessCommand("docker tag " + sourceDockerTag + " " + targetDockerTag);
            processExecutor.executeProcessCommand("docker push " + targetDockerTag);
        }
    }

    private void createDriverImageStreams(DockerDeployment dockerDeployment, ExternalDriver externalDriver) {
        String imageStreamName = externalDriver.getImageName();
        String dockerTag = externalDriver.getTargetDockerTag(dockerDeployment.getUrl());

        project.createImageStream(imageStreamName, dockerTag);
    }

    private void deployCustomTrustedSecret() {
        project.processTemplateAndCreateResources(OpenShiftTemplate.CUSTOM_TRUSTED_SECRET.getTemplateUrl(),
                Collections.emptyMap());

        extraVars.put(OpenShiftApbConstants.KIESERVER_SECRET_NAME, DeploymentConstants.getCustomTrustedSecretName());
        extraVars.put(OpenShiftApbConstants.KIESERVER_KEYSTORE_ALIAS,
                DeploymentConstants.getCustomTrustedKeystoreAlias());
        extraVars.put(OpenShiftApbConstants.KIESERVER_KEYSTORE_PWD, DeploymentConstants.getCustomTrustedKeystorePwd());
    }

    public Map<String, String> getScenarioEnvironment() {
        return new HashMap<>(extraVars);
    }
}
