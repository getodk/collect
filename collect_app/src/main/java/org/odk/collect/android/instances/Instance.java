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

package org.odk.collect.android.instances;

import org.odk.collect.android.storage.StoragePathProvider;

/**
 * A filled form stored on the device.
 *
 * Objects of this class are created using the builder pattern: https://en.wikipedia.org/wiki/Builder_pattern
 */
public final class Instance {
    // status for instances
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_SUBMISSION_FAILED = "submissionFailed";

    private final String displayName;
    private final String submissionUri;
    private final boolean canEditWhenComplete;
    private final String instanceFilePath;
    private final String jrFormId;
    private final String jrVersion;
    private final String status;
    private final Long lastStatusChangeDate;
    private final Long deletedDate;
    private final String geometryType;
    private final String geometry;

    private final Long databaseId;

    private Instance(Builder builder) {
        displayName = builder.displayName;
        submissionUri = builder.submissionUri;
        canEditWhenComplete = builder.canEditWhenComplete;
        instanceFilePath = builder.instanceFilePath;
        jrFormId = builder.jrFormId;
        jrVersion = builder.jrVersion;
        status = builder.status;
        lastStatusChangeDate = builder.lastStatusChangeDate;
        deletedDate = builder.deletedDate;
        geometryType = builder.geometryType;
        geometry = builder.geometry;

        databaseId = builder.databaseId;
    }

    public static class Builder {
        private String displayName;
        private String submissionUri;
        private boolean canEditWhenComplete;
        private String instanceFilePath;
        private String jrFormId;
        private String jrVersion;
        private String status;
        private Long lastStatusChangeDate;
        private Long deletedDate;
        private String geometryType;
        private String geometry;

        private Long databaseId;

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder submissionUri(String submissionUri) {
            this.submissionUri = submissionUri;
            return this;
        }

        public Builder canEditWhenComplete(boolean canEditWhenComplete) {
            this.canEditWhenComplete = canEditWhenComplete;
            return this;
        }

        public Builder instanceFilePath(String instanceFilePath) {
            this.instanceFilePath = new StoragePathProvider().getInstanceDbPath(instanceFilePath);
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

        public Builder deletedDate(Long deletedDate) {
            this.deletedDate = deletedDate;
            return this;
        }

        public Builder geometryType(String geometryType) {
            this.geometryType = geometryType;
            return this;
        }

        public Builder geometry(String geometry) {
            this.geometry = geometry;
            return this;
        }

        public Builder id(Long databaseId) {
            this.databaseId = databaseId;
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

    public boolean canEditWhenComplete() {
        return canEditWhenComplete;
    }

    public String getInstanceFilePath() {
        return instanceFilePath;
    }

    public String getAbsoluteInstanceFilePath() {
        return new StoragePathProvider().getAbsoluteInstanceFilePath(instanceFilePath);
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

    public Long getDeletedDate() {
        return deletedDate;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public String getGeometry() {
        return geometry;
    }

    public Long getId() {
        return databaseId;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Instance
                && this.instanceFilePath.equals(((Instance) other).instanceFilePath);
    }

    @Override
    public int hashCode() {
        return instanceFilePath.hashCode();
    }
}
