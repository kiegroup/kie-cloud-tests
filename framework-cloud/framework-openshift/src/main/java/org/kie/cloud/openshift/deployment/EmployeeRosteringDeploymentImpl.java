package org.kie.cloud.openshift.deployment;

import java.net.URL;
import java.util.regex.Pattern;

import org.kie.cloud.api.deployment.EmployeeRosteringDeployment;
import org.kie.cloud.openshift.resource.Project;

public class EmployeeRosteringDeploymentImpl extends OpenShiftDeployment implements EmployeeRosteringDeployment {

    private static final Pattern PATTERN = Pattern.compile(".*-optaweb-employee-rostering");

    public EmployeeRosteringDeploymentImpl(final Project project) {
        super(project);
    }

    @Override
    public URL getUrl() {
        return getHttpRouteUrl(getServiceName());
    }

    @Override
    public String getServiceName() {
        return ServiceUtil.getServiceName(getOpenShiftUtil(), PATTERN);
    }
}
