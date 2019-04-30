/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.cloud.openshift.util.sso;


import java.util.Collections;
import java.util.List;

import org.kie.cloud.openshift.util.sso.entity.User;

public interface SsoApi {

    public default String createUser(User user) {
        return createUser(user.username, user.password, user.firstName, user.lastName, user.email, null);
    }

    public default String createUser(User user, List<String> rolenames) {
        return createUser(user.username, user.password, user.firstName, user.lastName, user.email, rolenames);
    }

    public default String createUser(String username, String password) {
        return createUser(username, password, null, null, null, null);
    }

    public default String createUser(String username, String password, List<String> rolenames) {
        return createUser(username, password, null, null, null, rolenames);
    }

    public default String createUser(String username, String password, String firstname, String lastname, String email) {
        return createUser(username, password, firstname, lastname, email, null);
    }

    public String createUser(String username, String password, String firstname, String lastname, String email, List<String> rolenames);

    public void createRole(String rolename);

    public String createOidcBearerClient(String clientName);

    public default String createOicdConfidentialClient(String clientName, String rootUrl, String redirectUri, String baseUrl, String adminUrl) {
        return createOicdConfidentialClient(clientName, rootUrl, Collections.singletonList(redirectUri), baseUrl, adminUrl);
    }

    public String createOicdConfidentialClient(String clientName, String rootUrl, List<String> redirectUris, String baseUrl, String adminUrl);

    public default String createInsecureSamlClient(String clientName, String masterSamlUrl, String baseUrl, String redirectUri) {
        return createInsecureSamlClient(clientName, masterSamlUrl, baseUrl, Collections.singletonList(redirectUri));
    }

    public String createInsecureSamlClient(String clientName, String masterSamlUrl, String baseUrl, List<String> redirectUris);

    public default String createOidcPublicClient(String clientName, String rootUrl, String redirectUri, String webOrigin) {
        return createOidcPublicClient(clientName, rootUrl, Collections.singletonList(redirectUri), Collections.singletonList(webOrigin));
    }

    public String createOidcPublicClient(String clientName, String rootUrl, List<String> redirectUris, List<String> webOrigins);

    public default void addRealmRoleToUser(String userId, String rolename) {
        addRealmRolesToUser(userId, Collections.singletonList(rolename));
    }

    public void addRealmRolesToUser(String userId, List<String> rolenames);

    public void addBultinMappersToSamlClient(String clientId);

    public String getUserId(String username);

    public String getClientId(String clientName);

    public String getRealmId();

    public String getRealmPublicKey();

    public String getOicdInstallationXmlFile(String clientId);

    public String getSamlInstallationXmlFile(String clientId);

    public String getJsonInstallationFile(String clientId);

    public void updateUserDetails(User user);

    public void deleteUser(String userId);

    public void forceNameIdFormat(String clientId);

    public void updateClientRedirectUri(String clientId, List<String> redirectUris);

}