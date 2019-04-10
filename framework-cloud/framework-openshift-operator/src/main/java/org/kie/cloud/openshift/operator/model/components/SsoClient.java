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
 * SSO client configuration.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SsoClient {

    private String name;
    private String secret;
    private String hostnameHTTP;
    private String hostnameHTTPS;

    public String getHostnameHTTP() {
        return hostnameHTTP;
    }

    public void setHostnameHTTP(String hostnameHTTP) {
        this.hostnameHTTP = hostnameHTTP;
    }

    public String getHostnameHTTPS() {
        return hostnameHTTPS;
    }

    public void setHostnameHTTPS(String hostnameHTTPS) {
        this.hostnameHTTPS = hostnameHTTPS;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
