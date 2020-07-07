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

package org.kie.cloud.openshift.operator.deployment;

import java.util.Optional;

import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.RouterUtil;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.KieAppDoneable;
import org.kie.cloud.openshift.operator.model.KieAppList;
import org.kie.cloud.openshift.operator.model.components.SmartRouter;
import org.kie.cloud.openshift.resource.Project;

public class SmartRouterOperatorDeployment extends SmartRouterDeploymentImpl {

    private NonNamespaceOperation<KieApp, KieAppList, KieAppDoneable, Resource<KieApp, KieAppDoneable>> kieAppClient;

    public SmartRouterOperatorDeployment(Project project, NonNamespaceOperation<KieApp, KieAppList, KieAppDoneable, Resource<KieApp, KieAppDoneable>> kieAppClient) {
        super(project);
        this.kieAppClient = kieAppClient;
    }

    @Override
    public void scale(int instances) {
        if (isReady()) {
            KieApp kieApp = kieAppClient.withName(OpenShiftConstants.getKieApplicationName()).get();
            SmartRouter smartRouter = kieApp.getSpec().getObjects().getSmartRouter();
            smartRouter.setReplicas(instances);
            kieAppClient.createOrReplace(kieApp);
        }
    }

    @Override
    public void waitForScale() {
        KieApp kieApp = kieAppClient.withName(OpenShiftConstants.getKieApplicationName()).get();

        Integer replicas = Optional.ofNullable(kieApp.getStatus())
                                   .map(status -> status.getApplied())
                                   .map(applied -> applied.getObjects().getSmartRouter().getReplicas())
                                   .orElseGet(() -> getOpenShift().getDeploymentConfig(getServiceName()).getSpec().getReplicas());

        waitUntilAllPodsAreReadyAndRunning(replicas);
        if (replicas > 0) {
            getInsecureUrl().ifPresent(RouterUtil::waitForRouter);
            getSecureUrl().ifPresent(RouterUtil::waitForRouter);
        }
    }
}
