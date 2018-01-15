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

import java.io.IOException;
import java.util.UUID;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.kie.cloud.git.AbstractGitProvider;
import org.kie.cloud.git.constants.GitConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubGitProvider extends AbstractGitProvider {

    private static final Logger logger = LoggerFactory.getLogger(GitHubGitProvider.class);

    private GitHubClient client;

    @Override
    public String createGitRepositoryWithPrefix(String repositoryPrefixName, String repositoryPath) {
        String repositoryName = repositoryPrefixName + "-" + UUID.randomUUID().toString().substring(0, 4);

        try {
            RepositoryService service = new RepositoryService(client);

            Repository repository = new Repository();
            repository.setName(repositoryName);
            repository.setPrivate(false);
            repository = service.createRepository(repository);
            String httpUrl = repository.getSshUrl();

            pushToGitRepository(httpUrl, repositoryPath);
        } catch (Exception e) {
            logger.error("Error while preparing GitHub project " + repositoryName, e);
            throw new RuntimeException("Error while preparing GitHub project " + repositoryName, e);
        }

        return repositoryName;
    }

    @Override
    public void deleteGitRepository(String repositoryName) {
        try {
            client.delete("/repos/" + GitConstants.getGitHubUser() + "/" + repositoryName);
        } catch (IOException e) {
            logger.error("Error while deleting GitHub project " + repositoryName, e);
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
            logger.error("Error while retrieving GitHub project URL from " + repositoryName, e);
            throw new RuntimeException("Error while retrieving GitHub project URL from " + repositoryName, e);
        }
    }

    @Override
    public void init() {
        client = new GitHubClient();
        client.setCredentials(GitConstants.getGitHubUser(), GitConstants.getGitHubPassword());
    }
}
