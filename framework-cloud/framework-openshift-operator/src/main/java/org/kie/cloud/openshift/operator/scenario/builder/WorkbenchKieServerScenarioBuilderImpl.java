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

package org.kie.cloud.openshift.operator.scenario.builder;

import java.time.Duration;

import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.openshift.operator.scenario.WorkbenchKieServerScenarioImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchKieServerScenarioBuilderImpl implements WorkbenchKieServerScenarioBuilder {

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerScenarioImpl.class);

    public WorkbenchKieServerScenarioBuilderImpl() {
    }

    @Override
    public WorkbenchKieServerScenario build() {
        return new WorkbenchKieServerScenarioImpl();
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        logger.error("Method 'withExternalMavenRepo' is currently not implemented, returning builder without changes.");
        // TODO: currently skipped to make tests running somehow, needs to be implemented
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withKieServerId(String kieServerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowCredentials(boolean allowCredentials) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowHeaders(String... allowedHeaders) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowMethods(String... allowedMethods) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowOrigin(String url) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlMaxAge(Duration maxAge) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
