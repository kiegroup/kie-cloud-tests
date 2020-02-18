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

package org.kie.cloud.tests.common;

import org.kie.cloud.api.deployment.Deployment;

public class AutoScalerDeployment {

    /**
     * Scale down to zero the deployment, run the action and scale up the deployment back to the original replicas count.
     * @param deployment deployment to scale up and down
     * @param action to perform between the deployment is down
     */
    public static void on(Deployment deployment, Runnable action) {
        int originalInstances = deployment.getReplicas();
        deployment.scale(0);
        deployment.waitForScale();
        
        action.run();
        
        deployment.scale(originalInstances);
        deployment.waitForScale();
    }

}
