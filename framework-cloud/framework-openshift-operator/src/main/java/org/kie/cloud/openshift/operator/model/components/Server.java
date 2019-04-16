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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * KieApp Kie server configuration.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Server {

    private String name;
    private List<Env> env = new ArrayList<>();
    private String keystoreSecret;
    private Integer deployments;
    private Integer replicas;
    private SsoClient ssoClient;
    private Resources resources;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addEnv(Env env) {
        this.env.add(env);
    }

    public void addEnvs(List<Env> envs) {
        this.env.addAll(envs);
    }

    public Env[] getEnv() {
        return env.toArray(new Env[0]);
    }

    public void setEnv(Env[] env) {
        this.env = Arrays.asList(env);
    }

    public String getKeystoreSecret() {
        return keystoreSecret;
    }

    public void setKeystoreSecret(String keystoreSecret) {
        this.keystoreSecret = keystoreSecret;
    }

    public Integer getDeployments() {
        return deployments;
    }

    public void setDeployments(Integer deployments) {
        this.deployments = deployments;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public SsoClient getSsoClient() {
        return ssoClient;
    }

    public void setSsoClient(SsoClient ssoClient) {
        this.ssoClient = ssoClient;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }
}
