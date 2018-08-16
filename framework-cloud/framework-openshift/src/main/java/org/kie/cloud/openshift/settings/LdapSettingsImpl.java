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
import org.kie.cloud.api.settings.LdapSettings;
import static org.kie.cloud.openshift.constants.OpenShiftTemplateConstants.*;

public class LdapSettingsImpl implements LdapSettings {

    private String url;
    private String bindDn;
    private String bindCredential;
    private String jaasSecurityDomain;
    private String baseCtxDn;
    private String baseFilter;
    private String searchScope;
    private Long limit;
    private String distinguishedNameAttribute;
    private Boolean parseUsername;
    private String usernameBegin;
    private String usernameEnd;
    private String roleAttributeId;
    private String rolesCtxDn;
    private String roleFilter;
    private Long roleRecutsion;
    private String defaultRole;
    private String roleNameAttributeId;
    private Boolean parseRoleNameFromDn;
    private Boolean roleAttributeIsDn;
    private String referralUserAttributeIdToCheck;

    @Override
    public Map<String, String> getEnvVariables() {
        Map<String, String> envVariables = new HashMap<>();

        envVariables.put(AUTH_LDAP_URL, getLdapUrl());
        envVariables.put(AUTH_LDAP_BIND_DN, getLdapBindDn());
        envVariables.put(AUTH_LDAP_BIND_CREDENTIAL, getLdapBindCredential());
        envVariables.put(AUTH_LDAP_JAAS_SECURITY_DOMAIN, getLdapJaasSecurityDomain());
        envVariables.put(AUTH_LDAP_BASE_CTX_DN, getLdapBaseCtxDn());
        envVariables.put(AUTH_LDAP_BASE_FILTER, getLdapBaseFilter());
        envVariables.put(AUTH_LDAP_SEARCH_SCOPE, getLdapSearchScope());
        envVariables.put(AUTH_LDAP_SEARCH_TIME_LIMIT, getLdapSearchTimeLimit());
        envVariables.put(AUTH_LDAP_DISTINGUISHED_NAME_ATTRIBUTE, getLdapDistinguishedNameAttribute());
        envVariables.put(AUTH_LDAP_PARSE_USERNAME, getLdapParseUsername());
        envVariables.put(AUTH_LDAP_USERNAME_BEGIN_STRING, getLdapUsernameBeginString());
        envVariables.put(AUTH_LDAP_USERNAME_END_STRING, getLdapUsernameEndString());
        envVariables.put(AUTH_LDAP_ROLE_ATTRIBUTE_ID, getLdapRoleAttributeId());
        envVariables.put(AUTH_LDAP_ROLES_CTX_DN, getLdapRolesCtxDn());
        envVariables.put(AUTH_LDAP_ROLE_FILTER, getLdapRoleFilter());
        envVariables.put(AUTH_LDAP_ROLE_RECURSION, getLdapRoleRecursion());
        envVariables.put(AUTH_LDAP_DEFAULT_ROLE, getLdapDefaultRole());
        envVariables.put(AUTH_LDAP_ROLE_NAME_ATTRIBUTE_ID, getLdapRoleNameAttributeId());
        envVariables.put(AUTH_LDAP_PARSE_ROLE_NAME_FROM_DN, getLdapParseRoleNameFromDn());
        envVariables.put(AUTH_LDAP_ROLE_ATTRIBUTE_IS_DN, getLdapRoleAttributeId());
        envVariables.put(AUTH_LDAP_REFERRAL_USER_ATTRIBUTE_ID_TO_CHECK, getLdapReferralUserAttributeIdToCheck());

        return envVariables.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }

    @Override
    public String getLdapUrl() {
        return url;
    }

    @Override
    public String getLdapBindDn() {
        return bindDn;
    }

    @Override
    public String getLdapBindCredential() {
        return bindCredential;
    }

    @Override
    public String getLdapJaasSecurityDomain() {
        return jaasSecurityDomain;
    }

    @Override
    public String getLdapBaseCtxDn() {
        return baseCtxDn;
    }

    @Override
    public String getLdapBaseFilter() {
        return baseFilter;
    }

    @Override
    public String getLdapSearchScope() {
        return searchScope;
    }

    @Override
    public String getLdapSearchTimeLimit() {
        return limit == null ? "" : limit.toString();
    }

    @Override
    public String getLdapDistinguishedNameAttribute() {
        return distinguishedNameAttribute;
    }

    @Override
    public String getLdapParseUsername() {
        return parseUsername == null ? "" : parseUsername.toString();
    }

    @Override
    public String getLdapUsernameBeginString() {
        return usernameBegin;
    }

    @Override
    public String getLdapUsernameEndString() {
        return usernameEnd;
    }

    @Override
    public String getLdapRoleAttributeId() {
        return roleAttributeId;
    }

    @Override
    public String getLdapRolesCtxDn() {
        return rolesCtxDn;
    }

    @Override
    public String getLdapRoleFilter() {
        return roleFilter;
    }

    @Override
    public String getLdapRoleRecursion() {
        return roleRecutsion == null ? "" : roleRecutsion.toString();
    }

    @Override
    public String getLdapDefaultRole() {
        return defaultRole;
    }

    @Override
    public String getLdapRoleNameAttributeId() {
        return roleNameAttributeId;
    }

    @Override
    public String getLdapParseRoleNameFromDn() {
        return parseRoleNameFromDn == null ? "" : parseRoleNameFromDn.toString();
    }

    @Override
    public String getLdapRoleAttributeIsDn() {
        return roleAttributeIsDn == null ? "" : roleAttributeIsDn.toString();
    }

    @Override
    public String getLdapReferralUserAttributeIdToCheck() {
        return referralUserAttributeIdToCheck;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    public void setBindCredential(String bindCredential) {
        this.bindCredential = bindCredential;
    }

    public void setJaasSecurityDomain(String jaasSecurityDomain) {
        this.jaasSecurityDomain = jaasSecurityDomain;
    }

    public void setBaseCtxDn(String baseCtxDn) {
        this.baseCtxDn = baseCtxDn;
    }

    public void setBaseFilter(String baseFilter) {
        this.baseFilter = baseFilter;
    }

    public void setSearchScope(String searchScope) {
        this.searchScope = searchScope;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public void setDistinguishedNameAttribute(String distinguishedNameAttribute) {
        this.distinguishedNameAttribute = distinguishedNameAttribute;
    }

    public void setParseUsername(Boolean parseUsername) {
        this.parseUsername = parseUsername;
    }

    public void setUsernameBegin(String usernameBegin) {
        this.usernameBegin = usernameBegin;
    }

    public void setUsernameEnd(String usernameEnd) {
        this.usernameEnd = usernameEnd;
    }

    public void setRoleAttributeId(String roleAttributeId) {
        this.roleAttributeId = roleAttributeId;
    }

    public void setRolesCtxDn(String rolesCtxDn) {
        this.rolesCtxDn = rolesCtxDn;
    }

    public void setRoleFilter(String roleFilter) {
        this.roleFilter = roleFilter;
    }

    public void setRoleRecutsion(Long roleRecutsion) {
        this.roleRecutsion = roleRecutsion;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    public void setRoleNameAttributeId(String roleNameAttributeId) {
        this.roleNameAttributeId = roleNameAttributeId;
    }

    public void setParseRoleNameFromDn(Boolean parseRoleNameFromDn) {
        this.parseRoleNameFromDn = parseRoleNameFromDn;
    }

    public void setRoleAttributeIsDn(Boolean roleAttributeIsDn) {
        this.roleAttributeIsDn = roleAttributeIsDn;
    }

    public void setReferralUserAttributeIdToCheck(String referralUserAttributeIdToCheck) {
        this.referralUserAttributeIdToCheck = referralUserAttributeIdToCheck;
    }

}
