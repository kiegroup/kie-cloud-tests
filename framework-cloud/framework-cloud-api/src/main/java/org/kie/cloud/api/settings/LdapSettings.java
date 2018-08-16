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
package org.kie.cloud.api.settings;

import java.util.Map;

public interface LdapSettings {

    /**
     * Retruns values set by this class.
     *
     * @return Map of environment variables
     */
    Map<String, String> getEnvVariables();

    /**
     * Returns LDAP Endpoint to connect for authentication.
     *
     * @return LDAP Endpoint
     */
    String getLdapUrl();

    /**
     * Returns Bind DN used for authentication.
     *
     * @return LDAP Bind DN
     */
    String getLdapBindDn();

    /**
     * Returns LDAP Credentials used for authentication.
     *
     * @return LDAP Bind Credentials
     */
    String getLdapBindCredential();

    /**
     * Retunrns the JMX ObjectName of the JaasSecurityDomain used to decrypt the
     * password.
     *
     * @return LDAP JAAS Security Domain
     */
    String getLdapJaasSecurityDomain();

    /**
     * Returns LDAP Base DN of the top-level context to begin the user search.
     *
     * @return LDAP Base DN
     */
    String getLdapBaseCtxDn();

    /**
     * Returns LDAP search filter used to locate the context of the user to
     * authenticate.
     *
     * @return LDAP Base Search filter
     */
    String getLdapBaseFilter();

    /**
     * Returns the search scope to use.
     *
     * @return LDAP Search scope
     */
    String getLdapSearchScope();

    /**
     * Returns the timeout in milliseconds for user or role searches.
     *
     * @return LDAP Search time limit
     */
    String getLdapSearchTimeLimit();

    /**
     * Returns the name of the attribute in the user entry that contains the DN
     * of the user.
     *
     * @return LDAP DN attribute
     */
    String getLdapDistinguishedNameAttribute();

    /**
     * Returns a flag indicating if the DN is to be parsed for the username.
     *
     * @return LDAP Parse username
     */
    String getLdapParseUsername();

    /**
     * Returns the String which define part to be removed from the start of the
     * DN to reveal the username.
     *
     * @return LDAP Username begin string
     */
    String getLdapUsernameBeginString();

    /**
     * Returns the String which define part to be removed from the end of the DN
     * to reveal the username.
     *
     * @return LDAP Username end string
     */
    String getLdapUsernameEndString();

    /**
     * Returns name of the attribute containing the user roles.
     *
     * @return LDAP Role attributeID
     */
    String getLdapRoleAttributeId();

    /**
     * Returns the fixed DN of the context to search for user roles.
     *
     * @return LDAP Roles Search DN
     */
    String getLdapRolesCtxDn();

    /**
     * Returns a search filter used to locate the roles associated with the
     * authenticated user.
     *
     * @return LDAP Role search filter
     */
    String getLdapRoleFilter();

    /**
     * Returns the number of levels of recursion the role search will go below a
     * matching context.
     *
     * @return LDAP Role recursion
     */
    String getLdapRoleRecursion();

    /**
     * Returns a role included for all authenticated users
     *
     * @return LDAP Default role
     */
    String getLdapDefaultRole();

    /**
     * Returns name of the attribute within the roleCtxDN context which contains
     * the role name.
     *
     * @return LDAP Role name attribute ID
     */
    String getLdapRoleNameAttributeId();

    /**
     * Returns a flag indicating if the DN returned by a query contains the
     * roleNameAttributeID.
     *
     * @return LDAP Role DN contains roleNameAttributeID
     */
    String getLdapParseRoleNameFromDn();

    /**
     * Returns the roleAttributeID contains the fully-qualified DN of a role
     * object.
     *
     * @return LDAP Role Attribute ID is DN
     */
    String getLdapRoleAttributeIsDn();

    /**
     * Returns referrals.
     *
     * @return LDAP Referral user attribute ID
     */
    String getLdapReferralUserAttributeIdToCheck();

}
