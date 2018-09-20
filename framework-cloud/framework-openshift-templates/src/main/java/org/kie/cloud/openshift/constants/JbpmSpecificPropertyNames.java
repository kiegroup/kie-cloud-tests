/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.cloud.openshift.constants;

public class JbpmSpecificPropertyNames implements ProjectSpecificPropertyNames {

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
