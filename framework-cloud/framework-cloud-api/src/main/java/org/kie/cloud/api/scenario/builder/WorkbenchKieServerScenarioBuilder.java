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

package org.kie.cloud.api.scenario.builder;

import java.time.Duration;

import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;

/**
 * Cloud builder for Workbench and Kie Server in project. Built setup
 * for WorkbenchKieServerScenario
 * @see WorkbenchKieServerScenario
 */
public interface WorkbenchKieServerScenarioBuilder extends DeploymentScenarioBuilder<WorkbenchKieServerScenario> {

    /**
     * Return setup builder with additional configuration of external maven
     * repo.
     * @param repoUrl Maven repo URL.
     * @param repoUserName Maven repo user name.
     * @param repoPassword Maven repo user password.
     * @return Builder with configured external maven repo.
     */
    WorkbenchKieServerScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword);

    /**
     * @param kieServerId kie-server id
     * @return Builder with kie-server id environment variable set to given id
     */
    WorkbenchKieServerScenarioBuilder withKieServerId(String kieServerId);

    /**
     * @param url URL to be allowed to send requests to Kie server, can be set to "*" to allow all URLs.
     * @return Builder with Access-Control-Allow-Origin response header for Kie server set to given URL.
     */
    WorkbenchKieServerScenarioBuilder withAccessControlAllowOrigin(String url);

    /**
     * @param allowedMethods HTTP methods which are allowed by Kie server, for example "POST, GET".
     * @return Builder with Access-Control-Allow-Methods response header for Kie server set to given methods.
     */
    WorkbenchKieServerScenarioBuilder withAccessControlAllowMethods(String... allowedMethods);

    /**
     * @param allowedHeaders HTTP headers which are allowed by Kie server, for example "Accept, Authorization, Content-Type".
     * @return Builder with Access-Control-Allow-Headers response header for Kie server set to given headers.
     */
    WorkbenchKieServerScenarioBuilder withAccessControlAllowHeaders(String... allowedHeaders);

    /**
     * @param allowCredentials Configures whether the Kie server request can be made using credentials.
     * @return Builder with Access-Control-Allow-Credentials response header for Kie server.
     */
    WorkbenchKieServerScenarioBuilder withAccessControlAllowCredentials(boolean allowCredentials);

    /**
     * @param maxAge Configures how long the CORS response should be cached in browser.
     * @return Builder with Access-Control-Max-Age response header for Kie server.
     */
    WorkbenchKieServerScenarioBuilder withAccessControlMaxAge(Duration maxAge);

    /**
     * @return Builder with Prometheus monitoring configured monitoring deployed Kie servers.
     */
    WorkbenchKieServerScenarioBuilder withPrometheusMonitoring();
}
