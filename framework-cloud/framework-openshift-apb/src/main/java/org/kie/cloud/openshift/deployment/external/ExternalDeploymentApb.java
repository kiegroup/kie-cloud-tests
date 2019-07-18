package org.kie.cloud.openshift.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;

public interface ExternalDeploymentApb<T extends Deployment> extends ExternalDeployment<T, Map<String, String>> {

}
