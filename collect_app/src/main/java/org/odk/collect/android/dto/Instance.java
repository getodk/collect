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

/**
 * This class represents a single row from the instances table which is located in
 * {@link org.odk.collect.android.provider.InstanceProvider#DATABASE_NAME}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_transfer_object
 * Objects of this class are created using builder pattern: https://en.wikipedia.org/wiki/Builder_pattern
 */
public class Instance {

    private final String displayName;
    private final String submissionUri;
    private final String canEditWhenComplete;
    private final String instanceFilePath;
    private final String jrFormId;
    private final String jrVersion;
    private final String status;
    private final Long lastStatusChangeDate;
    private final String displaySubtext;
    private final Long deletedDate;

    private Instance(Builder builder) {
        displayName = builder.displayName;
        submissionUri = builder.submissionUri;
        canEditWhenComplete = builder.canEditWhenComplete;
        instanceFilePath = builder.instanceFilePath;
        jrFormId = builder.jrFormId;
        jrVersion = builder.jrVersion;
        status = builder.status;
        lastStatusChangeDate = builder.lastStatusChangeDate;
        displaySubtext = builder.displaySubtext;
        deletedDate = builder.deletedDate;
    }

    public static class Builder {
        private String displayName;
        private String submissionUri;
        private String canEditWhenComplete;
        private String instanceFilePath;
        private String jrFormId;
        private String jrVersion;
        private String status;
        private Long lastStatusChangeDate;
        private String displaySubtext;
        private Long deletedDate;

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder submissionUri(String submissionUri) {
            this.submissionUri = submissionUri;
            return this;
        }

        public Builder canEditWhenComplete(String canEditWhenComplete) {
            this.canEditWhenComplete = canEditWhenComplete;
            return this;
        }

        public Builder instanceFilePath(String instanceFilePath) {
            this.instanceFilePath = instanceFilePath;
            return this;
        }

        public Builder jrFormId(String jrFormId) {
            this.jrFormId = jrFormId;
            return this;
        }

        public Builder jrVersion(String jrVersion) {
            this.jrVersion = jrVersion;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder lastStatusChangeDate(Long lastStatusChangeDate) {
            this.lastStatusChangeDate = lastStatusChangeDate;
            return this;
        }

        public Builder displaySubtext(String displaySubtext) {
            this.displaySubtext = displaySubtext;
            return this;
        }

        public Builder deletedDate(Long deletedDate) {
            this.deletedDate = deletedDate;
            return this;
        }

        public Instance build() {
            return new Instance(this);
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSubmissionUri() {
        return submissionUri;
    }

    public String getCanEditWhenComplete() {
        return canEditWhenComplete;
    }

    public String getInstanceFilePath() {
        return instanceFilePath;
    }

    public String getJrFormId() {
        return jrFormId;
    }

    public String getJrVersion() {
        return jrVersion;
    }

    public String getStatus() {
        return status;
    }

    public Long getLastStatusChangeDate() {
        return lastStatusChangeDate;
    }

    public String getDisplaySubtext() {
        return displaySubtext;
    }

    public Long getDeletedDate() {
        return deletedDate;
    }
}
