package org.kie.cloud.tests.common;

import org.kie.cloud.api.deployment.Deployment;

public class AutoScalerDeployment {

    public static void on(Deployment deployment, Runnable action) {
        int originalInstances = deployment.getReplicas();
        deployment.scale(0);
        deployment.waitForScale();
        
        action.run();
        
        deployment.scale(originalInstances);
        deployment.waitForScale();
    }

}
