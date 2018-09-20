package org.kie.cloud.openshift.template;

import java.util.Arrays;

public enum ProjectProfile {
    DROOLS("decision-central"),
    JBPM("business-central");

    private final String workbenchName;
    private static final String SYSTEM_PROPERTY_NAME = "template.project";

    ProjectProfile(String workbenchName) {
        this.workbenchName = workbenchName;
    }

    public String getWorkbenchName() {
        return workbenchName;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static ProjectProfile fromSystemProperty() {
        final String value = System.getProperty(SYSTEM_PROPERTY_NAME);
        return Arrays.stream(ProjectProfile.values())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Invalid value of system property %s='%s' (must be one of %s)"
                                , SYSTEM_PROPERTY_NAME, value, Arrays.toString(ProjectProfile.values())))
                );
    }
}
