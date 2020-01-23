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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpaceProjects {

    private String spaceName;
    private List<String> projectNames;

    public SpaceProjects(String spaceName) {
        this.spaceName=spaceName;
        this.projectNames=new ArrayList<>();
    }

    public SpaceProjects(String spaceName, String projectName) {
        this(spaceName);
        this.projectNames.add(projectName);
    }

    public SpaceProjects(String spaceName, Collection<String> projectNames) {
        this(spaceName);
        projectNames.addAll(projectNames);
    }

    public String getSpaceName() {
        return spaceName;
    }

    public List<String> getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(List<String> projectNames) {
        this.projectNames = projectNames;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public void addProjectNames(List<String> names) {
        this.projectNames.addAll(names);
    }

}