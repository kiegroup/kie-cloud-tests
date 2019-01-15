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

import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.openshift.constants.ApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.scenario.WorkbenchKieServerScenarioApb;

public class WorkbenchKieServerScenarioBuilderApb implements WorkbenchKieServerScenarioBuilder {

    private final Map<String, String> extraVars = new HashMap<>();

    public WorkbenchKieServerScenarioBuilderApb() {
        extraVars.put(OpenShiftApbConstants.APB_PLAN_ID, ApbConstants.Plans.TRIAL);
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_DB_TYPE, ApbConstants.DbType.H2);
        extraVars.put(OpenShiftApbConstants.APB_IMAGE_STREAM_TAG, "1.0");
    }

    @Override
    public WorkbenchKieServerScenario build() {
        return new WorkbenchKieServerScenarioApb(extraVars);
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_URL, repoUrl);
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_USER, repoUserName);
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_PWD, repoPassword);
        return this;
        //throw new UnsupportedOperationException("Not supported for APB.");
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
}
