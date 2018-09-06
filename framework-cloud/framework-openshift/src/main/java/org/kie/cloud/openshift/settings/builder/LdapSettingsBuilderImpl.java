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
package org.kie.cloud.openshift.settings.builder;

import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.api.settings.builder.LdapSettingsBuilder;
import org.kie.cloud.openshift.settings.LdapSettingsImpl;

public class LdapSettingsBuilderImpl implements LdapSettingsBuilder {

    private LdapSettingsImpl ldapSettings;

    public LdapSettingsBuilderImpl() {
        ldapSettings = new LdapSettingsImpl();
    }

    @Override
    public LdapSettings build() {
        return ldapSettings;
    }

    @Override
    @Deprecated
    public LdapSettingsBuilder withLdapUrl(String url) {
        ldapSettings.setUrl(url);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapBindDn(String bindDn) {
        ldapSettings.setBindDn(bindDn);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapBindCredential(String bindCredential) {
        ldapSettings.setBindCredential(bindCredential);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapJaasSecurityDomain(String jaasSecurityDomain) {
        ldapSettings.setJaasSecurityDomain(jaasSecurityDomain);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapBaseCtxDn(String baseCtxDn) {
        ldapSettings.setBaseCtxDn(baseCtxDn);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapBaseFilter(String baseFilter) {
        ldapSettings.setBaseFilter(baseFilter);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapSearchScope(String searchScope) {
        ldapSettings.setSearchScope(searchScope);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapSearchTimeLimit(Long limit) {
        ldapSettings.setLimit(limit);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapDistinguishedNameAttribute(String distinguishedNameAttribute) {
        ldapSettings.setDistinguishedNameAttribute(distinguishedNameAttribute);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapParseUsername(Boolean parseUsername) {
        ldapSettings.setParseUsername(parseUsername);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapUsernameBeginString(String usernameBegin) {
        ldapSettings.setUsernameBegin(usernameBegin);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapUsernameEndString(String usernameEnd) {
        ldapSettings.setUsernameEnd(usernameEnd);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapRoleAttributeId(String roleAttributeId) {
        ldapSettings.setRoleAttributeId(roleAttributeId);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapRolesCtxDn(String rolesCtxDn) {
        ldapSettings.setRolesCtxDn(rolesCtxDn);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapRoleFilter(String roleFilter) {
        ldapSettings.setRoleFilter(roleFilter);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapRoleRecursion(Long roleRecutsion) {
        ldapSettings.setRoleRecutsion(roleRecutsion);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapDefaultRole(String defaultRole) {
        ldapSettings.setDefaultRole(defaultRole);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapRoleNameAttributeId(String roleNameAttributeId) {
        ldapSettings.setRoleNameAttributeId(roleNameAttributeId);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapParseRoleNameFromDn(Boolean parseRoleNameFromDn) {
        ldapSettings.setParseRoleNameFromDn(parseRoleNameFromDn);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapRoleAttributeIsDn(Boolean roleAttributeIsDn) {
        ldapSettings.setRoleAttributeIsDn(roleAttributeIsDn);
        return this;
    }

    @Override
    public LdapSettingsBuilder withLdapReferralUserAttributeIdToCheck(String referralUserAttributeIdToCheck) {
        ldapSettings.setReferralUserAttributeIdToCheck(referralUserAttributeIdToCheck);
        return this;
    }

}
