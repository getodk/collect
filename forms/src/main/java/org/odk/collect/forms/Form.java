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

package org.odk.collect.forms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A form definition stored on the device.
 * <p>
 * Objects of this class are created using the builder pattern: https://en.wikipedia.org/wiki/Builder_pattern
 */
public final class Form {

    private final Long dbId;
    private final String displayName;
    private final String description;
    private final String formId;
    private final String version;
    private final String formFilePath;
    private final String submissionUri;
    private final String base64RSAPublicKey;
    private final String md5Hash;
    private final Long date;
    private final String jrCacheFilePath;
    private final String formMediaPath;
    private final String language;
    private final String autoSend;
    private final String autoDelete;
    private final String geometryXPath;
    private final boolean deleted;
    private final Long lastDetectedAttachmentsUpdateDate;
    private final boolean usesEntities;

    private Form(Form.Builder builder) {
        dbId = builder.dbId;
        displayName = builder.displayName;
        description = builder.description;
        formId = builder.formId;
        version = builder.version;
        formFilePath = builder.formFilePath;
        submissionUri = builder.submissionUri;
        base64RSAPublicKey = builder.base64RSAPublicKey;
        md5Hash = builder.md5Hash;
        date = builder.date;
        jrCacheFilePath = builder.jrCacheFilePath;
        formMediaPath = builder.formMediaPath;
        language = builder.language;
        autoSend = builder.autoSend;
        autoDelete = builder.autoDelete;
        geometryXPath = builder.geometryXpath;
        deleted = builder.deleted;
        lastDetectedAttachmentsUpdateDate = builder.lastDetectedAttachmentsUpdateDate;
        usesEntities = builder.usesEntities;
    }

    public static class Builder {
        private Long dbId;
        private String displayName;
        private String description;
        private String formId;
        private String version;
        private String formFilePath;
        private String submissionUri;
        private String base64RSAPublicKey;
        private String md5Hash;
        private Long date;
        private String jrCacheFilePath;
        private String formMediaPath;
        private String language;
        private String autoSend;
        private String autoDelete;
        private String geometryXpath;
        private boolean deleted;
        private Long lastDetectedAttachmentsUpdateDate;
        private boolean usesEntities;

        public Builder() {
        }

        public Builder(@NotNull Form form) {
            dbId = form.dbId;
            displayName = form.displayName;
            description = form.description;
            formId = form.formId;
            version = form.version;
            formFilePath = form.formFilePath;
            submissionUri = form.submissionUri;
            base64RSAPublicKey = form.base64RSAPublicKey;
            md5Hash = form.md5Hash;
            date = form.date;
            jrCacheFilePath = form.jrCacheFilePath;
            formMediaPath = form.formMediaPath;
            language = form.language;
            autoSend = form.autoSend;
            autoDelete = form.autoDelete;
            geometryXpath = form.geometryXPath;
            deleted = form.deleted;
            lastDetectedAttachmentsUpdateDate = form.lastDetectedAttachmentsUpdateDate;
            usesEntities = form.usesEntities;
        }

        public Builder dbId(Long id) {
            this.dbId = id;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder formId(String formId) {
            this.formId = formId;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder formFilePath(String formFilePath) {
            this.formFilePath = formFilePath;
            return this;
        }

        public Builder submissionUri(String submissionUri) {
            this.submissionUri = submissionUri;
            return this;
        }

        public Builder base64RSAPublicKey(String base64RSAPublicKey) {
            this.base64RSAPublicKey = base64RSAPublicKey;
            return this;
        }

        public Builder md5Hash(String md5Hash) {
            this.md5Hash = md5Hash;
            return this;
        }

        public Builder date(Long date) {
            this.date = date;
            return this;
        }

        public Builder jrCacheFilePath(String jrCacheFilePath) {
            this.jrCacheFilePath = jrCacheFilePath;
            return this;
        }

        public Builder formMediaPath(String formMediaPath) {
            this.formMediaPath = formMediaPath;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder autoSend(String autoSend) {
            this.autoSend = autoSend;
            return this;
        }

        public Builder autoDelete(String autoDelete) {
            this.autoDelete = autoDelete;
            return this;
        }

        public Builder geometryXpath(String geometryXpath) {
            this.geometryXpath = geometryXpath;
            return this;
        }

        public Builder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder lastDetectedAttachmentsUpdateDate(Long lastDetectedAttachmentsUpdateDate) {
            this.lastDetectedAttachmentsUpdateDate = lastDetectedAttachmentsUpdateDate;
            return this;
        }

        public Builder usesEntities(boolean usesEntities) {
            this.usesEntities = usesEntities;
            return this;
        }

        public Form build() {
            return new Form(this);
        }
    }

    public Long getDbId() {
        return dbId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getFormId() {
        return formId;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    public String getFormFilePath() {
        return formFilePath;
    }

    public String getSubmissionUri() {
        return submissionUri;
    }

    @Nullable
    public String getBASE64RSAPublicKey() {
        return base64RSAPublicKey;
    }

    public String getMD5Hash() {
        return md5Hash;
    }

    public Long getDate() {
        return date;
    }

    public String getJrCacheFilePath() {
        return jrCacheFilePath;
    }

    @Nullable
    public String getFormMediaPath() {
        return formMediaPath;
    }

    public String getLanguage() {
        return language;
    }

    @Nullable
    public String getAutoSend() {
        return autoSend;
    }

    public String getAutoDelete() {
        return autoDelete;
    }

    public String getGeometryXpath() {
        return geometryXPath;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Long getLastDetectedAttachmentsUpdateDate() {
        return lastDetectedAttachmentsUpdateDate;
    }

    public boolean usesEntities() {
        return usesEntities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Form form = (Form) o;
        return deleted == form.deleted &&
                Objects.equals(dbId, form.dbId) &&
                Objects.equals(displayName, form.displayName) &&
                Objects.equals(description, form.description) &&
                Objects.equals(formId, form.formId) &&
                Objects.equals(version, form.version) &&
                Objects.equals(formFilePath, form.formFilePath) &&
                Objects.equals(submissionUri, form.submissionUri) &&
                Objects.equals(base64RSAPublicKey, form.base64RSAPublicKey) &&
                Objects.equals(md5Hash, form.md5Hash) &&
                Objects.equals(date, form.date) &&
                Objects.equals(jrCacheFilePath, form.jrCacheFilePath) &&
                Objects.equals(formMediaPath, form.formMediaPath) &&
                Objects.equals(language, form.language) &&
                Objects.equals(autoSend, form.autoSend) &&
                Objects.equals(autoDelete, form.autoDelete) &&
                Objects.equals(geometryXPath, form.geometryXPath) &&
                Objects.equals(lastDetectedAttachmentsUpdateDate, form.lastDetectedAttachmentsUpdateDate) &&
                Objects.equals(usesEntities, form.usesEntities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbId, displayName, description, formId, version, formFilePath,
                submissionUri, base64RSAPublicKey, md5Hash, date, jrCacheFilePath, formMediaPath,
                language, autoSend, autoDelete, geometryXPath, deleted, lastDetectedAttachmentsUpdateDate,
                usesEntities);
    }

    @Override
    public String toString() {
        return "Form{" +
                "formId='" + formId + '\'' +
                "version='" + version + '\'' +
                '}';
    }
}
