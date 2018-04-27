package org.kie.cloud.openshift.template;

import java.util.Arrays;

class TemplateSelector {

    enum Database {
        postgresql,
        mysql,
        general
    }

    enum Product {
        drools,
        jbpm
    }

    static Database getDatabase() {
        return enumConstantFromSystemProperty("template.database", Database.class);
    }

    static Product getProduct() {
        return enumConstantFromSystemProperty("template.product", Product.class);
    }

    private static <E extends Enum<E>> E enumConstantFromSystemProperty(String key, Class<E> enumClass) {
        final String value = System.getProperty(key);
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Invalid value of system property %s='%s' (must be one of %s)"
                                , key, value, Arrays.toString(enumClass.getEnumConstants())))
                );
    }
}
