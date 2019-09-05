/*
 * Copyright 2017 Nafundi
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

package org.odk.collect.android.dto;

public final class TreeReferenceString {

    private final String treeReferenceString;

    private TreeReferenceString(Builder builder) {
        treeReferenceString = builder.treeReferenceString;
    }

    public static class Builder {
        private String treeReferenceString;

        public Builder id(int id) {
            return this;
        }

        public Builder treeReferenceString(String treeReferenceString) {
            this.treeReferenceString = treeReferenceString;
            return this;
        }

        public TreeReferenceString build() {
            return new TreeReferenceString(this);
        }
    }

}
