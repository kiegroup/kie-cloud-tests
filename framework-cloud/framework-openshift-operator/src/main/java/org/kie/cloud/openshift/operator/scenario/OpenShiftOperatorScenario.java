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

import java.util.Collections;

import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.rbac.KubernetesRole;
import io.fabric8.kubernetes.api.model.rbac.KubernetesRoleBinding;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorConstants;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.KieAppDoneable;
import org.kie.cloud.openshift.operator.model.KieAppList;
import org.kie.cloud.openshift.operator.model.components.Console;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.SmartRouter;
import org.kie.cloud.openshift.operator.resources.OpenShiftResource;
import org.kie.cloud.openshift.operator.scenario.extra.ExtraScenarioDeploymentOperator;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.scenario.OpenShiftScenario;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpenShiftOperatorScenario<T extends DeploymentScenario<T>> extends OpenShiftScenario<T> {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftOperatorScenario.class);

    protected KieApp kieApp;

    public OpenShiftOperatorScenario(KieApp kieApp) {
        super(false);
        this.kieApp = kieApp;
    }

    @Override
    protected void deployKieDeployments() {
        deployOperator();
        deployCustomTrustedSecret();
        deployCustomResource();
    }

    private void deployOperator() {
        // Operations need to be done as an administrator
        OpenShiftBinary adminBinary = OpenShifts.adminBinary();

        createCustomResourceDefinitionsInOpenShift(adminBinary);
        createServiceAccountInProject(project);
        createRoleInProject(project);
        createRoleBindingsInProject(project);
        createOperatorInProject(project);
    }

    private void createCustomResourceDefinitionsInOpenShift(OpenShiftBinary adminBinary) {
        // TODO: Commented out due to UnrecognizedPropertyException, uncomment with raised Fabric8 version to check if it is fixed already.
        //        List<CustomResourceDefinition> customResourceDefinitions = OpenShifts.master().customResourceDefinitions().list().getItems();
        //        boolean operatorCrdExists = customResourceDefinitions.stream().anyMatch(i -> i.getMetadata().getName().equals("kieapps.app.kiegroup.org"));
        //        if(!operatorCrdExists) {
        logger.info("Creating custom resource definitions from " + OpenShiftResource.CRD.getResourceUrl().toString());
        adminBinary.execute("create", "-n", getNamespace(), "-f", OpenShiftResource.CRD.getResourceUrl().toString());
        //        }
    }

    private void createServiceAccountInProject(Project project) {
        logger.info("Creating service account in project '" + project.getName() + "' from " + OpenShiftResource.SERVICE_ACCOUNT.getResourceUrl().toString());
        ServiceAccount serviceAccount = project.getOpenShiftAdmin().serviceAccounts().load(OpenShiftResource.SERVICE_ACCOUNT.getResourceUrl()).get();
        project.getOpenShiftAdmin().serviceAccounts().inNamespace(project.getName()).create(serviceAccount);
    }

    private void createRoleInProject(Project project) {
        logger.info("Creating role in project '" + project.getName() + "' from " + OpenShiftResource.ROLE.getResourceUrl().toString());
        KubernetesRole role = project.getOpenShiftAdmin().rbac().kubernetesRoles().load(OpenShiftResource.ROLE.getResourceUrl()).get();
        project.getOpenShiftAdmin().rbac().kubernetesRoles().inNamespace(project.getName()).create(role);
    }

    private void createRoleBindingsInProject(Project project) {
        logger.info("Creating role bindings in project '" + project.getName() + "' from " + OpenShiftResource.ROLE_BINDING.getResourceUrl().toString());
        KubernetesRoleBinding roleBinding = project.getOpenShiftAdmin().rbac().kubernetesRoleBindings().load(OpenShiftResource.ROLE_BINDING.getResourceUrl()).get();
        project.getOpenShiftAdmin().rbac().kubernetesRoleBindings().inNamespace(project.getName()).create(roleBinding);
    }

    private void createOperatorInProject(Project project) {
        logger.info("Creating operator in project '" + project.getName() + "' from " + OpenShiftResource.OPERATOR.getResourceUrl().toString());
        Deployment deployment = project.getOpenShift().apps().deployments().load(OpenShiftResource.OPERATOR.getResourceUrl()).get();
        deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImage(OpenShiftOperatorConstants.getKieOperatorImageTag());
        project.getOpenShift().apps().deployments().create(deployment);

        // wait until operator is ready
        project.getOpenShift().waiters().areExactlyNPodsRunning(1, "name", "kie-cloud-operator").waitFor();

        if (!OpenShiftOperatorConstants.skipKieOperatorConsoleCheck()) {
            // wait until operator console is ready
            project.getOpenShift().waiters().areExactlyNPodsRunning(1, "name", "console-cr-form").waitFor();
        }
    }

    protected abstract void deployCustomResource();

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
        CustomResourceDefinition customResourceDefinition = OpenShifts.master().customResourceDefinitions().withName("kieapps.app.kiegroup.org").get();
        return OpenShifts.master().customResources(customResourceDefinition, KieApp.class, KieAppList.class, KieAppDoneable.class).inNamespace(getNamespace());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void configureWithExtraDeployments() {
        getExtraScenarioDeployments().stream()
                                     .forEach(extraDeployment -> ((ExtraScenarioDeploymentOperator) extraDeployment).configure(kieApp));
    }
}
