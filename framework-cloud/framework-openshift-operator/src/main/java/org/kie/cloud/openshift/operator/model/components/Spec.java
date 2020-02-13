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
 * KieApp specification.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Spec {

    private String environment;
    private CommonConfig commonConfig;
    private Objects objects = new Objects();
    private Auth auth;
    private ImageRegistry imageRegistry;
    private Upgrades upgrades;
    private String version;
    private Boolean useImageTags;

    public CommonConfig getCommonConfig() {
        return commonConfig;
    }

    public void setCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Objects getObjects() {
        return objects;
    }

    public void setObjects(Objects objects) {
        this.objects = objects;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public ImageRegistry getImageRegistry() {
        return imageRegistry;
    }

    public void setImageRegistry(ImageRegistry imageRegistry) {
        this.imageRegistry = imageRegistry;
    }

    public Upgrades getUpgrades() {
        return upgrades;
    }

    public void setUpgrades(Upgrades upgrades) {
        this.upgrades = upgrades;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getUseImageTags() {
        return useImageTags;
    }

    public void setUseImageTags(Boolean useImageTags) {
        this.useImageTags = useImageTags;
    }
}
