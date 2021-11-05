/*
 * Copyright 2021 JBoss by Red Hat.
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

package org.kie.cloud.integrationtests.testproviders;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.KieServerScenario;

import static org.assertj.core.api.Assertions.assertThat;

public class KieServerDependenciesTestProvider {

    private static final String DEPENDENCIES_PATH = "/opt/kie/dependencies/";
    private static final String KAFKA_DEPENDENCY_FOLDER = "jbpm-kafka";
    private static final String KAFKA_DEPENDENCY_NAME_PREFIX = "jbpm-event-emitters-kafka-";
    private static final String CLUSTER_DEPENDENCY_FOLDER = "jbpm-clustering";
    private static final String CLUSTER_DEPENDENCY_NAME_PREFIX = "kie-server-services-jbpm-cluster-";

    private KieServerDependenciesTestProvider() {}

    /**
     * Create provider instance
     *
     * @return provider instance
     */
    public static KieServerDependenciesTestProvider create() {
        return create(null);
    }

    /**
     * Create provider instance and init it with given environment
     *
     * @param environment if not null, initialize this provider with the environment
     *
     * @return provider instance
     */
    public static KieServerDependenciesTestProvider create(DeploymentScenario<?> deploymentScenario) {
        KieServerDependenciesTestProvider provider = new KieServerDependenciesTestProvider();
        if (Objects.nonNull(deploymentScenario)) {
            provider.init(deploymentScenario);
        }
        return provider;
    }

    private void init(DeploymentScenario<?> deploymentScenario) {
    }

    private String getFolderContent(KieServerScenario deploymentScenario, String path) {
        KieServerDeployment kieServerDeployment = deploymentScenario.getKieServerDeployments().get(0);
        List<String> instanceNames = kieServerDeployment
                .getInstances()
                .stream()
                .map(Instance::getName)
                .collect(Collectors.toList());
        OpenShiftBinary oc = OpenShifts.masterBinary(deploymentScenario.getNamespace());
        String[] args = {"rsh", instanceNames.get(0), "ls", path};
        String contents = oc.execute(args).trim(); 
        return contents;
    }

    public void testDependenciesFolderNotEmpty(KieServerScenario deploymentScenario) {
        assertThat(getFolderContent(deploymentScenario, DEPENDENCIES_PATH)).isNotEmpty();
    }

    public void testKafkaDependencyExists(KieServerScenario deploymentScenario) {
        assertThat(getFolderContent(deploymentScenario, DEPENDENCIES_PATH)).contains(KAFKA_DEPENDENCY_FOLDER);
        assertThat(getFolderContent(deploymentScenario, DEPENDENCIES_PATH + KAFKA_DEPENDENCY_FOLDER)).isNotEmpty();
    }

    public void testKafkaDependencyVersion(KieServerScenario deploymentScenario) {
        assertThat(getFolderContent(deploymentScenario, DEPENDENCIES_PATH + KAFKA_DEPENDENCY_FOLDER)).isEqualTo(KAFKA_DEPENDENCY_NAME_PREFIX + DeploymentConstants.getKieArtifactVersion() + ".jar");
    }

    public void testClusterDependencyExists(KieServerScenario deploymentScenario) {
        assertThat(getFolderContent(deploymentScenario, DEPENDENCIES_PATH)).contains(CLUSTER_DEPENDENCY_FOLDER);
        assertThat(getFolderContent(deploymentScenario, DEPENDENCIES_PATH + CLUSTER_DEPENDENCY_FOLDER)).isNotEmpty();
    }

    public void testClusterDependencyVersion(KieServerScenario deploymentScenario) {
        assertThat(getFolderContent(deploymentScenario, DEPENDENCIES_PATH + CLUSTER_DEPENDENCY_FOLDER)).isEqualTo(CLUSTER_DEPENDENCY_NAME_PREFIX + DeploymentConstants.getKieArtifactVersion() + ".jar");
    }

}
