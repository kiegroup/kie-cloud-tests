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

package org.kie.cloud.openshift.database.driver;

import java.io.File;
import java.net.URL;

import org.kie.cloud.openshift.constants.OpenShiftConstants;

public abstract class AbstractExternalDriver implements ExternalDriver {

    private static final String ARTIFACT_MVN_REPO_RELATIVE_PATH = "drivers";

    @Override
    public String getDockerTag() {
        return getImageName() + ":" + getImageVersion();
    }

    @Override
    public String getDockerTag(URL dockerUrl) {
        return dockerUrl.getHost() + ":" + dockerUrl.getPort() + "/kie-server/" + getDockerTag();
    }

    @Override
    public String getDockerImageBuildCommand(File kieJdbcDriverScriptsFolder, URL dockerUrl) {
        if (!kieJdbcDriverScriptsFolder.exists()) {
            throw new RuntimeException("JDBC driver script folder " + kieJdbcDriverScriptsFolder.getAbsolutePath() + " doesn't exist.");
        }

        File kieJdbcDriverDockerDir = new File(kieJdbcDriverScriptsFolder, getDockerFileRelativePath());
        File kieJdbcDriverDockerFile = new File(kieJdbcDriverDockerDir, "Dockerfile");
        if (!kieJdbcDriverDockerFile.exists()) {
            throw new RuntimeException("JDBC driver Dockerfile " + kieJdbcDriverDockerFile.getAbsolutePath() + " doesn't exist.");
        }

        return "docker build -f " + kieJdbcDriverDockerFile.getAbsolutePath() + " --build-arg ARTIFACT_MVN_REPO=" + ARTIFACT_MVN_REPO_RELATIVE_PATH + " -t " + getDockerTag(dockerUrl) + " " + kieJdbcDriverScriptsFolder.getAbsolutePath();
    }

    @Override
    public File getDriverBinaryFileLocation() {
        File kieJdbcDriverScriptsFolder = OpenShiftConstants.getKieJdbcDriverScriptsFolder();
        return new File(kieJdbcDriverScriptsFolder, ARTIFACT_MVN_REPO_RELATIVE_PATH + "/" + getDriverBinaryFileLocationInArtifactRepo());
    }

    /**
     * Values returned by this method have to be same as binary file locations defined in driver scripts! 
     *
     * @return Path to the driver binary file in "artifact repo" placed within driver scripts folder.
     */
    protected abstract String getDriverBinaryFileLocationInArtifactRepo();
}
