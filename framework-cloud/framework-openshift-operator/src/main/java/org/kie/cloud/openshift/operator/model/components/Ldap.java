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
 * LDAP configuration.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Ldap {

    private String url;
    private String bindDN;
    private String bindCredential;
    private String jaasSecurityDomain;
    private String baseCtxDN;
    private String baseFilter;
    private String searchScope;
    private Integer searchTimeLimit;
    private String distinguishedNameAttribute;
    private Boolean parseUsername;
    private String usernameBeginString;
    private String usernameEndString;
    private String roleAttributeID;
    private String rolesCtxDN;
    private String roleFilter;
    private Integer roleRecursion;
    private String defaultRole;
    private String roleNameAttributeID;
    private Boolean parseRoleNameFromDN;
    private Boolean roleAttributeIsDN;
    private String referralUserAttributeIDToCheck;

    public String getBaseCtxDN() {
        return baseCtxDN;
    }

    public void setBaseCtxDN(String baseCtxDN) {
        this.baseCtxDN = baseCtxDN;
    }

    public String getBaseFilter() {
        return baseFilter;
    }

    public void setBaseFilter(String baseFilter) {
        this.baseFilter = baseFilter;
    }

    public String getBindCredential() {
        return bindCredential;
    }

    public void setBindCredential(String bindCredential) {
        this.bindCredential = bindCredential;
    }

    public String getBindDN() {
        return bindDN;
    }

    public void setBindDN(String bindDN) {
        this.bindDN = bindDN;
    }

    public String getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    public String getDistinguishedNameAttribute() {
        return distinguishedNameAttribute;
    }

    public void setDistinguishedNameAttribute(String distinguishedNameAttribute) {
        this.distinguishedNameAttribute = distinguishedNameAttribute;
    }

    public String getJaasSecurityDomain() {
        return jaasSecurityDomain;
    }

    public void setJaasSecurityDomain(String jaasSecurityDomain) {
        this.jaasSecurityDomain = jaasSecurityDomain;
    }

    public Boolean getParseRoleNameFromDN() {
        return parseRoleNameFromDN;
    }

    public void setParseRoleNameFromDN(Boolean parseRoleNameFromDN) {
        this.parseRoleNameFromDN = parseRoleNameFromDN;
    }

    public Boolean getParseUsername() {
        return parseUsername;
    }

    public void setParseUsername(Boolean parseUsername) {
        this.parseUsername = parseUsername;
    }

    public String getReferralUserAttributeIDToCheck() {
        return referralUserAttributeIDToCheck;
    }

    public void setReferralUserAttributeIDToCheck(String referralUserAttributeIDToCheck) {
        this.referralUserAttributeIDToCheck = referralUserAttributeIDToCheck;
    }

    public String getRoleAttributeID() {
        return roleAttributeID;
    }

    public void setRoleAttributeID(String roleAttributeID) {
        this.roleAttributeID = roleAttributeID;
    }

    public Boolean getRoleAttributeIsDN() {
        return roleAttributeIsDN;
    }

    public void setRoleAttributeIsDN(Boolean roleAttributeIsDN) {
        this.roleAttributeIsDN = roleAttributeIsDN;
    }

    public String getRoleFilter() {
        return roleFilter;
    }

    public void setRoleFilter(String roleFilter) {
        this.roleFilter = roleFilter;
    }

    public String getRoleNameAttributeID() {
        return roleNameAttributeID;
    }

    public void setRoleNameAttributeID(String roleNameAttributeID) {
        this.roleNameAttributeID = roleNameAttributeID;
    }

    public Integer getRoleRecursion() {
        return roleRecursion;
    }

    public void setRoleRecursion(Integer roleRecursion) {
        this.roleRecursion = roleRecursion;
    }

    public String getRolesCtxDN() {
        return rolesCtxDN;
    }

    public void setRolesCtxDN(String rolesCtxDN) {
        this.rolesCtxDN = rolesCtxDN;
    }

    public String getSearchScope() {
        return searchScope;
    }

    public void setSearchScope(String searchScope) {
        this.searchScope = searchScope;
    }

    public Integer getSearchTimeLimit() {
        return searchTimeLimit;
    }

    public void setSearchTimeLimit(Integer searchTimeLimit) {
        this.searchTimeLimit = searchTimeLimit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsernameBeginString() {
        return usernameBeginString;
    }

    public void setUsernameBeginString(String usernameBeginString) {
        this.usernameBeginString = usernameBeginString;
    }

    public String getUsernameEndString() {
        return usernameEndString;
    }

    public void setUsernameEndString(String usernameEndString) {
        this.usernameEndString = usernameEndString;
    }
}
