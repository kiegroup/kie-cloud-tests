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
import java.util.Collections;
import java.util.List;

import cz.xtf.openshift.OpenShiftBinaryClient;
import cz.xtf.openshift.OpenShiftUtils;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.KieAppDoneable;
import org.kie.cloud.openshift.operator.model.KieAppList;
import org.kie.cloud.openshift.operator.model.components.Console;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.SmartRouter;
import org.kie.cloud.openshift.operator.resources.OpenShiftResource;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.scenario.OpenShiftScenario;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpenShiftOperatorScenario extends OpenShiftScenario {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftOperatorScenario.class);

    @Override
    public void deploy() {
        super.deploy();

        try {
            // Operations need to be done as an administrator
            OpenShiftBinaryClient.getInstance().login(OpenShiftConstants.getOpenShiftUrl(), OpenShiftConstants.getOpenShiftAdminUserName(), OpenShiftConstants.getOpenShiftAdminPassword(), null);

            createCustomResourceDefinitionsInOpenShift();
            createServiceAccountInProject(project);
            createRoleInProject(project);
            createRoleBindingsInProject(project);
            createOperatorInProject(project);
        } catch (IOException e) {
            throw new RuntimeException("Error while initializing Operator.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while initializing Operator.", e);
        }

        deployCustomTrustedSecret();
    }

    private void createCustomResourceDefinitionsInOpenShift() {
        List<CustomResourceDefinition> customResourceDefinitions = OpenShiftUtils.admin().client().customResourceDefinitions().list().getItems();
        boolean operatorCrdExists = customResourceDefinitions.stream().anyMatch(i -> i.getMetadata().getName().equals("kieapps.app.kiegroup.org"));
        if(!operatorCrdExists) {
            logger.info("Creating custom resource definitions from " + OpenShiftResource.CRD.getResourceUrl().toString());
            OpenShiftBinaryClient instance = OpenShiftBinaryClient.getInstance();
            try (ProcessExecutor executor = new ProcessExecutor()) {
                executor.executeProcessCommand(instance.getOcBinaryPath().toString() + " --config=" + instance.getOcConfigPath().toString() + " create -n " + getNamespace() + " -f " + OpenShiftResource.CRD.getResourceUrl().toString());
            }
        }
    }

    private void createServiceAccountInProject(Project project) {
        logger.info("Creating service account in project '" + project.getName() + "' from " + OpenShiftResource.SERVICE_ACCOUNT.getResourceUrl().toString());
        OpenShiftBinaryClient.getInstance().project(project.getName());
        OpenShiftBinaryClient.getInstance().executeCommand("Service account creation failed.", "create", "-f", OpenShiftResource.SERVICE_ACCOUNT.getResourceUrl().toString());
    }

    private void createRoleInProject(Project project) {
        logger.info("Creating role in project '" + project.getName() + "' from " + OpenShiftResource.ROLE.getResourceUrl().toString());
        OpenShiftBinaryClient.getInstance().project(project.getName());
        OpenShiftBinaryClient.getInstance().executeCommand("Role creation failed.", "create", "-f", OpenShiftResource.ROLE.getResourceUrl().toString());
    }

    private void createRoleBindingsInProject(Project project) {
        logger.info("Creating role bindings in project '" + project.getName() + "' from " + OpenShiftResource.ROLE_BINDING.getResourceUrl().toString());
        OpenShiftBinaryClient.getInstance().project(project.getName());
        OpenShiftBinaryClient.getInstance().executeCommand("Role binding failed.", "create", "-f", OpenShiftResource.ROLE_BINDING.getResourceUrl().toString());
    }

    private void createOperatorInProject(Project project) {
        logger.info("Creating operator in project '" + project.getName() + "' from " + OpenShiftResource.OPERATOR.getResourceUrl().toString());
        OpenShiftBinaryClient.getInstance().project(project.getName());
        OpenShiftBinaryClient.getInstance().executeCommand("operator failed.", "create", "-f", OpenShiftResource.OPERATOR.getResourceUrl().toString());
        // wait until operator is ready
        project.getOpenShiftUtil().waiters().areExactlyNPodsRunning(1, "name", "kie-cloud-operator");
    }

    private void deployCustomTrustedSecret() {
        project.processTemplateAndCreateResources(OpenShiftTemplate.CUSTOM_TRUSTED_SECRET.getTemplateUrl(), Collections.emptyMap());
    }

    protected void registerCustomTrustedSecret(Console console) {
        console.addEnv(new Env("HTTPS_NAME", DeploymentConstants.getCustomTrustedKeystoreAlias()));
        console.addEnv(new Env("HTTPS_PASSWORD", DeploymentConstants.getCustomTrustedKeystorePwd()));
        console.setKeystoreSecret(DeploymentConstants.getCustomTrustedSecretName());
    }

    protected void registerCustomTrustedSecret(SmartRouter smartRouter) {
        smartRouter.addEnv(new Env("KIE_SERVER_ROUTER_TLS_KEYSTORE_KEYALIAS", DeploymentConstants.getCustomTrustedKeystoreAlias()));
        smartRouter.addEnv(new Env("KIE_SERVER_ROUTER_TLS_KEYSTORE_PASSWORD", DeploymentConstants.getCustomTrustedKeystorePwd()));
        smartRouter.setKeystoreSecret(DeploymentConstants.getCustomTrustedSecretName());
    }

    protected void registerCustomTrustedSecret(Server server) {
        server.addEnv(new Env("HTTPS_NAME", DeploymentConstants.getCustomTrustedKeystoreAlias()));
        server.addEnv(new Env("HTTPS_PASSWORD", DeploymentConstants.getCustomTrustedKeystorePwd()));
        server.setKeystoreSecret(DeploymentConstants.getCustomTrustedSecretName());
    }

    /**
     * @return OpenShift client which is aware of KieApp custom resource.
     */
    protected NonNamespaceOperation<KieApp, KieAppList, KieAppDoneable, Resource<KieApp, KieAppDoneable>> getKieAppClient() {
        CustomResourceDefinition customResourceDefinition = OpenShiftUtils.admin().client().customResourceDefinitions().withName("kieapps.app.kiegroup.org").get();
        return OpenShiftUtils.admin().client().customResources(customResourceDefinition, KieApp.class, KieAppList.class, KieAppDoneable.class).inNamespace(getNamespace());
    }
}
