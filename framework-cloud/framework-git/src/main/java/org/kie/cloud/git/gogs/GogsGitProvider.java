/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.git.gogs;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.kie.cloud.git.AbstractGitProvider;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GogsGitProvider extends AbstractGitProvider {

    private final String url;
    private final String user;
    private final String password;

    private static final String URL_API_SUFFIX = "/api/v1/";
    private static final String URL_CREATE_REPOSITORY_SUFFIX = "user/repos";
    private static final String URL_REPOSITORY_SUFFIX = "repos/";
    private static final String URL_GIT_SUFFIX = ".git";

    private static final Logger logger = LoggerFactory.getLogger(GogsGitProvider.class);

    public GogsGitProvider(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override public String createGitRepositoryWithPrefix(String repositoryPrefixName, String repositoryPath) {
        final String repositoryName = generateRepositoryName(repositoryPrefixName);
        createRepository(repositoryName);
        pushToGitRepository(getRepositoryUrl(repositoryName), repositoryPath,
                user, password);

        return repositoryName;
    }

    @Override public synchronized void deleteGitRepository(String repositoryName) {
        try {
            final StatusLine statusLine = Request.Delete(deleteRepositoryUrl(repositoryName))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeaderValue())
                    .execute()
                    .returnResponse()
                    .getStatusLine();

            if (statusLine.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                logger.error("Bad status code '{}' while deleting Gogs project, error message '{}'", statusLine.getStatusCode(), statusLine.getReasonPhrase());
                throw new RuntimeException("Bad status code '" + statusLine.getStatusCode() + "' while deleting Gogs project, error message '" + statusLine.getReasonPhrase() + "'");
            }
        } catch (Exception e) {
            logger.error("Error while deleting Git repository {}", repositoryName);
            throw new RuntimeException("Error while deleting Git repository", e);
        }
    }

    @Override public String getRepositoryUrl(String repositoryName) {
        try {
            URL repositoryUrl = new URL(url);
            repositoryUrl = new URL(repositoryUrl, user + "/");
            repositoryUrl = new URL(repositoryUrl, repositoryName + URL_GIT_SUFFIX);

            return repositoryUrl.toString();
        } catch (MalformedURLException e) {
            logger.error("Unable to build repository url");
            throw new RuntimeException("Unable to build repository url", e);
        }
    }

    private synchronized void createRepository(String repositoryName) {
        try {
            final StatusLine statusLine = Request.Post(createRepositoryUrl())
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeaderValue())
                    .bodyString("{ \"name\" : \""+ repositoryName + "\" }", ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse()
                    .getStatusLine();

            if (statusLine.getStatusCode() != HttpStatus.SC_CREATED) {
                logger.error("Bad status code '{}' while preparing Gogs project, error message '{}'", statusLine.getStatusCode(), statusLine.getReasonPhrase());
                throw new RuntimeException("Bad status code '" + statusLine.getStatusCode() + "' while preparing Gogs project, error message '" + statusLine.getReasonPhrase() + "'");
            }
        } catch (Exception e) {
            logger.error("Error while creating Git repository {}", repositoryName);
            throw new RuntimeException("Error while creating Git repository", e);
        }
    }

    private String createRepositoryUrl() {
        try {
            URL requestUrl = new URL(url);
            requestUrl = new URL(requestUrl, URL_API_SUFFIX);
            requestUrl = new URL(requestUrl, URL_CREATE_REPOSITORY_SUFFIX);

            return requestUrl.toString();
        } catch (MalformedURLException e) {
            logger.error("Error building create repository url");
            throw new RuntimeException("Error building create repository url", e);
        }
    }

    private String deleteRepositoryUrl(String repositoryName) {
        try {
            URL requestUrl = new URL(url);
            requestUrl = new URL(requestUrl, URL_API_SUFFIX);
            requestUrl = new URL(requestUrl, URL_REPOSITORY_SUFFIX);
            requestUrl = new URL(requestUrl, user + "/");
            requestUrl = new URL(requestUrl, repositoryName);

            return requestUrl.toString();
        } catch (MalformedURLException e) {
            logger.error("Error building delete repository url");
            throw new RuntimeException("Error building delete repository url", e);
        }
    }

    private String authHeaderValue() {
        final String auth = user + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(
                auth.getBytes(StandardCharsets.UTF_8));
        final String authHeader = "Basic " + new String(encodedAuth);

        return authHeader;
    }
}
