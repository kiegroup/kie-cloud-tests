package org.kie.cloud.openshift.operator.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.constants.ImageEnvVariables;
import org.kie.cloud.openshift.deployment.external.impl.AbstractMavenRepositoryExternalDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.Server;

public class MavenRepositoryExternalDeploymentImpl extends AbstractMavenRepositoryExternalDeployment<KieApp> implements ExternalDeploymentOperator<MavenRepositoryDeployment> {

    public MavenRepositoryExternalDeploymentImpl(Map<String, String> config) {
        super(config);
    }

    @Override
    public void configure(KieApp kieApp) {
        super.configure(kieApp);

        MavenRepositoryDeployment deployment = getDeploymentInformation();
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_URL, deployment.getSnapshotsRepositoryUrl().toString()));
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_USERNAME, deployment.getUsername()));
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_PASSWORD, deployment.getPassword()));
        }
    }

}
