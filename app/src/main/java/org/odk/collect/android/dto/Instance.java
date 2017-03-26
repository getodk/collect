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

    private String mDisplayName;
    private String mSubmissionUri;
    private String mCanEditWhenComplete;
    private String mInstanceFilePath;
    private String mJrFormId;
    private String mJrVersion;
    private String mStatus;
    private Long mLastStatusChangeDate;
    private String mDisplaySubtext;
    private Long mDeletedDate;

    private Instance(Builder builder) {
        mDisplayName = builder.mDisplayName;
        mSubmissionUri = builder.mSubmissionUri;
        mCanEditWhenComplete = builder.mCanEditWhenComplete;
        mInstanceFilePath = builder.mInstanceFilePath;
        mJrFormId = builder.mJrFormId;
        mJrVersion = builder.mJrVersion;
        mStatus = builder.mStatus;
        mLastStatusChangeDate = builder.mLastStatusChangeDate;
        mDisplaySubtext = builder.mDisplaySubtext;
        mDeletedDate = builder.mDeletedDate;
    }

    public static class Builder {
        private String mDisplayName;
        private String mSubmissionUri;
        private String mCanEditWhenComplete;
        private String mInstanceFilePath;
        private String mJrFormId;
        private String mJrVersion;
        private String mStatus;
        private Long mLastStatusChangeDate;
        private String mDisplaySubtext;
        private Long mDeletedDate;

        public Builder displayName(String displayName) {
            mDisplayName = displayName;
            return this;
        }

        public Builder submissionUri(String submissionUri) {
            mSubmissionUri = submissionUri;
            return this;
        }

        public Builder canEditWhenComplete(String canEditWhenComplete) {
            mCanEditWhenComplete = canEditWhenComplete;
            return this;
        }

        public Builder instanceFilePath(String instanceFilePath) {
            mInstanceFilePath = instanceFilePath;
            return this;
        }

        public Builder jrFormId(String jrFormId) {
            mJrFormId = jrFormId;
            return this;
        }

        public Builder jrVersion(String jrVersion) {
            mJrVersion = jrVersion;
            return this;
        }

        public Builder status(String status) {
            mStatus = status;
            return this;
        }

        public Builder lastStatusChangeDate(Long lastStatusChangeDate) {
            mLastStatusChangeDate = lastStatusChangeDate;
            return this;
        }

        public Builder displaySubtext(String displaySubtext) {
            mDisplaySubtext = displaySubtext;
            return this;
        }

        public Builder deletedDate(Long deletedDate) {
            mDeletedDate = deletedDate;
            return this;
        }

        public Instance build() {
            return new Instance(this);
        }
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getSubmissionUri() {
        return mSubmissionUri;
    }

    public String getCanEditWhenComplete() {
        return mCanEditWhenComplete;
    }

    public String getInstanceFilePath() {
        return mInstanceFilePath;
    }

    public String getJrFormId() {
        return mJrFormId;
    }

    public String getJrVersion() {
        return mJrVersion;
    }

    public String getStatus() {
        return mStatus;
    }

    public Long getLastStatusChangeDate() {
        return mLastStatusChangeDate;
    }

    public String getDisplaySubtext() {
        return mDisplaySubtext;
    }

    public Long getDeletedDate() {
        return mDeletedDate;
    }
}
