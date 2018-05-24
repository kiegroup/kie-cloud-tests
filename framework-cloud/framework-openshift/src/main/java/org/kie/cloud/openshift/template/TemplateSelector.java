package org.kie.cloud.openshift.template;

import java.util.Arrays;

class TemplateSelector {

    enum Project {
        DROOLS,
        JBPM;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    static Project getProject() {
        return enumConstantFromSystemProperty("template.project", Project.class);
    }

    private static <E extends Enum<E>> E enumConstantFromSystemProperty(String key, Class<E> enumClass) {
        final String value = System.getProperty(key);
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Invalid value of system property %s='%s' (must be one of %s)"
                                , key, value, Arrays.toString(enumClass.getEnumConstants())))
                );
    }
}
