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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.ImageStream;
import org.apache.commons.lang3.StringUtils;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorConstants;
import org.kie.cloud.openshift.operator.deployment.external.ExternalDeploymentOperator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpenShiftOperatorScenario<T extends DeploymentScenario<T>> extends OpenShiftScenario<T> {

    protected static final String OPERATOR_DEPLOYMENT_NAME = "business-automation-operator";

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftOperatorScenario.class);

    protected KieApp kieApp;

    public OpenShiftOperatorScenario(KieApp kieApp) {
        super(true);
        this.kieApp = kieApp;
    }

    @Override
    protected void deployKieDeployments() {
        deployOperator();
        deployCustomResource();
    }

    @Override
    public void undeploy() {
        deleteClusterRoleBindingsInProject(project);
        super.undeploy();
    }

    private void deployOperator() {
        // Operations need to be done as an administrator
        OpenShiftBinary adminBinary = OpenShifts.adminBinary();

        createCustomResourceDefinitionsInOpenShift(adminBinary);
        createServiceAccountInProject(project);
        createRoleInProject(project);
        createRoleBindingsInProject(project);
        createClusterRoleInProject(project);
        createClusterRoleBindingsInProject(project);
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
        Role role = project.getOpenShiftAdmin().rbac().roles().load(OpenShiftResource.ROLE.getResourceUrl()).get();
        project.getOpenShiftAdmin().rbac().roles().inNamespace(project.getName()).create(role);
    }

    private void createRoleBindingsInProject(Project project) {
        logger.info("Creating role bindings in project '" + project.getName() + "' from " + OpenShiftResource.ROLE_BINDING.getResourceUrl().toString());
        RoleBinding roleBinding = project.getOpenShiftAdmin().rbac().roleBindings().load(OpenShiftResource.ROLE_BINDING.getResourceUrl()).get();
        project.getOpenShiftAdmin().rbac().roleBindings().inNamespace(project.getName()).create(roleBinding);
    }

    private void createClusterRoleInProject(Project project) {
        logger.info("Creating cluster role in project '" + getNamespace() + "' from " + OpenShiftResource.CLUSTER_ROLE.getResourceUrl().toString());
        ClusterRole role = project.getOpenShiftAdmin().rbac().clusterRoles().load(OpenShiftResource.CLUSTER_ROLE.getResourceUrl()).get();
        project.getOpenShiftAdmin().rbac().clusterRoles().inNamespace(getNamespace()).createOrReplace(role);
    }

    private void createClusterRoleBindingsInProject(Project project) {
        logger.info("Creating cluster role bindings in project '" + getNamespace() + "' from " + OpenShiftResource.CLUSTER_ROLE_BINDING.getResourceUrl().toString());
        ClusterRoleBinding roleBinding = project.getOpenShiftAdmin().rbac().clusterRoleBindings().load(OpenShiftResource.CLUSTER_ROLE_BINDING.getResourceUrl()).get();
        roleBinding.getMetadata().setName(OPERATOR_DEPLOYMENT_NAME + getNamespace());
        roleBinding.getMetadata().setNamespace(getNamespace());
        roleBinding.getSubjects().forEach(subject -> subject.setNamespace(getNamespace()));
        project.getOpenShiftAdmin().rbac().clusterRoleBindings().inNamespace(getNamespace()).create(roleBinding);
    }

    private void deleteClusterRoleBindingsInProject(Project project) {
        project.getOpenShiftAdmin().rbac().clusterRoleBindings().withName(OPERATOR_DEPLOYMENT_NAME + getNamespace()).delete();
    }

    private void createOperatorInProject(Project project) {
        logger.info("Creating operator in project '" + project.getName() + "' from " + OpenShiftResource.OPERATOR.getResourceUrl().toString());
        Deployment deployment = project.getOpenShift().apps().deployments().load(OpenShiftResource.OPERATOR.getResourceUrl()).get();

        // Get the operator image tag (composed of name + tag).
        // Retrieve the image name and see if it fits an image stream.
        // If yes, then use the image stream image's name and same tag as defined (use latest if no tag).
        // If not, use as it is as image name.
        String operatorImage = getLatestOperatorVersion();
        if (overridesVersionTag() != null) {
            operatorImage = StringUtils.substringBeforeLast(operatorImage, ":") + ":" + overridesVersionTag();
        }
        deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImage(operatorImage);
        project.getOpenShift().apps().deployments().create(deployment);

        // wait until operator is ready
        project.getOpenShift().waiters().areExactlyNPodsRunning(1, "name", OPERATOR_DEPLOYMENT_NAME).waitFor();

        if (!OpenShiftOperatorConstants.skipKieOperatorConsoleCheck()) {
            // wait until operator console is ready
            project.getOpenShift().waiters().areExactlyNPodsRunning(1, "name", "console-cr-form").waitFor();
        }
    }

    protected abstract void deployCustomResource();

    protected void registerTrustedSecret(Console console) {
        console.addEnv(new Env("HTTPS_NAME", DeploymentConstants.getTrustedKeystoreAlias()));
        console.addEnv(new Env("HTTPS_PASSWORD", DeploymentConstants.getTrustedKeystorePwd()));
        console.setKeystoreSecret(OpenShiftConstants.getKieApplicationSecretName());
    }

    protected void registerTrustedSecret(SmartRouter smartRouter) {
        smartRouter.addEnv(new Env("KIE_SERVER_ROUTER_TLS_KEYSTORE_KEYALIAS", DeploymentConstants.getTrustedKeystoreAlias()));
        smartRouter.addEnv(new Env("KIE_SERVER_ROUTER_TLS_KEYSTORE_PASSWORD", DeploymentConstants.getTrustedKeystorePwd()));
        smartRouter.setKeystoreSecret(OpenShiftConstants.getKieApplicationSecretName());
    }

    protected void registerTrustedSecret(Server server) {
        server.addEnv(new Env("HTTPS_NAME", DeploymentConstants.getTrustedKeystoreAlias()));
        server.addEnv(new Env("HTTPS_PASSWORD", DeploymentConstants.getTrustedKeystorePwd()));
        server.setKeystoreSecret(OpenShiftConstants.getKieApplicationSecretName());
    }

    /**
     * @return OpenShift client which is aware of KieApp custom resource.
     */
    protected NonNamespaceOperation<KieApp, KieAppList, KieAppDoneable, Resource<KieApp, KieAppDoneable>> getKieAppClient() {
        CustomResourceDefinition customResourceDefinition = OpenShifts.admin().customResourceDefinitions().withName("kieapps.app.kiegroup.org").get();
        return OpenShifts.admin().customResources(customResourceDefinition, KieApp.class, KieAppList.class, KieAppDoneable.class).inNamespace(getNamespace());
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void configureWithExternalDeployment(ExternalDeployment<?, ?> externalDeployment) {
        ((ExternalDeploymentOperator) externalDeployment).configure(kieApp);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void removeConfigurationFromExternalDeployment(ExternalDeployment<?, ?> externalDeployment) {
        ((ExternalDeploymentOperator) externalDeployment).removeConfiguration(kieApp);
    }

    /**
     * @return get the operator version to deploy from configuration.
     */
    protected String getLatestOperatorVersion() {
        String operatorImageTag = OpenShiftOperatorConstants.getKieOperatorImageTag();
        String[] split = operatorImageTag.split(":");
        ImageStream operatorImageStream = project.getOpenShiftAdmin().getImageStream(split[0]);
        if (Objects.nonNull(operatorImageStream)) {
            final String streamTag = split.length > 1 ? split[1] : "latest";
            operatorImageTag = operatorImageStream.getStatus().getDockerImageRepository() + ":" + streamTag;
        }

        return operatorImageTag;
    }

    /**
     * Allows to override the operator version to deploy.
     * @return null if overrides is disabled.
     */
    protected String overridesVersionTag() {
        return null;
    }

    public Map<String, String> getScenarioEnvironment() {
        Map<String, String> map = new HashMap<>();
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            Stream.of(server.getEnv()).forEach(e -> map.put(e.getName(), e.getValue()));
        }
        return map;
    }
}
