package org.kie.cloud.openshift.scenario.extra;

import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;

public interface ExtraScenarioDeploymentTemplates<T extends Deployment> extends ExtraScenarioDeployment<T, Map<String, String>> {

}
