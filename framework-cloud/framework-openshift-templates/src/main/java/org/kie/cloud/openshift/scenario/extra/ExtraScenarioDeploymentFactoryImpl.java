package org.kie.cloud.openshift.scenario.extra;

import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;

public class ExtraScenarioDeploymentFactoryImpl implements ExtraScenarioDeploymentFactory {

    public ExtraScenarioDeployment<? extends Deployment, ?> get(String extraDeploymentKey, Map<String, String> extraDeploymentConfig) {
        switch (extraDeploymentKey) {
            case MavenRepositoryExtraScenarioDeployment.ID:
                return new MavenRepositoryExtraScenarioDeploymentImpl(extraDeploymentConfig);
            default:
                throw new RuntimeException("Unknown extra scenario deployment: " + extraDeploymentKey);
        }
    }
}
