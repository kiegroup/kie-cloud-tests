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

package org.kie.cloud.openshift.scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.api.model.Pod;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.DeploymentScenarioListener;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.images.imagestream.ImageStreamProvider;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.log.EventsRecorder;
import org.kie.cloud.openshift.log.InstancesLogCollectorRunnable;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpenShiftScenario<T extends DeploymentScenario<T>> implements DeploymentScenario<T> {

    private static final Integer DEFAULT_SCHEDULED_FIX_RATE_LOG_COLLECTOR_IN_SECONDS = 5;

    protected String projectName;
    protected Project project;
    private String logFolderName;
    private boolean createImageStreams;

    private ScheduledExecutorService logCollectorExecutorService;
    private InstancesLogCollectorRunnable instancesLogCollectorRunnable;

    private List<DeploymentScenarioListener<T>> deploymentScenarioListeners = new ArrayList<>();
    protected List<ExternalDeployment<?, ?>> externalDeployments = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftScenario.class);

    public OpenShiftScenario() {
        this(true);
    }

    public OpenShiftScenario(boolean createImageStreams) {
        this.createImageStreams = createImageStreams;
    }

    @Override
    public String getNamespace() {
        return projectName;
    }

    @Override
    public String getLogFolderName() {
        if (logFolderName == null) {
            return projectName;
        }
        return logFolderName;
    }

    @Override
    public void setLogFolderName(String logFolderName) {
        this.logFolderName = logFolderName;
    }

    @Override
    public final void deploy() {

        // OpenShift restriction: Hostname must be shorter than 63 characters
        projectName = UUID.randomUUID().toString().substring(0, 4);
        OpenShiftConstants.getNamespacePrefix().ifPresent(p -> projectName = p + "-" + projectName);

        logger.info("Generated project name is " + projectName);

        logger.info("Creating project " + projectName);
        project = OpenShiftController.createProject(projectName);

        // Init the log collector
        logger.info("Launch instances log collector on project {}", projectName);
        initLogCollectors();

        deploySecretConfig();
        deploySecretAppUser();

        if (createImageStreams) {
            logger.info("Creating image streams.");
            ImageStreamProvider.createImageStreamsInProject(project);
        }

        for (DeploymentScenarioListener<T> deploymentScenarioListener : deploymentScenarioListeners) {
            deploymentScenarioListener.beforeDeploymentStarted((T) this);
        }

        deployKieDeployments();
    }

    /**
     * Deploy Kie deployments for this scenario and wait until deployments are ready
     * for use.
     */
    protected abstract void deployKieDeployments();

    protected void configureWithExternalDeployment(ExternalDeployment<?, ?> externalDeployment) {
        logger.warn("No configuration with external deployment done in {}", this.getClass().getName());
    }

    protected void removeConfigurationFromExternalDeployment(ExternalDeployment<?, ?> externalDeployment) {
        logger.warn("No remove configuration from external deployment done in {}", this.getClass().getName());
    }

    @Override
    public void undeploy() {
        for (DeploymentScenarioListener<T> deploymentScenarioListener : deploymentScenarioListeners) {
            deploymentScenarioListener.afterScenarioFinished((T) this);
        }

        try {
            logger.info("Release log collector(s)");
            releaseLogCollectors();

            logger.info("Store project events.");
            EventsRecorder.recordProjectEvents(project, getLogFolderName());

            project.delete();
            project.close();
        } catch (Exception e) {
            logger.error("Error undeploy", e);
            throw new RuntimeException("Error while undeploying scenario.", e);
        }
    }

    protected void logNodeNameOfAllInstances() {
        for (Deployment deployment : getDeployments()) {
            deployment.getInstances().forEach(instance -> {
                Pod pod = project.getOpenShift().getPod(instance.getName());
                String podName = pod.getMetadata().getName();
                String instanceNodeName = pod.getSpec().getNodeName();
                logger.info("Node name of the {}: {} ", podName, instanceNodeName);
            });
        }
    }

    private void initLogCollectors() {
        logCollectorExecutorService = Executors.newScheduledThreadPool(1);
        instancesLogCollectorRunnable = new InstancesLogCollectorRunnable(project, getLogFolderName());
        logCollectorExecutorService.scheduleWithFixedDelay(instancesLogCollectorRunnable, 0, DEFAULT_SCHEDULED_FIX_RATE_LOG_COLLECTOR_IN_SECONDS, TimeUnit.SECONDS);
    }

    private void releaseLogCollectors() {
        try {
            if (Objects.nonNull(logCollectorExecutorService)) {
                logCollectorExecutorService.shutdownNow();
                logCollectorExecutorService = null;
            }
            if (Objects.nonNull(instancesLogCollectorRunnable)) {
                instancesLogCollectorRunnable.closeAndFlushRemainingInstanceCollectors(5000);
            }
        } catch (Exception e) {
            logger.error("Error killing log collector thread", e);
        }
    }

    @Override
    public void addDeploymentScenarioListener(DeploymentScenarioListener<T> deploymentScenarioListener) {
        deploymentScenarioListeners.add(deploymentScenarioListener);
    }

    /**
     * Add an external deployment to be executed before the specific scenario deployments are done
     * and undeployed when scenario is over.
     * 
     * This implements and add a deployment scenario listener to the scenario to be launched accordingly.
     * 
     * <b>Note that the deployment does NOT wait for the deployment to be ready.</b>
     * 
     * @param externalDeployment External deployment to add to the scenario
     */
    public void addExtraDeployment(ExternalDeployment<?, ?> externalDeployment) {
        externalDeployments.add(externalDeployment);
        addDeploymentScenarioListener(new DeploymentScenarioListener<T>() {

            @Override
            public void beforeDeploymentStarted(T deploymentScenario) {
                Deployment deployment = externalDeployment.deploy(project);
                deployment.waitForScheduled();
                configureWithExternalDeployment(externalDeployment);
            }

            @Override
            public void afterScenarioFinished(T deploymentScenario) {
                removeConfigurationFromExternalDeployment(externalDeployment);
            }

        });
    }

    /**
     * Add an external deployment to be executed before the specific scenario deployments are done
     * and undeployed when scenario is over, in a synchronized manner, meaning that it is waiting
     * that the deployment is ready to going further.
     * 
     * This implements and add a deployment scenario listener to the scenario to be launched accordingly.
     * 
     * @param externalDeployment External deployment to add to the scenario
     */
    public void addExtraDeploymentSynchronized(ExternalDeployment<?, ?> externalDeployment) {
        externalDeployments.add(externalDeployment);
        addDeploymentScenarioListener(new DeploymentScenarioListener<T>() {

            @Override
            public void beforeDeploymentStarted(T deploymentScenario) {
                Deployment deployment = externalDeployment.deploy(project);
                deployment.waitForScale();
                configureWithExternalDeployment(externalDeployment);
            }

            @Override
            public void afterScenarioFinished(T deploymentScenario) {
                removeConfigurationFromExternalDeployment(externalDeployment);
            }

        });
    }

    @Override
    public MavenRepositoryDeployment getMavenRepositoryDeployment() {
        return externalDeployments.stream().filter(deployment -> ExternalDeploymentID.MAVEN_REPOSITORY.equals(deployment.getKey()))
                                           .map(ExternalDeployment::getDeploymentInformation)
                                           .map(deployment -> (MavenRepositoryDeployment) deployment)
                                           .findAny()
                                           .orElseThrow(() -> new RuntimeException("Maven repository deployment not found."));
    }

    private void deploySecretConfig() {
        logger.info("Creating generally used secret from " + OpenShiftTemplate.SECRET.getTemplateUrl().toString());
        Map<String, String> secretConfig = new HashMap<>();
        secretConfig.put(OpenShiftConstants.SECRET_NAME, OpenShiftConstants.getKieApplicationSecretName());
        secretConfig.put(OpenShiftConstants.CREDENTIALS_SECRET, DeploymentConstants.getAppCredentialsSecretName());

        project.processTemplateAndCreateResources(OpenShiftTemplate.SECRET.getTemplateUrl(), secretConfig);
    }

    private void deploySecretAppUser() {
        logger.info("Creating user secret '{}'", DeploymentConstants.getAppCredentialsSecretName());
        Map<String, String> data = new HashMap<>();
        data.put(OpenShiftConstants.KIE_ADMIN_USER, DeploymentConstants.getAppUser());
        data.put(OpenShiftConstants.KIE_ADMIN_PWD, DeploymentConstants.getAppPassword());

        project.createSecret(DeploymentConstants.getAppCredentialsSecretName(), data);
    }
}
