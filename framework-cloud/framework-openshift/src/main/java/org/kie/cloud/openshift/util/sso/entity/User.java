/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.cloud.openshift.util.sso.entity;


public class User {

    public static User getAndroid(int id) {
        return getAndroid(id, "x");
    }

    public static User getAndroid(int id, String uniqueIdentifier) {
        String username = String.format("android-%s-%d", uniqueIdentifier, id);
        String password = String.format("pass-%s-%d", uniqueIdentifier, id);
        String firstname = String.format("Bender-%s-%d", uniqueIdentifier, id);
        String lastname = String.format("Rodriguez-%s-%d", uniqueIdentifier, id);
        String email = String.format("death-%s-%d@toallhumans.com", uniqueIdentifier, id);

        return new User(username, password, firstname, lastname, email);
    }

    public String id;
    public String username;
    public String password;
    public String firstName;
    public String lastName;
    public String email;

    public User(String username, String password, String firstName, String lastName, String email) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}