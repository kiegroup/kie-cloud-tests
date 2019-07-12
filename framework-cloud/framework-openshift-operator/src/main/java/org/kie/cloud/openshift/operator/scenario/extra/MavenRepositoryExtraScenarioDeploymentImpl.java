package org.kie.cloud.openshift.operator.scenario.extra;

import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.scenario.extra.impl.AbstractMavenRepositoryExtraScenarioDeployment;

public class MavenRepositoryExtraScenarioDeploymentImpl extends AbstractMavenRepositoryExtraScenarioDeployment<KieApp> implements ExtraScenarioDeploymentOperator<MavenRepositoryDeployment> {

    public MavenRepositoryExtraScenarioDeploymentImpl(Map<String, String> config) {
        super(config);
    }

    @Override
    public void configure(KieApp kieApp) {
        // TODO Auto-generated method stub
    }

}
