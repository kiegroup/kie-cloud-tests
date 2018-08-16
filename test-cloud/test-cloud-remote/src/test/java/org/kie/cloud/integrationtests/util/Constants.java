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

package org.kie.cloud.integrationtests.util;

public class Constants {
    public class ProcessId {
        public static final String USERTASK = "definition-project.usertask";
        public static final String UPDATED_USERTASK = "definition-project.updated-usertask";
        public static final String SIGNALTASK = "definition-project.signaltask";
        public static final String SIGNALUSERTASK = "definition-project.signalusertask";
        public static final String LONG_SCRIPT = "definition-project.longScript";
        public static final String SIMPLE_RULEFLOW = "simple-ruleflow";
        public static final String LOG = "definition-project.logProcess";
        public static final String TIMER = "timer-start";
    }

    public class Signal {
        public static final String SIGNAL_NAME = "signal1";
        public static final String SIGNAL_2_NAME = "signal2";
    }

    public class User {
        public static final String YODA = "yoda";
    }

    public class BusinessCentralImage {
        public static final String GIT_HOOKS_DIR = "GIT_HOOKS_DIR";
    }
}
