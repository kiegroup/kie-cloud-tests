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

import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.operator.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl implements ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl.class);

    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl() {
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario build() {
        return new ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl();
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        logger.error("Method 'withExternalMavenRepo' is currently not implemented, returning builder without changes.");
        // TODO: currently skipped to make tests running somehow, needs to be implemented
        return this;
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withSmartRouterId(String smartRouterId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withTimerServiceDataStoreRefreshInterval(Duration timerServiceDataStoreRefreshInterval) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder deploySso() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withBusinessCentralMavenUser(String user, String password) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer1Hostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer2Hostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer1Hostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer2Hostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
