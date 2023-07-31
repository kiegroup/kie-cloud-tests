/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.api.constants;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestInfoPrinter {

    private static final Logger logger = LoggerFactory.getLogger(TestInfoPrinter.class);
    public static final String PRINT_TEST_CONSTANTS_DISABLED = "print.test.constants.disabled";

    public static void printTestConstants() {
        if (System.getProperty(PRINT_TEST_CONSTANTS_DISABLED) != null
                && Boolean.valueOf(System.getProperty(PRINT_TEST_CONSTANTS_DISABLED)).equals(Boolean.TRUE)) {
            logger.info("Initializing Constants print is disabled.");
            return;
        }

        logger.info("--------- Initializing Constants ----------");

        ServiceLoader<Constants> serviceLoader = ServiceLoader.load(Constants.class);

        TreeMap<String, String> params = new TreeMap<String, String>();
        int maxKeyLength = 0;
        for (Constants constants : serviceLoader) {
            for (Field f : constants.getClass().getDeclaredFields()) {
                if (String.class.isAssignableFrom(f.getType())) {
                    try {
                        String paramName = (String) f.get(null);
                        String paramValue = System.getProperty(paramName);
                        maxKeyLength = Math.max(maxKeyLength, paramName.length());
                        params.put(paramName, paramValue);
                    } catch (IllegalAccessException ex) {
                        logger.error("Cannot read field '{}'.", f.getName(), ex);
                    }
                }
            }
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramName = entry.getKey();
            String value = entry.getValue();

            logger.info("{} = {}",
                    String.format("%-" + maxKeyLength + "s", paramName),
                    value
            );
        }

        logger.info("-------------------------------------------");
    }
}
