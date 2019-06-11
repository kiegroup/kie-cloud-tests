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

package org.kie.cloud.hacep;

import java.util.List;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Pod;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.HACepScenario;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.impl.ProjectImpl;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;

public class HACEPLeaderElectionIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<HACepScenario> {

    private static final String LEADERS_CONFIG_MAP = "default-leaders";
    private static final String LEADER_POD_KEY = "leader.pod.null";

    @Override
    protected HACepScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getHACepScenarioBuilder().build();
    }

    @Test
    public void testStartupLeaderElection() throws Exception {
        try (Project project = new ProjectImpl(deploymentScenario.getNamespace())) {
            final List<String> podNames = deploymentScenario
                    .getDeployments()
                    .get(0)
                    .getInstances()
                    .stream().map(instance -> instance.getName())
                    .collect(Collectors.toList());

            final ConfigMap leadersConfigMap = project.getOpenShift().configMaps().withName(LEADERS_CONFIG_MAP).get();
            Assertions.assertThat(leadersConfigMap.getData()).containsKey(LEADER_POD_KEY);
            Assertions.assertThat(leadersConfigMap.getData().get(LEADER_POD_KEY)).isIn(podNames);
        }
    }

    @Test
    public void testLeaderElectionFailOver() throws Exception {
        try (Project project = new ProjectImpl(deploymentScenario.getNamespace())) {
            ConfigMap leadersConfigMap = project.getOpenShift().configMaps().withName(LEADERS_CONFIG_MAP).get();
            Assertions.assertThat(leadersConfigMap.getData()).containsKey(LEADER_POD_KEY);
            final String leaderToRemovePod = leadersConfigMap.getData().get(LEADER_POD_KEY);
            final Pod oldLeader = project.getOpenShift().getPod(leaderToRemovePod);
            Assertions.assertThat(oldLeader).isNotNull();
            project.getOpenShift().deletePod(oldLeader);

            deploymentScenario.getDeployments().get(0).waitForScale();

            leadersConfigMap = project.getOpenShift().configMaps().withName(LEADERS_CONFIG_MAP).get();
            Assertions.assertThat(leadersConfigMap.getData()).containsKey(LEADER_POD_KEY);
            Assertions.assertThat(leadersConfigMap.getData().get(LEADER_POD_KEY)).isNotEqualTo(leaderToRemovePod);
            final List<String> podNames = deploymentScenario
                    .getDeployments()
                    .get(0)
                    .getInstances()
                    .stream().map(instance -> instance.getName())
                    .collect(Collectors.toList());
            Assertions.assertThat(leadersConfigMap.getData().get(LEADER_POD_KEY)).isIn(podNames);
        }
    }
}
