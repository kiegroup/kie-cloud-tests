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

import java.io.IOException;

import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabSession;
import org.kie.cloud.git.AbstractGitProvider;
import org.kie.cloud.git.constants.GitConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitLabGitProvider extends AbstractGitProvider {

    private static final Logger logger = LoggerFactory.getLogger(GitLabGitProvider.class);

    private GitlabAPI gitLabApi;

    @Override
    public void createGitRepository(String repositoryName, String repositoryPath) {
        try {
            GitlabProject gitLabProject = gitLabApi.createUserProject(gitLabApi.getUser().getId(), repositoryName, null, null, null, null, null, null, null, true, null, null);
            String httpUrl = gitLabProject.getHttpUrl();

            pushToGitRepository(httpUrl, repositoryPath);
        } catch (Exception e) {
            logger.error("Error while preparing GitLab project " + repositoryName, e);
            throw new RuntimeException("Error while preparing GitLab project " + repositoryName, e);
        }
    }

    @Override
    public void deleteGitRepository(String repositoryName) {
        try {
            for (GitlabProject project : gitLabApi.getProjects()) {
                if (project.getName().equals(repositoryName)) {
                    gitLabApi.deleteProject(project.getId());
                }
            }
        } catch (IOException e) {
            logger.error("Error while deleting GitLab project " + repositoryName, e);
            throw new RuntimeException("Error while deleting GitLab project " + repositoryName, e);
        }
    }

    @Override
    public String getRepositoryUrl(String repositoryName) {
        try {
            for (GitlabProject project : gitLabApi.getProjects()) {
                if (project.getName().equals(repositoryName)) {
                    return project.getHttpUrl();
                }
            }
        } catch (IOException e) {
            logger.error("Error while retrieving GitLab projects from " + repositoryName, e);
            throw new RuntimeException("Error while retrieving GitLab projects from " + repositoryName, e);
        }
        throw new RuntimeException("URL of repository " + repositoryName + " not found");
    }

    @Override
    public void init() {
        try {
            GitlabSession session = GitlabAPI.connect(GitConstants.getGitLabUrl(), GitConstants.getGitLabUser(), GitConstants.getGitLabPassword());
            String privateToken = session.getPrivateToken();
            gitLabApi = GitlabAPI.connect(GitConstants.getGitLabUrl(), privateToken);
        } catch (IOException e) {
            logger.error("Error while initializing GitLab.", e);
            throw new RuntimeException("Error while initializing GitLab.", e);
        }
    }
}
