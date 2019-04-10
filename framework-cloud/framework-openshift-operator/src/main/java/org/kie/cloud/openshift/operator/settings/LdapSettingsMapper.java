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
package org.kie.cloud.openshift.operator.settings;

import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.operator.model.components.Ldap;

/**
 * Conversions between LDAP test objects and Kie Operator custom resource POJO.
 */
public class LdapSettingsMapper {

    public static Ldap toLdapModel(LdapSettings ldapSettings) {
        Ldap ldap = new Ldap();
        ldap.setBaseCtxDN(ldapSettings.getLdapBaseCtxDn());
        ldap.setBaseFilter(ldapSettings.getLdapBaseFilter());
        ldap.setBindCredential(ldapSettings.getLdapBindCredential());
        ldap.setBindDN(ldapSettings.getLdapBindDn());
        ldap.setDefaultRole(ldapSettings.getLdapDefaultRole());
        ldap.setDistinguishedNameAttribute(ldapSettings.getLdapDistinguishedNameAttribute());
        ldap.setJaasSecurityDomain(ldapSettings.getLdapJaasSecurityDomain());
        ldap.setParseRoleNameFromDN(Boolean.valueOf(ldapSettings.getLdapParseRoleNameFromDn()));
        ldap.setParseUsername(Boolean.valueOf(ldapSettings.getLdapParseUsername()));
        ldap.setReferralUserAttributeIDToCheck(ldapSettings.getLdapReferralUserAttributeIdToCheck());
        ldap.setRoleAttributeID(ldapSettings.getLdapRoleAttributeId());
        ldap.setRoleAttributeIsDN(Boolean.valueOf(ldapSettings.getLdapRoleAttributeIsDn()));
        ldap.setRoleFilter(ldapSettings.getLdapRoleFilter());
        ldap.setRoleNameAttributeID(ldapSettings.getLdapRoleNameAttributeId());
        ldap.setRoleRecursion(ldapSettings.getLdapRoleRecursion().isEmpty() ? null : Integer.valueOf(ldapSettings.getLdapRoleRecursion()));
        ldap.setRolesCtxDN(ldapSettings.getLdapRolesCtxDn());
        ldap.setSearchScope(ldapSettings.getLdapSearchScope());
        ldap.setSearchTimeLimit(ldapSettings.getLdapSearchTimeLimit().isEmpty() ? null : Integer.valueOf(ldapSettings.getLdapSearchTimeLimit()));
        ldap.setUrl(ldapSettings.getLdapUrl());
        ldap.setUsernameBeginString(ldapSettings.getLdapUsernameBeginString());
        ldap.setUsernameEndString(ldapSettings.getLdapUsernameEndString());
        return ldap;
    }
}
