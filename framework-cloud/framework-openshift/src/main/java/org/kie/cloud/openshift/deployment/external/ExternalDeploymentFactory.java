package org.kie.cloud.openshift.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;

public interface ExternalDeploymentFactory {

    ExternalDeployment<? extends Deployment, ?> get(String extraDeploymentKey, Map<String, String> extraDeploymentConfig);
}
