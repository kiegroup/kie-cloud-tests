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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Awaitility utils to make a long or repeatable operation.
 */
public class AwaitilityUtils {

    private static final Logger logger = LoggerFactory.getLogger(AwaitilityUtils.class);

    private AwaitilityUtils() {

    }

    /**
     * Wait until supplier returns a not null instance.
     *
     * @param supplier method to return the instance.
     * @return the non null instance.
     */
    @SuppressWarnings("unchecked")
    public static final <T> T untilIsNotNull(Supplier<T> supplier) {
        return until(supplier, (Matcher<T>) Matchers.notNullValue());
    }

    /**
     * Wait until supplier returns a not empty array.
     *
     * @param supplier method to return the instance.
     * @return the non empty array.
     */
    public static final <T> T[] untilIsNotEmpty(Supplier<T[]> supplier) {
        return until(supplier, Matchers.arrayWithSize(Matchers.greaterThan(0)));
    }

    /**
     * Wait until a condition is satisfied.
     *
     * @param asserts custom assertions that the instance must satisfy.
     */
    public static final void untilAsserted(Runnable condition) {
        awaits().untilAsserted(condition::run);
    }

    /**
     * Wait until the supplier returns an instance that satisfies the asserts.
     *
     * @param supplier method to return the instance.
     * @param asserts custom assertions that the instance must satisfy.
     */
    public static final <T> void untilAsserted(Supplier<T> supplier, Consumer<T> asserts) {
        awaits().untilAsserted(() -> asserts.accept(get(supplier).call()));
    }

    private static final <T> T until(Supplier<T> supplier, Matcher<T> matcher) {
        return awaits().until(get(supplier), matcher);
    }

    private static final <T> Callable<T> get(Supplier<T> supplier) {
        return () -> {
            T instance = supplier.get();
            logger.trace("Checking ... {}", instance);
            return instance;
        };
    }

    private static final ConditionFactory awaits() {
        return Awaitility.await()
                         .pollInterval(5, TimeUnit.SECONDS)
                         .atMost(3, TimeUnit.MINUTES)
                         .catchUncaughtExceptions()
                         .ignoreExceptions();
    }
}
