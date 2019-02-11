package org.kie.cloud.openshift.deployment;

import java.net.URL;
import java.util.regex.Pattern;

import org.kie.cloud.api.deployment.EmployeeRosteringDeployment;
import org.kie.cloud.openshift.resource.Project;

public class EmployeeRosteringDeploymentImpl extends OpenShiftDeployment implements EmployeeRosteringDeployment {

    private static final Pattern PATTERN = Pattern.compile(".*-optaweb-employee-rostering");
    private URL url;

    public EmployeeRosteringDeploymentImpl(final Project project) {
        super(project);
    }

    @Override
    public URL getUrl() {
        if (url == null) {
            url = getHttpRouteUrl(getServiceName()).orElseThrow(() -> new RuntimeException("No Docker URL is available."));
        }
        return url;
    }

    @Override
    public String getServiceName() {
        return ServiceUtil.getServiceName(getOpenShiftUtil(), PATTERN);
    }
}
