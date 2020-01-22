/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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


package org.kie.cloud.util;

public enum Users {
    JOHN("JohnDoe","pwd1234"),
    FRODO("FrodoBaggins","RingBearer"),
    SAM("SamwiseGamgee", "BearerOfRingBearer"),
    GANDALF("GandalfTheGrey","YouShallNotPass"),
    LEGOLAS("Legolas","TheyTakingHobbitsToIsengard"),
    GIMLI("Gimli", "NeverTrustAnElf"),
    ARAGORN("Aragorn","ArwenIsMyLove"),
    BOROMIR("Boromir","OneDoesNotSimply"),
    MERRY("MeriadocBrandybuck","FirstBreakfastMaker"),
    PIPPIN("PeregrinTook","SecondBreakfastMaker");

    private String name;
    private String password;

    private Users(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String toString() {
        return name + ":" + password;
    }
}
