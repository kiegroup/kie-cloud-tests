/*
 * Copyright 2018 JBoss by Red Hat.
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
package org.kie.cloud.openshift.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.kie.cloud.openshift.constants.OpenShiftApbConstants;


public class LdapSettingsImpl extends AbstractLdapSettings {

    @Override
    public Map<String, String> getEnvVariables() {
        Map<String, String> envVariables = new HashMap<>();

        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_URL, getLdapUrl());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_BIND_DN, getLdapBindDn());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_BIND_CREDENTIAL, getLdapBindCredential());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_JAAS_SECURITY_DOMAIN, getLdapJaasSecurityDomain());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_BASE_CTX_DN, getLdapBaseCtxDn());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_BASE_FILTER, getLdapBaseFilter());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_SEARCH_SCOPE, getLdapSearchScope());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_SEARCH_TIME_LIMIT, getLdapSearchTimeLimit());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_DISTINGUISHED_NAME_ATTRIBUTE, getLdapDistinguishedNameAttribute());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_PARSE_USERNAME, getLdapParseUsername());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_USERNAME_BEGIN_STRING, getLdapUsernameBeginString());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_USERNAME_END_STRING, getLdapUsernameEndString());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_ROLE_ATTRIBUTE_ID, getLdapRoleAttributeId());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_ROLES_CTX_DN, getLdapRolesCtxDn());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_ROLE_FILTER, getLdapRoleFilter());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_ROLE_RECURSION, getLdapRoleRecursion());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_DEFAULT_ROLE, getLdapDefaultRole());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_ROLE_NAME_ATTRIBUTE_ID, getLdapRoleNameAttributeId());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_PARSE_ROLE_NAME_FROM_DN, getLdapParseRoleNameFromDn());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_ROLE_ATTRIBUTE_IS_DN, getLdapRoleAttributeId());
        envVariables.put(OpenShiftApbConstants.AUTH_LDAP_REFERRAL_USER_ATTRIBUTE_ID_TO_CHECK, getLdapReferralUserAttributeIdToCheck());

        return envVariables.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }
}
