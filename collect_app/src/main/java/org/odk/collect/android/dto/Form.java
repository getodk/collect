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
 * This class represents a single row from the forms table which is located in
 * {@link org.odk.collect.android.provider.FormsProvider#DATABASE_NAME}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_transfer_object
 * Objects of this class are created using builder pattern: https://en.wikipedia.org/wiki/Builder_pattern
 */
public class Form {
    private String displayName;
    private String description;
    private String jrFormId;
    private String jrVersion;
    private String formFilePath;
    private String submissionUri;
    private String base64RSAPublicKey;
    private String displaySubtext;
    private String md5Hash;
    private Long date;
    private String jrCacheFilePath;
    private String formMediaPath;
    private String language;

    private Form(Form.Builder builder) {
        displayName = builder.displayName;
        description = builder.description;
        jrFormId = builder.jrFormId;
        jrVersion = builder.jrVersion;
        formFilePath = builder.formFilePath;
        submissionUri = builder.submissionUri;
        base64RSAPublicKey = builder.base64RSAPublicKey;
        displaySubtext = builder.displaySubtext;
        md5Hash = builder.md5Hash;
        date = builder.date;
        jrCacheFilePath = builder.jrCacheFilePath;
        formMediaPath = builder.formMediaPath;
        language = builder.language;
    }

    public static class Builder {
        private String displayName;
        private String description;
        private String jrFormId;
        private String jrVersion;
        private String formFilePath;
        private String submissionUri;
        private String base64RSAPublicKey;
        private String displaySubtext;
        private String md5Hash;
        private Long date;
        private String jrCacheFilePath;
        private String formMediaPath;
        private String language;

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
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

        public Builder displaySubtext(String displaySubtext) {
            this.displaySubtext = displaySubtext;
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

        public Form build() {
            return new Form(this);
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getJrFormId() {
        return jrFormId;
    }

    public String getJrVersion() {
        return jrVersion;
    }

    public String getFormFilePath() {
        return formFilePath;
    }

    public String getSubmissionUri() {
        return submissionUri;
    }

    public String getBASE64RSAPublicKey() {
        return base64RSAPublicKey;
    }

    public String getDisplaySubtext() {
        return displaySubtext;
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

    public String getFormMediaPath() {
        return formMediaPath;
    }

    public String getLanguage() {
        return language;
    }
}
