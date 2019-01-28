package org.kie.cloud.api.deployment;

import java.net.URL;

/**
 * optaweb-employee-rostering application
 */
public interface EmployeeRosteringDeployment extends Deployment {

    /**
     * Get URL for Optaweb Employee Rostering service (deployment).
     *
     * @return Kie Server URL
     */
    URL getUrl();
}
