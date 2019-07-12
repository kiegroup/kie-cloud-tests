package org.kie.cloud.openshift.operator.scenario.extra;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.scenario.extra.ExtraScenarioDeployment;

public interface ExtraScenarioDeploymentOperator<T extends Deployment> extends ExtraScenarioDeployment<T, KieApp> {

}
