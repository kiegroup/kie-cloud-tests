package org.kie.cloud.openshift.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;

public interface ExternalDeploymentTemplates<T extends Deployment> extends ExternalDeployment<T, Map<String, String>> {

}
