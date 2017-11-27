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

package org.kie.cloud.common.time;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.BooleanSupplier;

public class TimeUtils {

    private static final Duration DEFAULT_WAIT_STEP = Duration.of(5, ChronoUnit.SECONDS);

    public static void wait(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException("Waiting was interrupted", e);
        }
    }

    public static void wait(Duration maxTime, BooleanSupplier booleanSupplier) {
        wait(maxTime, DEFAULT_WAIT_STEP, booleanSupplier);
    }

    public static void wait(Duration maxDuration, Duration waitStep, BooleanSupplier booleanSupplier) {
        Instant startTime = Instant.now();
        while (startTime.plus(maxDuration).isAfter(Instant.now()) && !booleanSupplier.getAsBoolean()) {
            wait(waitStep);
        }
    }
}
