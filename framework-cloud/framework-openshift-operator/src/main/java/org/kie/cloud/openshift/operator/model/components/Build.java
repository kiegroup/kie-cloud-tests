/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.operator.model.components;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Kie server build configuration.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Build {

    private String artifactDir;
    private GitSource gitSource;
    private String kieServerContainerDeployment;
    private String mavenMirrorURL;
    private String extensionImageStreamTag;
    private String extensionImageStreamTagNamespace;
    private String extensionImageInstallDir;

    public String getArtifactDir() {
        return artifactDir;
    }

    public void setArtifactDir(String artifactDir) {
        this.artifactDir = artifactDir;
    }

    public GitSource getGitSource() {
        return gitSource;
    }

    public void setGitSource(GitSource gitSource) {
        this.gitSource = gitSource;
    }

    public String getKieServerContainerDeployment() {
        return kieServerContainerDeployment;
    }

    public void setKieServerContainerDeployment(String kieServerContainerDeployment) {
        this.kieServerContainerDeployment = kieServerContainerDeployment;
    }

    public String getMavenMirrorURL() {
        return mavenMirrorURL;
    }

    public void setMavenMirrorURL(String mavenMirrorURL) {
        this.mavenMirrorURL = mavenMirrorURL;
    }

    public String getExtensionImageStreamTag() {
        return extensionImageStreamTag;
    }

    public void setExtensionImageStreamTag(String extensionImageStreamTag) {
        this.extensionImageStreamTag = extensionImageStreamTag;
    }

    public String getExtensionImageStreamTagNamespace() {
        return extensionImageStreamTagNamespace;
    }

    public void setExtensionImageStreamTagNamespace(String extensionImageStreamTagNamespace) {
        this.extensionImageStreamTagNamespace = extensionImageStreamTagNamespace;
    }

    public String getExtensionImageInstallDir() {
        return extensionImageInstallDir;
    }

    public void setExtensionImageInstallDir(String extensionImageInstallDir) {
        this.extensionImageInstallDir = extensionImageInstallDir;
    }
}
