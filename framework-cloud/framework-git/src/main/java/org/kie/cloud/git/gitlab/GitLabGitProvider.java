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

package org.kie.cloud.git.gitlab;

import org.kie.cloud.git.AbstractGitProvider;

import cz.xtf.git.GitLabUtil;
import cz.xtf.git.GitProject;

public class GitLabGitProvider extends AbstractGitProvider {

    private final GitLabUtil gitLabUtil;

    public GitLabGitProvider() {
        gitLabUtil = new GitLabUtil();
    }

    @Override
    public String createGitRepositoryWithPrefix(String repositoryPrefixName, String repositoryPath) {
        GitProject gitProject = gitLabUtil.createProjectFromPath(repositoryPrefixName, repositoryPath);
        return gitProject.getName();
    }

    @Override
    public void deleteGitRepository(String repositoryName) {
        gitLabUtil.deleteProject(repositoryName);
    }

    @Override
    public String getRepositoryUrl(String repositoryName) {
        return gitLabUtil.getProjectUrl(repositoryName);
    }
}
