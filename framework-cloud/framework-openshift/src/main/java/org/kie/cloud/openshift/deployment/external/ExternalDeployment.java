package org.kie.cloud.openshift.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.openshift.resource.Project;

/**
 * Definition of a deployment which should be launched parallel or not to scenario
 */
public interface ExternalDeployment<T extends Deployment, U> {

    /** 
     * @return Key used to identity the extra deployment
     */
    String getKey();

    /**
     * Launch deployment into the given project
     * 
     * @return Deployment entity
     */
    T deploy(Project project);

    /**
     * Retrieve the deployment variables
     */
    Map<String, String> getDeploymentVariables();

    /**
     * Configure the given object with deployment variables
     * 
     * @param object This object should be specific for deployment process (templates, operator, apb ...)
     */
    void configure(U object);
}
