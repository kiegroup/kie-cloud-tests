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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.ApbImageGetter;
import org.kie.cloud.openshift.util.DockerRegistryDeployer;
import org.kie.cloud.openshift.util.OpenShiftTemplateProcessor;
import org.kie.cloud.openshift.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.xtf.openshift.OpenShiftBinaryClient;
import cz.xtf.wait.SimpleWaiter;

public class KieServerWithExternalDatabaseScenarioApb extends OpenShiftScenario implements KieServerWithExternalDatabaseScenario {

    private KieServerDeploymentImpl kieServerDeployment;
    private DockerDeployment dockerDeployment;
    private Map<String, String> extraVars;

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithExternalDatabaseScenario.class);

    public KieServerWithExternalDatabaseScenarioApb(Map<String, String> extraVars) {
        this.extraVars = extraVars;
    }

    @Override public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override public void deploy() {
        super.deploy();

        ExternalDatabase externalDatabase = ApbExternalDatabaseProvider.getExternalDatabase();
        extraVars.putAll(externalDatabase.getExternalDatabaseEnvironmentVariables());

        externalDatabase.getExternalDriver().ifPresent(val -> {
            ExternalDriver externalDriver = externalDatabase.getExternalDriver().get();
            String kieServerCustomImageStreamName = "kieserver-openshift-with-custom-driver";

            dockerDeployment = DockerRegistryDeployer.deploy(project);

            URL driverBinaryUrl = OpenShiftConstants.getKieJdbcDriverBinaryUrl();
            File driverBinaryFileLocation = externalDriver.getDriverBinaryFileLocation();
            downloadDriverBinary(driverBinaryUrl, driverBinaryFileLocation);

            installDriverImageToRegistry(dockerDeployment, externalDriver);
            createDriverImageStreams(dockerDeployment, externalDriver);
            buildCustomKieServerImageStream(externalDriver, kieServerCustomImageStreamName);

            extraVars.put(OpenShiftApbConstants.KIE_SERVER_IMAGE_STREAM_NAME, kieServerCustomImageStreamName);
        });

        //logger.info("Processing template and creating resources from " + OpenShiftTemplate.KIE_SERVER_DATABASE_EXTERNAL.getTemplateUrl().toString());
        //extraVars.put(OpenShiftApbConstants.IMAGE_STREAM_NAMESPACE, project.getName());
        //project.processTemplateAndCreateResources(OpenShiftTemplate.KIE_SERVER_DATABASE_EXTERNAL.getTemplateUrl(), extraVars);

        logger.info("Creating trusted secret");
        deployCustomTrustedSecret();

        logger.info("Processesin APB image plan: " + extraVars.get(OpenShiftApbConstants.APB_PLAN_ID));
        //extraVars.put(OpenShiftApbConstants.IMAGE_STREAM_NAMESPACE, projectName);
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

    @Override public List<Deployment> getDeployments() {
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

    private void downloadDriverBinary(URL driverBinaryUrl, File driverBinaryFileLocation) {
        logger.info("Downloading JDBC driver from " + driverBinaryUrl.toString() + " to " + driverBinaryFileLocation.getAbsolutePath());
        driverBinaryFileLocation.getParentFile().mkdirs();
        if (driverBinaryFileLocation.exists()) {
            driverBinaryFileLocation.delete();
        }

        try (ReadableByteChannel rbc = Channels.newChannel(driverBinaryUrl.openStream());
             FileOutputStream fos = new FileOutputStream(driverBinaryFileLocation);) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            throw new RuntimeException("Error while downloading driver binary.", e);
        }
    }

    private void installDriverImageToRegistry(DockerDeployment dockerDeployment, ExternalDriver externalDriver) {
        File kieJdbcDriverScriptsFolder = OpenShiftConstants.getKieJdbcDriverScriptsFolder();
        String dockerImageBuildCommand = externalDriver.getDockerImageBuildCommand(kieJdbcDriverScriptsFolder, dockerDeployment.getUrl());
        String dockerTag = externalDriver.getDockerTag(dockerDeployment.getUrl());

        try (ProcessExecutor processExecutor = new ProcessExecutor()) {
            logger.info("Building JDBC driver image.");
            processExecutor.executeProcessCommand(dockerImageBuildCommand);

            logger.info("Pushing JDBC driver image to Docker registry.");
            processExecutor.executeProcessCommand("docker push " + dockerTag);
        }
    }

    private void createDriverImageStreams(DockerDeployment dockerDeployment, ExternalDriver externalDriver) {
        String imageStreamName = externalDriver.getImageName();
        String dockerTag = externalDriver.getDockerTag(dockerDeployment.getUrl());

        project.createImageStream(imageStreamName, dockerTag);
    }

    private void buildCustomKieServerImageStream(ExternalDriver externalDriver, String kieServerCustomImageStreamName) {
        logger.info("Build Kie server custom image with JDBC driver included.");
        String buildName = externalDriver.getImageName();
        String kieServerImageStreamName = OpenShiftTemplateProcessor.getParameterValue(OpenShiftTemplate.KIE_SERVER_DATABASE_EXTERNAL, OpenShiftApbConstants.KIE_SERVER_IMAGE_STREAM_NAME);
        String kieServerImageStreamTag = OpenShiftTemplateProcessor.getParameterValue(OpenShiftTemplate.KIE_SERVER_DATABASE_EXTERNAL, OpenShiftApbConstants.APB_IMAGE_STREAM_TAG);
        String originalKieServerImageStream = project.getName() + "/" + kieServerImageStreamName + ":" + kieServerImageStreamTag;
        String jdbcSourceImage = project.getName() + "/" + externalDriver.getImageName() + ":" + externalDriver.getImageVersion();
        String targetKieServerImageStream = kieServerCustomImageStreamName + ":" + kieServerImageStreamTag;

        OpenShiftBinaryClient.getInstance().project(project.getName());
        OpenShiftBinaryClient.getInstance().executeCommand("Custom Kie server image build failed.", "new-build", "--name", buildName, "--image-stream=" + originalKieServerImageStream, "--source-image=" + jdbcSourceImage, "--source-image-path=" + externalDriver.getSourceImagePath(), "--to=" + targetKieServerImageStream, "-e", "CUSTOM_INSTALL_DIRECTORIES=" + externalDriver.getCustomInstallDirectories());

        waitUntilBuildCompletes(buildName);
    }

    private void waitUntilBuildCompletes(String buildName) {
        try {
            new SimpleWaiter(() -> project.getOpenShiftUtil().getLatestBuild(buildName) != null).timeout(TimeUnit.MINUTES, 1).execute();
            project.getOpenShiftUtil().waiters().hasBuildCompleted(project.getOpenShiftUtil().getLatestBuild(buildName).getMetadata().getName()).execute();
        } catch (TimeoutException e) {
            throw new RuntimeException("Error while waiting for the custom Kie server build to finish.", e);
        }
    }

    private void deployCustomTrustedSecret() {
        project.processTemplateAndCreateResources(OpenShiftTemplate.CUSTOM_TRUSTED_SECRET.getTemplateUrl(),
                Collections.emptyMap());

        extraVars.put(OpenShiftApbConstants.KIESERVER_SECRET_NAME, DeploymentConstants.getCustomTrustedSecretName());
        extraVars.put(OpenShiftApbConstants.KIESERVER_KEYSTORE_ALIAS,
                DeploymentConstants.getCustomTrustedKeystoreAlias());
        extraVars.put(OpenShiftApbConstants.KIESERVER_KEYSTORE_PWD, DeploymentConstants.getCustomTrustedKeystorePwd());
    }
}
