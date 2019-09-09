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

            final String leaderPodName = HACEPTestsUtils.leaderPodName(project);
            Assertions.assertThat(leaderPodName).isIn(podNames);
        }
    }

    @Test
    public void testLeaderElectionFailOver() throws Exception {
        try (Project project = new ProjectImpl(deploymentScenario.getNamespace())) {
            final String oldLeaderName = HACEPTestsUtils.leaderPodName(project);
            final Pod oldLeader = HACEPTestsUtils.leaderPod(project);
            project.getOpenShift().deletePod(oldLeader);

            deploymentScenario.getDeployments().get(0).waitForScale();

            final String newLeaderName = HACEPTestsUtils.leaderPodName(project);
            Assertions.assertThat(newLeaderName).isNotEqualTo(oldLeaderName);
            final List<String> podNames = deploymentScenario
                    .getDeployments()
                    .get(0)
                    .getInstances()
                    .stream().map(instance -> instance.getName())
                    .collect(Collectors.toList());
            Assertions.assertThat(newLeaderName).isIn(podNames);
        }
    }
}
