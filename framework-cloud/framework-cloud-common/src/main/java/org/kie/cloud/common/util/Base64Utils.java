/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Collection of utils to decode string into Base64.
 */
public final class Base64Utils {

    private Base64Utils() {

    }

    /**
     * Decode the content into a base64 codec using UTF-8.
     * @param content to decode.
     * @return a base64 string.
     */
    public static final String decode(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

}
