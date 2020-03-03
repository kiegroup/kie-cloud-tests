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
package org.kie.cloud.api.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class to configure the GIT instance to be used among the tests.
 *
 * Currently, there is only one way to create the GitSettings via properties (see method .fromProperties()).
 */
public class GitSettings {

    public static final String GIT_PROVIDER = "git.provider";

    private final String provider;
    private List<Consumer<String>> repositoryLoadedActions = new ArrayList<>();
    private String repositoryName;
    private String repositoryPath;

    public GitSettings(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public List<Consumer<String>> getRepositoryLoadedActions() {
        return repositoryLoadedActions;
    }

    public GitSettings withRepository(String repositoryName, String repositoryPath) {
        this.repositoryName = repositoryName;
        this.repositoryPath = repositoryPath;
        return this;
    }

    public void addOnRepositoryLoaded(Consumer<String> repositoryLoadedAction) {
        if (this.repositoryName == null) {
            throw new RuntimeException("Repository name not configured");
        }

        repositoryLoadedActions.add(repositoryLoadedAction);
    }

    public static final GitSettings fromProperties() {
        return new GitSettings(System.getProperty(GIT_PROVIDER));
    }

}
