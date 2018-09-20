package org.kie.cloud.openshift.constants;

public class JbpmSystemPropertyNames implements ProjectSpecificPropertyNames {

    @Override
    public String workbenchMavenUserName() {
        return OpenShiftTemplateConstants.BUSINESS_CENTRAL_MAVEN_USERNAME;
    }

    @Override
    public String workbenchMavenPassword() {
        return OpenShiftTemplateConstants.BUSINESS_CENTRAL_MAVEN_PASSWORD;
    }

    @Override
    public String workbenchHttpsSecret() {
        return OpenShiftTemplateConstants.BUSINESS_CENTRAL_HTTPS_SECRET;
    }

    @Override
    public String workbenchMavenService() {
        return OpenShiftTemplateConstants.BUSINESS_CENTRAL_MAVEN_SERVICE;
    }

    @Override
    public String workbenchHostnameHttp() {
        return OpenShiftTemplateConstants.BUSINESS_CENTRAL_HOSTNAME_HTTP;
    }

    @Override
    public String workbenchHostnameHttps() {
        return OpenShiftTemplateConstants.BUSINESS_CENTRAL_HOSTNAME_HTTPS;
    }

    @Override
    public String workbenchSsoClient() {
        return OpenShiftTemplateConstants.BUSINESS_CENTRAL_SSO_CLIENT;
    }

    @Override
    public String workbenchSsoSecret() {
        return OpenShiftTemplateConstants.BUSINESS_CENTRAL_SSO_SECRET;
    }
}
