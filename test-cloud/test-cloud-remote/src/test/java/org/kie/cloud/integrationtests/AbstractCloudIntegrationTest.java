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

package org.kie.cloud.integrationtests;

import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.git.GitProvider;
import org.kie.cloud.git.GitProviderService;

public abstract class AbstractCloudIntegrationTest {

    protected static final String PROJECT_GROUP_ID = "org.kie.server.testing";
    protected static final String DEFINITION_PROJECT_NAME = "definition-project";
    protected static final String DEFINITION_PROJECT_VERSION = "1.0.0.Final";

    protected static final String DEFINITION_PROJECT_SNAPSHOT_NAME = "definition-project-snapshot";
    protected static final String DEFINITION_PROJECT_SNAPSHOT_VERSION = "1.0.0-SNAPSHOT";

    protected static final String CLOUD_BALANCE_PROJECT_SNAPSHOT_NAME = "cloudbalance-snapshot";
    protected static final String CLOUD_BALANCE_PROJECT_SNAPSHOT_VERSION = "1.0.0-SNAPSHOT";

    protected static final String RULE_PROJECT_NAME = "rule-project";
    protected static final String RULE_PROJECT_VERSION = "1.0.0-SNAPSHOT";

    protected static final String HELLO_RULES_PROJECT_NAME = "hello-rules-snapshot";
    protected static final String HELLO_RULES_PROJECT_VERSION = "1.0.0-SNAPSHOT";

    protected static final String CONTAINER_ID = "cont-id";
    protected static final String CONTAINER_ALIAS = "cont-alias";

    protected static final String WORKBENCH_LOGIN_SCREEN_TEXT = "Sign In";

    protected static final String SPACE_NAME = "mySpace";
    protected static final String SPACE_SECOND_NAME = "mySpaceTwo";

    protected static final String ORGANIZATIONAL_UNIT_REST_REQUEST = "rest/organizationalunits";
    protected static final String KIE_SERVER_INFO_REST_REQUEST_URL = "services/rest/server";
    protected static final String KIE_CONTAINERS_REQUEST_URL = "services/rest/server/containers";

    // Path relative to target/classes folder
    protected static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    protected static final DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

    protected static final GitProvider gitProvider = new GitProviderService().createGitProvider();
}
