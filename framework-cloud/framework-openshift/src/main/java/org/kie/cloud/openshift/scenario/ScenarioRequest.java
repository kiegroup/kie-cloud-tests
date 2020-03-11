/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.openshift.scenario;

import org.kie.cloud.api.settings.GitSettings;

public class ScenarioRequest {

    private boolean deploySso = false;
    private GitSettings gitSettings;
    private boolean deployPrometheus = false;

    public boolean isDeploySso() {
        return deploySso;
    }

    public GitSettings getGitSettings() {
        return gitSettings;
    }

    public boolean isDeployPrometheus() {
        return deployPrometheus;
    }

    public ScenarioRequest enableDeploySso() {
        this.deploySso = true;
        return this;
    }

    public ScenarioRequest setGitSettings(GitSettings gitSettings) {
        this.gitSettings = gitSettings;
        return this;
    }

    public ScenarioRequest enableDeployPrometheus() {
        this.deployPrometheus = true;
        return this;
    }

}
