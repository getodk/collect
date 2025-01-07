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

package org.odk.collect.forms.instances;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A filled form stored on the device.
 * <p>
 * Objects of this class are created using the builder pattern: https://en.wikipedia.org/wiki/Builder_pattern
 */
public final class Instance {
    // status for instances
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_INVALID = "invalid";
    public static final String STATUS_VALID = "valid";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_SUBMISSION_FAILED = "submissionFailed";

    public static final String GEOMETRY_TYPE_POINT = "Point";

    private final String displayName;
    private final String submissionUri;
    private final boolean canEditWhenComplete;
    private final String instanceFilePath;
    private final String formId;
    private final String formVersion;
    private final String status;
    private final Long lastStatusChangeDate;
    private final Long deletedDate;
    private final String geometryType;
    private final String geometry;

    private final Long dbId;
    private final boolean canDeleteBeforeSend;

    private Instance(Builder builder) {
        displayName = builder.displayName;
        submissionUri = builder.submissionUri;
        canEditWhenComplete = builder.canEditWhenComplete;
        instanceFilePath = builder.instanceFilePath;
        formId = builder.formId;
        formVersion = builder.formVersion;
        status = builder.status;
        lastStatusChangeDate = builder.lastStatusChangeDate;
        deletedDate = builder.deletedDate;
        geometryType = builder.geometryType;
        geometry = builder.geometry;
        canDeleteBeforeSend = builder.canDeleteBeforeSend;

        dbId = builder.dbId;
    }

    public static class Builder {
        private String displayName;
        private String submissionUri;
        private boolean canEditWhenComplete = true;
        private String instanceFilePath;
        private String formId;
        private String formVersion;
        private String status;
        private Long lastStatusChangeDate;
        private Long deletedDate;
        private String geometryType;
        private String geometry;

        private Long dbId;
        private boolean canDeleteBeforeSend = true;

        public Builder() {

        }

        public Builder(Instance instance) {
            dbId = instance.dbId;
            displayName = instance.displayName;
            submissionUri = instance.submissionUri;
            canEditWhenComplete = instance.canEditWhenComplete;
            instanceFilePath = instance.instanceFilePath;
            formId = instance.formId;
            formVersion = instance.formVersion;
            status = instance.status;
            lastStatusChangeDate = instance.lastStatusChangeDate;
            deletedDate = instance.deletedDate;
            geometryType = instance.geometryType;
            geometry = instance.geometry;
            canDeleteBeforeSend = instance.canDeleteBeforeSend;
        }

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
            this.instanceFilePath = instanceFilePath;
            return this;
        }

        public Builder formId(String formId) {
            this.formId = formId;
            return this;
        }

        public Builder formVersion(String jrVersion) {
            this.formVersion = jrVersion;
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

        public Builder dbId(Long dbId) {
            this.dbId = dbId;
            return this;
        }

        @NotNull
        public Builder canDeleteBeforeSend(boolean canDeleteBeforeSend) {
            this.canDeleteBeforeSend = canDeleteBeforeSend;
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

    public String getFormId() {
        return formId;
    }

    public String getFormVersion() {
        return formVersion;
    }

    public String getStatus() {
        return status;
    }

    public Long getLastStatusChangeDate() {
        return lastStatusChangeDate;
    }

    @Nullable
    public Long getDeletedDate() {
        return deletedDate;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public String getGeometry() {
        return geometry;
    }

    public Long getDbId() {
        return dbId;
    }

    public boolean canDeleteBeforeSend() {
        return canDeleteBeforeSend;
    }

    public boolean canDelete() {
        return canDeleteBeforeSend || !status.equals(Instance.STATUS_COMPLETE) && !status.equals(Instance.STATUS_SUBMISSION_FAILED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Instance instance = (Instance) o;
        return canEditWhenComplete == instance.canEditWhenComplete && Objects.equals(displayName, instance.displayName) && Objects.equals(submissionUri, instance.submissionUri) && Objects.equals(instanceFilePath, instance.instanceFilePath) && Objects.equals(formId, instance.formId) && Objects.equals(formVersion, instance.formVersion) && Objects.equals(status, instance.status) && Objects.equals(lastStatusChangeDate, instance.lastStatusChangeDate) && Objects.equals(deletedDate, instance.deletedDate) && Objects.equals(geometryType, instance.geometryType) && Objects.equals(geometry, instance.geometry) && Objects.equals(dbId, instance.dbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, submissionUri, canEditWhenComplete, instanceFilePath, formId, formVersion, status, lastStatusChangeDate, deletedDate, geometryType, geometry, dbId);
    }
}
