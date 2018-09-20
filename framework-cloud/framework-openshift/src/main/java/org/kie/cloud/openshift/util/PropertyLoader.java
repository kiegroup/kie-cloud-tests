/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyLoader {

    private static final Logger log = LoggerFactory.getLogger(PropertyLoader.class);

    private static final Pattern REPLACE_TAG_PATTERN = Pattern.compile(".*\\$\\{(.*)\\}.*");

    private PropertyLoader() {
        // Util class
    }

    /**
     * @param requestingClass Class used for loading resources. In case resources are defined with relative path then class package name is used as base point of relative path.
     * @param resourceFilename File name of resource to be loaded. Can be relative or absolute.
     * @return Loaded and filtered properties.
     */
    public static Properties loadProperties(Class<?> requestingClass, String resourceFilename) {
        Properties properties = loadPropertiesFromResource(requestingClass, resourceFilename);
        return filterProperties(properties);
    }

    private static Properties loadPropertiesFromResource(Class<?> requestingClass, String resourceFilename) {
        Properties properties = new Properties();
        try (InputStream is = requestingClass.getResourceAsStream(resourceFilename)) {
            if (is == null) {
                throw new NoSuchFileException(resourceFilename);
            }
            properties.load(is);
            log.info("Loaded {} propert{} from {}",
                     properties.size(),
                     properties.size() == 1 ? "y" : "ies",
                     resourceFilename
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + resourceFilename, e);
        }
        return properties;
    }

    /**
     * Process properties and replace any variable with appropriate value. The value can be retrieved from another property in properties.
     * Only the first tag in the property value is replaced.
     *
     * @param properties Properties to be processed.
     * @return Processed properties.
     */
    private static Properties filterProperties(Properties properties) {
        Properties processedProperties = new Properties();

        properties.forEach((k,v) -> {
            String value = (String) v;
            Matcher matcher = REPLACE_TAG_PATTERN.matcher(value);
            if(matcher.matches()) {
                // If property tag is provided using system properties then it is replaced automatically when loading properties by Java.
                // This step is used just in case the replacement is another property from loaded properties.
                String replacingKey = matcher.group(1);
                String replacingValue = properties.getProperty(replacingKey);
                validateReplacingValueDoesNotContainReplaceTag((String) k, replacingKey, replacingValue);
                processedProperties.put(k, value.replace("${" + replacingKey + "}", replacingValue));
            } else {
                processedProperties.put(k, v);
            }
        });

        return processedProperties;
    }

    private static void validateReplacingValueDoesNotContainReplaceTag(String originalPropertyKey, String replacingPropertyKey, String replacingPropertyValue) {
        Matcher matcher = REPLACE_TAG_PATTERN.matcher(replacingPropertyValue);
        if(matcher.matches()) {
            throw new RuntimeException("Detected indirect replacement. Value of property '" + originalPropertyKey + "' is being replaced by" +
                                       "property '" + replacingPropertyKey + "', however value of replacing property '" + replacingPropertyValue + "' contains replacement tag.");
        }
    }
}
