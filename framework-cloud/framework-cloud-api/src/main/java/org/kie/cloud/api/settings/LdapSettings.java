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
     * Set LDAP Endpoint to connect for authentication. Required: false
     *
     * @param url LDAP Endpoint example: "ldap://myldap.example.com"
     * @return LdapSettings
     */
    LdapSettings withLdapUrl(String url);

    /**
     * Returns Bind DN used for authentication.
     *
     * @return LDAP Bind DN
     */
    String getLdapBindDn();

    /**
     * Set Bind DN used for authentication. Required: false
     *
     * @param bindDn LDAP Bind DN example:
     * "uid=admin,ou=users,ou=exmample,ou=com"
     * @return LdapSettings
     */
    LdapSettings withLdapBindDn(String bindDn);

    /**
     * Returns LDAP Credentials used for authentication.
     *
     * @return LDAP Bind Credentials
     */
    String getLdapBindCredential();

    /**
     * Set LDAP Credentials used for authentication. Required: false
     *
     * @param bindCredential LDAP Bind Credentials example: "Password"
     * @return LdapSettings
     */
    LdapSettings withLdapBindCredential(String bindCredential);

    /**
     * Retunrns the JMX ObjectName of the JaasSecurityDomain used to decrypt the
     * password.
     *
     * @return LDAP JAAS Security Domain
     */
    String getLdapJaasSecurityDomain();

    /**
     * Set the JMX ObjectName of the JaasSecurityDomain used to decrypt the
     * password. Required: false
     *
     * @param jaasSecurityDomain LDAP JAAS Security Domain
     * @return LdapSettings
     */
    LdapSettings withLdapJaasSecurityDomain(String jaasSecurityDomain);

    /**
     * Returns LDAP Base DN of the top-level context to begin the user search.
     *
     * @return LDAP Base DN
     */
    String getLdapBaseCtxDn();

    /**
     * Set LDAP Base DN of the top-level context to begin the user search.
     * Required: false
     *
     * @param baseCtxDn LDAP Base DN example: "ou=users,ou=example,ou=com"
     * @return LdapSettings
     */
    LdapSettings withLdapBaseCtxDn(String baseCtxDn);

    /**
     * Returns LDAP search filter used to locate the context of the user to
     * authenticate.
     *
     * @return LDAP Base Search filter
     */
    String getLdapBaseFilter();

    /**
     * Set LDAP search filter used to locate the context of the user to
     * authenticate. The input username or userDN obtained from the login module
     * callback is substituted into the filter anywhere a {0} expression is
     * used. A common example for the search filter is (uid={0}). Required:
     * false
     *
     * @param baseFilter LDAP Base Search filter example: "(uid={0})"
     * @return LdapSettings
     */
    LdapSettings withLdapBaseFilter(String baseFilter);

    /**
     * Returns the search scope to use.
     *
     * @return LDAP Search scope
     */
    String getLdapSearchScope();

    /**
     * Set the search scope to use. Required: false
     *
     * @param searchScope LDAP Search scope example: "SUBTREE_SCOPE"
     * @return LdapSettings
     */
    LdapSettings withLdapSearchScope(String searchScope);

    /**
     * Returns the timeout in milliseconds for user or role searches.
     *
     * @return LDAP Search time limit
     */
    String getLdapSearchTimeLimit();

    /**
     * Set the timeout in milliseconds for user or role searches. Required:
     * false
     *
     * @param limit LDAP Search time limit example: "10000"
     * @return LdapSettings
     */
    LdapSettings withLdapSearchTimeLimit(Long limit);

    /**
     * Returns the name of the attribute in the user entry that contains the DN
     * of the user.
     *
     * @return LDAP DN attribute
     */
    String getLdapDistinguishedNameAttribute();

    /**
     * Set the name of the attribute in the user entry that contains the DN of
     * the user. This may be necessary if the DN of the user itself contains
     * special characters, backslash for example, that prevent correct user
     * mapping. If the attribute does not exist, the entry’s DN is used.
     * Required: false
     *
     * @param distinguishedNameAttribute LDAP DN attribute example:
     * "distinguishedName"
     * @return LdapSettings
     */
    LdapSettings withLdapDistinguishedNameAttribute(String distinguishedNameAttribute);

    /**
     * Returns a flag indicating if the DN is to be parsed for the username.
     *
     * @return LDAP Parse username
     */
    String getLdapParseUsername();

    /**
     * Set a flag indicating if the DN is to be parsed for the username. If set
     * to true, the DN is parsed for the username. If set to false the DN is not
     * parsed for the username. This option is used together with
     * usernameBeginString and usernameEndString. Required: false
     *
     * @param parseUsername LDAP Parse username example: "true"
     * @return LdapSettings
     */
    LdapSettings withLdapParseUsername(Boolean parseUsername);

    /**
     * Returns the String which define part to be removed from the start of the
     * DN to reveal the username.
     *
     * @return LDAP Username begin string
     */
    String getLdapUsernameBeginString();

    /**
     * Set the String which define part to be removed from the start of the DN
     * to reveal the username. This option is used together with
     * usernameEndString and only taken into account if parseUsername is set to
     * true. Required: false
     *
     * @param usernameBegin LDAP Username begin string
     * @return LdapSettings
     */
    LdapSettings withLdapUsernameBeginString(String usernameBegin);

    /**
     * Returns the String which define part to be removed from the end of the DN
     * to reveal the username.
     *
     * @return LDAP Username end string
     */
    String getLdapUsernameEndString();

    /**
     * Set the String which define part to be removed from the end of the DN to
     * reveal the username. This option is used together with usernameEndString
     * and only taken into account if parseUsername is set to true. Required:
     * false
     *
     * @param usernameEnd LDAP Username end string
     * @return LdapSettings
     */
    LdapSettings withLdapUsernameEndString(String usernameEnd);

    /**
     * Returns name of the attribute containing the user roles.
     *
     * @return LDAP Role attributeID
     */
    String getLdapRoleAttributeId();

    /**
     * Set name of the attribute containing the user roles. Required: false
     *
     * @param roleAttributeId LDAP Role attributeID example: memberOf
     * @return LdapSettings
     */
    LdapSettings withLdapRoleAttributeId(String roleAttributeId);

    /**
     * Returns the fixed DN of the context to search for user roles.
     *
     * @return LDAP Roles Search DN
     */
    String getLdapRolesCtxDn();

    /**
     * Set the fixed DN of the context to search for user roles. This is not the
     * DN where the actual roles are, but the DN where the objects containing
     * the user roles are. For example, in a Microsoft Active Directory server,
     * this is the DN where the user account is. Required: false
     *
     * @param rolesCtxDn LDAP Roles Search DN example:
     * "ou=groups,ou=example,ou=com"
     * @return LdapSettings
     */
    LdapSettings withLdapRolesCtxDn(String rolesCtxDn);

    /**
     * Returns a search filter used to locate the roles associated with the
     * authenticated user.
     *
     * @return LDAP Role search filter
     */
    String getLdapRoleFilter();

    /**
     * Set a search filter used to locate the roles associated with the
     * authenticated user. The input username or userDN obtained from the login
     * module callback is substituted into the filter anywhere a {0} expression
     * is used. The authenticated userDN is substituted into the filter anywhere
     * a {1} is used. An example search filter that matches on the input
     * username is (member={0}). An alternative that matches on the
     * authenticated userDN is (member={1}). Required: false
     *
     * @param roleFilter LDAP Role search filter example: "(memberOf={1})"
     * @return LdapSettings
     */
    LdapSettings withLdapRoleFilter(String roleFilter);

    /**
     * Returns the number of levels of recursion the role search will go below a
     * matching context.
     *
     * @return LDAP Role recursion
     */
    String getLdapRoleRecursion();

    /**
     * Set the number of levels of recursion the role search will go below a
     * matching context. Disable recursion by setting this to 0. Required: false
     *
     * @param roleRecutsion LDAP Role recursion example: "1"
     * @return LdapSettings
     */
    LdapSettings withLdapRoleRecursion(Long roleRecutsion);

    /**
     * Returns a role included for all authenticated users
     *
     * @return LDAP Default role
     */
    String getLdapDefaultRole();

    /**
     * Set a role included for all authenticated users. Rrequired: false
     *
     * @param defaultRole LDAP Default role example: "guest"
     * @return LdapSettings
     */
    LdapSettings withLdapDefaultRole(String defaultRole);

    /**
     * Returns name of the attribute within the roleCtxDN context which contains
     * the role name.
     *
     * @return LDAP Role name attribute ID
     */
    String getLdapRoleNameAttributeId();

    /**
     * Set name of the attribute within the roleCtxDN context which contains the
     * role name. If the roleAttributeIsDN property is set to true, this
     * property is used to find the role object’s name attribute. Required:
     * false
     *
     * @param roleNameAttributeId LDAP Role name attribute ID example: "name"
     * @return LdapSettings
     */
    LdapSettings withLdapRoleNameAttributeId(String roleNameAttributeId);

    /**
     * Returns a flag indicating if the DN returned by a query contains the
     * roleNameAttributeID.
     *
     * @return LDAP Role DN contains roleNameAttributeID
     */
    String getLdapParseRoleNameFromDn();

    /**
     * Set a flag indicating if the DN returned by a query contains the
     * roleNameAttributeID. If set to true, the DN is checked for the
     * roleNameAttributeID. If set to false, the DN is not checked for the
     * roleNameAttributeID. This flag can improve the performance of LDAP
     * queries. Required: false
     *
     * @param parseRoleNameFromDn LDAP Role DN contains roleNameAttributeID
     * example: "false"
     * @return LdapSettings
     */
    LdapSettings withLdapParseRoleNameFromDn(Boolean parseRoleNameFromDn);

    /**
     * Returns the roleAttributeID contains the fully-qualified DN of a role
     * object.
     *
     * @return LDAP Role Attribute ID is DN
     */
    String getLdapRoleAttributeIsDn();

    /**
     * Set whether or not the roleAttributeID contains the fully-qualified DN of
     * a role object. If false, the role name is taken from the value of the
     * roleNameAttributeId attribute of the context name. Certain directory
     * schemas, such as Microsoft Active Directory, require this attribute to be
     * set to true. Required: false
     *
     * @param roleAttributeIsDn LDAP Role Attribute ID is DN example: "false"
     * @return LdapSettings
     */
    LdapSettings withLdapRoleAttributeIsDn(Boolean roleAttributeIsDn);

    /**
     * Returns referrals.
     *
     * @return LDAP Referral user attribute ID
     */
    String getLdapReferralUserAttributeIdToCheck();

    /**
     * If you are not using referrals, this option can be ignored. When using
     * referrals, this option denotes the attribute name which contains users
     * defined for a certain role, for example member, if the role object is
     * inside the referral. Users are checked against the content of this
     * attribute name. If this option is not set, the check will always fail, so
     * role objects cannot be stored in a referral tree. Required: false
     *
     * @param referralUserAttributeIdToCheck LDAP Referral user attribute ID
     * @return LdapSettings
     */
    LdapSettings withLdapReferralUserAttributeIdToCheck(String referralUserAttributeIdToCheck);

}
