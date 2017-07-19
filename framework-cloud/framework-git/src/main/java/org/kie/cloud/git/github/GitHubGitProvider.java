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

package org.kie.cloud.git.github;

import java.io.File;
import java.io.IOException;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kie.cloud.git.GitProvider;
import org.kie.cloud.git.constants.GitConstants;

public class GitHubGitProvider implements GitProvider {

    private GitHubClient client;

    @Override
    public void createGitRepository(String repositoryName, String repositoryPath) {
        try {
            RepositoryService service = new RepositoryService(client);

            Repository repository = new Repository();
            repository.setName(repositoryName);
            repository.setPrivate(false);
            repository = service.createRepository(repository);
            String httpUrl = repository.getSshUrl();

            Git git = Git.init().setDirectory(new File(repositoryPath)).call();
            RemoteAddCommand remoteAdd = git.remoteAdd();
            remoteAdd.setName("origin");
            remoteAdd.setUri(new URIish(httpUrl));
            remoteAdd.call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();

            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(GitConstants.getGitHubUser(), GitConstants.getGitHubPassword());
            git.push().setCredentialsProvider(credentialsProvider).setRemote("origin").setPushAll().call();
        } catch (Exception e) {
            throw new RuntimeException("Error while preparing GitHub project " + repositoryName, e);
        }
    }

    @Override
    public void deleteGitRepository(String repositoryName) {
        try {
            client.delete("/repos/" + GitConstants.getGitHubUser() + "/" + repositoryName);
        } catch (IOException e) {
            throw new RuntimeException("Error while deleting GitHub project " + repositoryName, e);
        }
    }

    @Override
    public String getRepositoryUrl(String repositoryName) {
        try {
            RepositoryService service = new RepositoryService(client);
            Repository repository = service.getRepository(GitConstants.getGitHubUser(), repositoryName);
            return repository.getSvnUrl();
        } catch (IOException e) {
            throw new RuntimeException("Error while retrieving GitHub project URL from " + repositoryName, e);
        }
    }

    @Override
    public void init() {
        client = new GitHubClient();
        client.setCredentials(GitConstants.getGitHubUser(), GitConstants.getGitHubPassword());
    }
}
