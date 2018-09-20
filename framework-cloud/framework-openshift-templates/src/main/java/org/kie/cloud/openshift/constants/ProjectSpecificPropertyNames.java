package org.kie.cloud.openshift.constants;

import org.kie.cloud.openshift.template.ProjectProfile;

/**
 * Property names that differ based on whether droos / jbpm variant of project is used.
 * <p>
 * Usage:
 * <pre>
 *     ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();
 *     envVariables.put(propertyNames.workbenchMavenUserName(), "myuser");
 * </pre>
 */
public interface ProjectSpecificPropertyNames {

    String workbenchMavenUserName();

    String workbenchMavenPassword();

    String workbenchHttpsSecret();

    String workbenchMavenService();

    String workbenchHostnameHttp();

    String workbenchHostnameHttps();

    String workbenchSsoClient();

    String workbenchSsoSecret();

    static ProjectSpecificPropertyNames create() {
        ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
        switch (projectProfile) {
            case JBPM:
                return new JbpmSystemPropertyNames();
            case DROOLS:
                return new DroolsSpecificPropertyNames();
            default:
                throw new IllegalStateException("Unrecognized ProjectProfile: " + projectProfile);
        }
    }
}
