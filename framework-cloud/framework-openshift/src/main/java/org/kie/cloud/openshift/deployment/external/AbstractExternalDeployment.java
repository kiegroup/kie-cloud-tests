package org.kie.cloud.openshift.deployment.external;

import java.util.Map;
import java.util.Objects;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.openshift.resource.Project;

public abstract class AbstractExternalDeployment<T extends Deployment, U> implements ExternalDeployment<T, U> {

    protected Map<String, String> deploymentConfig;

    private T deployment;

    public AbstractExternalDeployment(Map<String, String> deploymentConfig) {
        super();
        this.deploymentConfig = deploymentConfig;
    }

    @Override
    public T deploy(Project project) {
        this.deployment = deployToProject(project);
        return this.deployment;
    }

    protected abstract T deployToProject(Project project);

    protected T getDeploymentInformation() {
        if (Objects.isNull(this.deployment)) {
            throw new RuntimeException("Trying to access deployment informaiton whereas the deployment has not been done ...");
        }
        return this.deployment;
    }

}
