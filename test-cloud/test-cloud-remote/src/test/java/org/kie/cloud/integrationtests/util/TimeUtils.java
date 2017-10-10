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

package org.kie.cloud.integrationtests.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.BooleanSupplier;

public class TimeUtils {

    private static final long DEFAULT_WAIT_STEP = 5000;

    public static void waitMilliseconds(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException("Waiting was interrupted", e);
        }
    }

    public static void waitMilliseconds(long milliseconds, BooleanSupplier booleanSupplier) {
        waitMilliseconds(milliseconds, DEFAULT_WAIT_STEP, booleanSupplier);
    }

    public static void waitMilliseconds(long milliseconds, long stepMilliseconds, BooleanSupplier booleanSupplier) {
        Instant startTime = Instant.now();
        while (startTime.plus(milliseconds, ChronoUnit.MILLIS).isAfter(Instant.now()) && !booleanSupplier.getAsBoolean()) {
            waitMilliseconds(stepMilliseconds);
        }
    }
}
