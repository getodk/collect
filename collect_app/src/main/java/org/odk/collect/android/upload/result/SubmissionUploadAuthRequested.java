package org.odk.collect.android.upload.result;

import android.net.Uri;
import android.support.annotation.NonNull;

public class SubmissionUploadAuthRequested extends SubmissionUploadResult {
        @NonNull
        private final Uri authRequestingServerUri;

        public SubmissionUploadAuthRequested(@NonNull Uri authRequestingServerUri) {
            super(false, true, null, "Authorization requested");
            this.authRequestingServerUri = authRequestingServerUri;
        }

        @NonNull
        public Uri getAuthRequestingServerUri() {
            return authRequestingServerUri;
        }
    }
