package org.kie.cloud.openshift.operator.deployment.external;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;

public interface ExternalDeploymentOperator<T extends Deployment> extends ExternalDeployment<T, KieApp> {

}
