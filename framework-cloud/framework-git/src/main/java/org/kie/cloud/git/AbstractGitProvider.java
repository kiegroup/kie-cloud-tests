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

package org.kie.cloud.git;

import java.io.File;
import java.util.UUID;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractGitProvider implements GitProvider {

    private static final Logger logger = LoggerFactory.getLogger(AbstractGitProvider.class);

    protected String generateRepositoryName(String repositoryPrefixName) {
        final String repositoryName = repositoryPrefixName + "-" + UUID.randomUUID().toString().substring(0, 4);
        logger.debug("Repository name {} was generated", repositoryName);

        return repositoryName;
    }

    protected void pushToGitRepository(String httpUrl, String repositoryPath,
            String username, String password) {
        try {
            Git git = Git.init().setDirectory(new File(repositoryPath)).call();

            RemoteAddCommand remoteAdd = git.remoteAdd();
            remoteAdd.setName("origin");
            remoteAdd.setUri(new URIish(httpUrl));
            remoteAdd.call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();

            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
            git.push().setCredentialsProvider(credentialsProvider).setRemote("origin").setPushAll().call();

            RemoteRemoveCommand remoteRemove = git.remoteRemove();
            remoteRemove.setName("origin");
            remoteRemove.call();
        } catch (Exception e) {
            logger.error("Error pushing to remote repository {}", httpUrl);
            throw new RuntimeException("Error pushing to remote repository" + httpUrl, e);
        }
    }
}
