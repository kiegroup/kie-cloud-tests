package org.kie.cloud.openshift.constants;

public class DroolsSpecificPropertyNames implements ProjectSpecificPropertyNames {

    @Override
    public String workbenchMavenUserName() {
        return OpenShiftTemplateConstants.DECISION_CENTRAL_MAVEN_USERNAME;
    }

    @Override
    public String workbenchMavenPassword() {
        return OpenShiftTemplateConstants.DECISION_CENTRAL_MAVEN_PASSWORD;
    }

    @Override
    public String workbenchHttpsSecret() {
        return OpenShiftTemplateConstants.DECISION_CENTRAL_HTTPS_SECRET;
    }

    @Override
    public String workbenchMavenService() {
        return OpenShiftTemplateConstants.DECISION_CENTRAL_MAVEN_SERVICE;
    }

    @Override
    public String workbenchHostnameHttp() {
        return OpenShiftTemplateConstants.DECISION_CENTRAL_HOSTNAME_HTTP;
    }

    @Override
    public String workbenchHostnameHttps() {
        return OpenShiftTemplateConstants.DECISION_CENTRAL_HOSTNAME_HTTPS;
    }

    @Override
    public String workbenchSsoClient() {
        return OpenShiftTemplateConstants.DECISION_CENTRAL_SSO_CLIENT;
    }

    @Override
    public String workbenchSsoSecret() {
        return OpenShiftTemplateConstants.DECISION_CENTRAL_SSO_SECRET;
    }
}
