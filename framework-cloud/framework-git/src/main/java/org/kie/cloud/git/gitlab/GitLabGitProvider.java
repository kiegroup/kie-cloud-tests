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

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabSession;
import org.kie.cloud.git.GitProperties;
import org.kie.cloud.git.GitProvider;

public class GitLabGitProvider implements GitProvider {

    private static final String GITLAB_URL = System.getProperty(GitProperties.GITLAB_URL);
    private static final String GITLAB_USER = System.getProperty(GitProperties.GITLAB_USER);
    private static final String GITLAB_PASSWORD = System.getProperty(GitProperties.GITLAB_PASSWORD);

    private GitlabAPI gitLabApi;

    @Override
    public void createGitRepository(String repositoryName, String repositoryPath) {
        try {
            GitlabProject gitLabProject = gitLabApi.createUserProject(gitLabApi.getUser().getId(), repositoryName, null, null, null, null, null, null, null, true, null, null);
            String httpUrl = gitLabProject.getHttpUrl();

            Git git = Git.init().setDirectory(new File(repositoryPath)).call();
            RemoteAddCommand remoteAdd = git.remoteAdd();
            remoteAdd.setName("origin");
            remoteAdd.setUri(new URIish(httpUrl));
            remoteAdd.call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();

            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(GITLAB_USER, GITLAB_PASSWORD);
            git.push().setCredentialsProvider(credentialsProvider).setRemote("origin").setPushAll().call();
        } catch (Exception e) {
            throw new RuntimeException("Error while preparing GitLab project" + repositoryName, e);
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
            throw new RuntimeException("Error while retrieving GitLab projects from " + repositoryName, e);
        }
        throw new RuntimeException("URL of repository " + repositoryName + " not found");
    }

    @Override
    public void init() {
        try {
            GitlabSession session = GitlabAPI.connect(GITLAB_URL, GITLAB_USER, GITLAB_PASSWORD);
            String privateToken = session.getPrivateToken();
            gitLabApi = GitlabAPI.connect(GITLAB_URL, privateToken);
        } catch (IOException e) {
            throw new RuntimeException("Error while initializing GitLab.", e);
        }
    }
}
