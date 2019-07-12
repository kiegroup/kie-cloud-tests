package org.kie.cloud.openshift.scenario.extra;

import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.scenario.extra.impl.AbstractMavenRepositoryExtraScenarioDeployment;

public class MavenRepositoryExtraScenarioDeploymentImpl extends AbstractMavenRepositoryExtraScenarioDeployment<Map<String, String>> implements ExtraScenarioDeploymentTemplates<MavenRepositoryDeployment> {

    public MavenRepositoryExtraScenarioDeploymentImpl(Map<String, String> config) {
        super(config);
    }

    @Override
    public void configure(Map<String, String> object) {
        // TODO Auto-generated method stub
    }

}
