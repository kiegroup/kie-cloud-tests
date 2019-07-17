/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.util.operator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used for deploying any operator using subscriptions to OpenShift project.
 */
public class OperatorDeployer {

    private static final Logger logger = LoggerFactory.getLogger(OperatorDeployer.class);

    public static void deploy(Project project, String operatorName, String updateChannel) {
        String catalogSourceName = project.getName() + "-" + operatorName;

        addClusterRoleToAdminUser(project);

        createCatalogSourceConfig(project, catalogSourceName, operatorName);
        createOperatorGroup(project, operatorName);
        createSubscription(project, catalogSourceName, operatorName, updateChannel);
    }

    public static void undeploy(Project project, String operatorName) {
        String catalogSourceName = project.getName() + "-" + operatorName;

        deleteCatalogSourceConfig(project, catalogSourceName);
    }

    private static void createCatalogSourceConfig(Project project, String catalogSourceName, String operatorName) {
        String catalogSourceConfig = "apiVersion: operators.coreos.com/v1\n" +
                "kind: CatalogSourceConfig\n" +
                "metadata:\n" +
                "  name: " + catalogSourceName + "\n" +
                "  namespace: openshift-marketplace\n" +
                "spec:\n" +
                "  targetNamespace: " + project.getName() + "\n" +
                "  packages: " + operatorName;
        executeYaml(project, catalogSourceConfig);
    }

    private static void deleteCatalogSourceConfig(Project project, String catalogSourceName) {
        String execute = project.runOcCommandAsAdmin("delete", "CatalogSourceConfig", catalogSourceName, "-n", "openshift-marketplace");
        logger.info(execute);
    }

    private static void createOperatorGroup(Project project, String operatorName) {
        String operatorGroup = "apiVersion: operators.coreos.com/v1alpha2\n" +
                "kind: OperatorGroup\n" +
                "metadata:\n" +
                "  name: " + operatorName + "\n" +
                "spec:\n" +
                "  targetNamespaces:\n" +
                "  - " + project.getName();
        executeYaml(project, operatorGroup);
    }

    private static void createSubscription(Project project, String catalogSourceName, String operatorName, String updateChannel) {
        String subscription = "apiVersion: operators.coreos.com/v1alpha1\n" +
                "kind: Subscription\n" +
                "metadata:\n" +
                "  name: " + operatorName + "\n" +
                "  namespace: " + project.getName() + "\n" +
                "spec:\n" +
                "  channel: " + updateChannel + "\n" +
                "  name: " + operatorName + "\n" +
                "  source: " + catalogSourceName + "\n" +
                "  sourceNamespace: " + project.getName();
        executeYaml(project, subscription);
    }

    private static void addClusterRoleToAdminUser(Project project) {
        String execute = project.runOcCommandAsAdmin("adm", "policy", "add-cluster-role-to-user", "cluster-admin", OpenShiftConstants.getOpenShiftAdminUserName());
        logger.info(execute);
    }

    private static void executeYaml(Project project, String yamlToBeExecuted) {
        Path subscriptionFile = storeStringAsYamlToTempFile(yamlToBeExecuted);
        String execute = project.runOcCommandAsAdmin("apply", "-f", subscriptionFile.toString());
        logger.info(execute);
    }

    private static Path storeStringAsYamlToTempFile(String stringToBeStored) {
        try {
            return Files.write(File.createTempFile("prometheus", ".yaml").toPath(), stringToBeStored.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error while storing object to temporary YAML file.", e);
        }
    }
}
