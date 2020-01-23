/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.api.scenario;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.kie.cloud.api.scenario.builder.HACepScenarioBuilder;

public interface HACepScenario extends DeploymentScenario<HACepScenario> {
    File getAMQStreamsDirectory();
    File getKafkaKeyStore();
    void setKjars(List<String> kjars);
    void setSpringDeploymentEnvironmentVariables(final Map<String, String> springDeploymentEnvironmentVariables);
    Properties getKafkaConnectionProperties();
}
