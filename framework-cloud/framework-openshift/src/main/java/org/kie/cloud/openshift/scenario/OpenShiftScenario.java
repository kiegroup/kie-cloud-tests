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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.api.model.Pod;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.DeploymentScenarioListener;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.images.imagestream.ImageStreamProvider;
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
        logCollectorExecutorService = Executors.newScheduledThreadPool(1);
        instancesLogCollectorRunnable = new InstancesLogCollectorRunnable(project, getLogFolderName());
        logCollectorExecutorService.scheduleWithFixedDelay(instancesLogCollectorRunnable, 0, DEFAULT_SCHEDULED_FIX_RATE_LOG_COLLECTOR_IN_SECONDS, TimeUnit.SECONDS);

        logger.info("Creating generally used secret from " + OpenShiftTemplate.SECRET.getTemplateUrl().toString());
        project.processTemplateAndCreateResources(OpenShiftTemplate.SECRET.getTemplateUrl(), Collections.singletonMap(OpenShiftConstants.SECRET_NAME, OpenShiftConstants.getKieApplicationSecretName()));

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
     * Deploy Kie deployments for this scenario and wait until deployments are ready for use.
     */
    protected abstract void deployKieDeployments();

    @Override
    public void undeploy() {
        try {
            logger.info("Release log collector(s)");
            try {
                logCollectorExecutorService.shutdownNow();
                logCollectorExecutorService = null;
                instancesLogCollectorRunnable.closeAndFlushRemainingInstanceCollectors(5000);
            } catch (Exception e) {
                logger.error("Error killing log collector thread", e);
            }

            logger.info("Store project events.");
            EventsRecorder.recordProjectEvents(project, logFolderName);

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

    @Override
    public void addDeploymentScenarioListener(DeploymentScenarioListener<T> deploymentScenarioListener) {
        deploymentScenarioListeners.add(deploymentScenarioListener);
    }
}
