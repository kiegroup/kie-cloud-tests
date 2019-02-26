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

package org.kie.cloud.openshift.operator.scenario;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import cz.xtf.openshift.OpenShiftBinaryClient;
import cz.xtf.openshift.OpenShiftUtils;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.openshift.api.model.ImageStream;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.operator.resources.OpenShiftResource;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.scenario.OpenShiftScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpenShiftOperatorScenario<T extends DeploymentScenario<T>> extends OpenShiftScenario<T> {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftOperatorScenario.class);

    @Override
    protected void deployKieDeployments() {
        deployOperator();
        deployCustomResource();
    }

    private void deployOperator() {
        try {
            // Operations need to be done as an administrator
            OpenShiftBinaryClient.getInstance().login(OpenShiftConstants.getOpenShiftUrl(), OpenShiftConstants.getOpenShiftAdminUserName(), OpenShiftConstants.getOpenShiftAdminPassword(), null);

            createImageStreamsInOpenShiftProject();
            createCustomResourceDefinitionsInOpenShift();
            createRoleBindingsInProject(project);
            createOperatorInProject(project);
        } catch (IOException e) {
            throw new RuntimeException("Error while initializing Operator.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while initializing Operator.", e);
        }
    }

    private void createImageStreamsInOpenShiftProject() {
        List<ImageStream> imageStreams = OpenShiftUtils.admin().client().imageStreams().inNamespace("openshift").list().getItems();
        boolean kieServerImageStreamExists = imageStreams.stream().anyMatch(i -> i.getMetadata().getName().matches(".*kieserver-openshift"));
        if (!kieServerImageStreamExists) {
            logger.info("Creating image streams in 'openshift' project from " + OpenShiftConstants.getKieImageStreams());
            try {
                KubernetesList resourceList = OpenShiftUtils.admin().client().lists().load(new URL(OpenShiftConstants.getKieImageStreams())).get();
                OpenShiftUtils.admin().client().lists().inNamespace("openshift").create(resourceList);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Malformed URL of Kie image stream.", e);
            }
        }
    }

    private void createCustomResourceDefinitionsInOpenShift() {
        List<CustomResourceDefinition> customResourceDefinitions = OpenShiftUtils.admin().client().customResourceDefinitions().list().getItems();
        boolean operatorCrdExists = customResourceDefinitions.stream().anyMatch(i -> i.getMetadata().getName().equals("apps.kiegroup.org"));
        if(!operatorCrdExists) {
            logger.info("Creating custom resource definitions from " + OpenShiftResource.CRD.getResourceUrl().toString());
            OpenShiftBinaryClient.getInstance().executeCommand("CRD failed.", "create", "-f", OpenShiftResource.CRD.getResourceUrl().toString());
        }
    }

    private void createRoleBindingsInProject(Project project) {
        logger.info("Creating role bindings in project '" + project.getName() + "' from " + OpenShiftResource.RBAC.getResourceUrl().toString());
        OpenShiftBinaryClient.getInstance().project(project.getName());
        OpenShiftBinaryClient.getInstance().executeCommand("RBAC failed.", "create", "-f", OpenShiftResource.RBAC.getResourceUrl().toString());
    }

    private void createOperatorInProject(Project project) {
        logger.info("Creating operator in project '" + project.getName() + "' from " + OpenShiftResource.OPERATOR.getResourceUrl().toString());
        OpenShiftBinaryClient.getInstance().project(project.getName());
        OpenShiftBinaryClient.getInstance().executeCommand("operator failed.", "create", "-f", OpenShiftResource.OPERATOR.getResourceUrl().toString());
        // wait until operator is ready
        project.getOpenShiftUtil().waiters().areExactlyNPodsRunning(1, "name", "kie-cloud-operator");
    }

    protected abstract void deployCustomResource();
}
