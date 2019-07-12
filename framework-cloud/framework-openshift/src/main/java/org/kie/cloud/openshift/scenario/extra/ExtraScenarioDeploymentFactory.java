package org.kie.cloud.openshift.scenario.extra;

import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;

public interface ExtraScenarioDeploymentFactory {

    ExtraScenarioDeployment<? extends Deployment, ?> get(String extraDeploymentKey, Map<String, String> extraDeploymentConfig);
}
