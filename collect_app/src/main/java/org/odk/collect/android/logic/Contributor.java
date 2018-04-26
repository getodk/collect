/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.logic;

public class Contributor {

    private final String commits;
    private final String name;
    private final String email;

    public Contributor(String commits, String name, String email) {
        this.commits = commits;
        this.name = name;
        this.email = email;
    }

    public String getCommits() {
        return commits;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
