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

package org.kie.cloud.maven.constants;

import org.kie.cloud.api.constants.Constants;

public class MavenConstants implements Constants {

    public static final String MAVEN_REPO_URL = "maven.repo.url";
    public static final String MAVEN_REPO_USERNAME = "maven.repo.username";
    public static final String MAVEN_REPO_PASSWORD = "maven.repo.password";

    public static String getMavenRepoUrl() {
        return System.getProperty(MAVEN_REPO_URL);
    }

    public static String getMavenRepoUser() {
        return System.getProperty(MAVEN_REPO_USERNAME);
    }

    public static String getMavenRepoPassword() {
        return System.getProperty(MAVEN_REPO_PASSWORD);
    }
}
