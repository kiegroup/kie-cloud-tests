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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.openshift.constants.ApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.WorkbenchKieServerScenarioApb;

public class WorkbenchKieServerScenarioBuilderApb extends AbstractOpenshiftScenarioBuilderApb<WorkbenchKieServerScenario> implements WorkbenchKieServerScenarioBuilder {

    private final Map<String, String> extraVars = new HashMap<>();
    private boolean deployPrometheus = false;

    public WorkbenchKieServerScenarioBuilderApb() {
        extraVars.put(OpenShiftApbConstants.APB_PLAN_ID, ApbConstants.Plans.TRIAL);
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_DB_TYPE, ApbConstants.DbType.H2);
        extraVars.put(OpenShiftApbConstants.APB_IMAGE_STREAM_TAG, OpenShiftConstants.getApbKieImageStreamTag());

        extraVars.put(OpenShiftApbConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
    }

    @Override
    public WorkbenchKieServerScenario getDeploymentScenarioInstance() {
        return new WorkbenchKieServerScenarioApb(extraVars, deployPrometheus);

    }

    @Override
    public WorkbenchKieServerScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withKieServerId(String kieServerId) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowOrigin(String url) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowMethods(String... allowedMethods) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowHeaders(String... allowedHeaders) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowCredentials(boolean allowCredentials) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlMaxAge(Duration maxAge) {
        throw new UnsupportedOperationException("Not supported for APB.");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withPrometheusMonitoring() {
        deployPrometheus = true;
        extraVars.put(OpenShiftApbConstants.PROMETHEUS_SERVER_EXT_DISABLED, "false");
        return this;
    }
}
