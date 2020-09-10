/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.operator.deployment;

import org.kie.cloud.openshift.deployment.ProcessMigrationDeploymentImpl;
import org.kie.cloud.openshift.deployment.RouterUtil;
import org.kie.cloud.openshift.resource.Project;

public class ProcessMigrationOperatorDeployment extends ProcessMigrationDeploymentImpl {

    public ProcessMigrationOperatorDeployment(Project project) {
        super(project);
    }

    @Override
    public void scale(int instances) {
        throw new RuntimeException("Operation scale is not supported for PIM Image");
    }

    @Override
    public void waitForScale() {
        Integer replicas = getOpenShift().getDeploymentConfig(getServiceName()).getSpec().getReplicas();

        waitUntilAllPodsAreReadyAndRunning(replicas);
        if (replicas > 0) {
            RouterUtil.waitForRouter(getUrl());
        }
    }
}