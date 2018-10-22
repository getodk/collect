/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.upload;

/**
 * Thrown to indicate that a problem with submitting the current finalized form has occurred. The
 * result of getDisplayMessage() is shown to the user.
 *
 * Throwing an UploadException makes the submission attempt move on to the next finalized form to
 * send except in the case of an {@link UploadAuthRequestedException} thrown when the submission
 * attempt was triggered manually by the user. In that case, the finalized form that resulted in the
 * exception will be re-tried after the user provides credentials.
 */
public class UploadException extends Exception {
    public UploadException(String message) {
        super(message);
    }

    public UploadException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns the message that should be shown to the user to describe the type of problem that
     * occurred. In the case of a common status, this is a localized message. In the case of a less
     * common status for which a message is needed primarily for developer troubleshooting, the
     * message is either an exception message or a non-localized English string.
     *
     * TODO: Consider mapping to something machine-readable like a message ID or status ID
     * instead of a mix of localized and non-localized user-facing strings.
     */
    public String getDisplayMessage() {
        return getMessage() != null ? getMessage() : getCause().getMessage();
    }
}