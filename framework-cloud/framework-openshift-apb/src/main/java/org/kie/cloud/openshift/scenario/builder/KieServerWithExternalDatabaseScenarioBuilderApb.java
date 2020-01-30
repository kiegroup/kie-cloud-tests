/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.scenario.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.openshift.constants.ApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.KieServerWithExternalDatabaseScenarioApb;

public class KieServerWithExternalDatabaseScenarioBuilderApb extends AbstractOpenshiftScenarioBuilderApb<KieServerWithExternalDatabaseScenario> implements KieServerWithExternalDatabaseScenarioBuilder {

    private final Map<String, String> extraVars = new HashMap<>();

    public KieServerWithExternalDatabaseScenarioBuilderApb() {
        // Required values to create persitence values.
        extraVars.put(OpenShiftApbConstants.APB_PLAN_ID, ApbConstants.Plans.IMMUTABLE_KIE);
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_DB_TYPE, ApbConstants.DbType.EXTERNAL);
        extraVars.put(OpenShiftApbConstants.APB_IMAGE_STREAM_TAG, OpenShiftConstants.getApbKieImageStreamTag());
        // Just for now set cert properties here.
        extraVars.put(OpenShiftApbConstants.KIESERVER_SECRET_NAME, DeploymentConstants.getCustomTrustedSecretName());
        extraVars.put(OpenShiftApbConstants.KIESERVER_KEYSTORE_ALIAS, DeploymentConstants.getCustomTrustedKeystoreAlias());
        extraVars.put(OpenShiftApbConstants.KIESERVER_KEYSTORE_PWD, DeploymentConstants.getCustomTrustedKeystorePwd());
        //apb_kieserver_image_stream_name -- can be also required, has default value (now rhpam72-kieserver-openshift)

        // Users
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_USER, DeploymentConstants.getAppUser());
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_PWD, DeploymentConstants.getAppPassword());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_USER, DeploymentConstants.getAppUser());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_PWD, DeploymentConstants.getAppPassword());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_USER, DeploymentConstants.getAppUser());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_PWD, DeploymentConstants.getAppPassword());

        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
    }

    @Override
    public KieServerWithExternalDatabaseScenario getDeploymentScenarioInstance() {
        return new KieServerWithExternalDatabaseScenarioApb(extraVars);
    }

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder withInternalMavenRepo(boolean waitForRunning) {
        setExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY, waitForRunning);
        return this;
    }

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder withKieServerId(String kieServerId) {
        throw new UnsupportedOperationException("Not supported for APB.");
        // has default not configureable value 
    }

}
