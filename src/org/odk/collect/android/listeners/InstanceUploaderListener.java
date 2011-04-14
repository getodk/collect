/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.listeners;

import java.net.URI;
import java.util.ArrayList;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

/**
 * @author Carl Hartung (carlhartung@gmail.com)
 */
// TODO: more useful errors in results
public interface InstanceUploaderListener {
    /**
     * Returns the outcomes of the instance uploader task.
     * 
     * @author mitchellsundt@gmail.com
     */
    public static class UploadOutcome {
        public final boolean isSuccessful;
        public final boolean notAllFilesUploaded;
        public final String instanceDir;
        public final String errorMessage;


        /**
         * Successful full upload.
         * 
         * @param instanceDir
         */
        public UploadOutcome(String instanceDir) {
            isSuccessful = true;
            notAllFilesUploaded = false;
            this.instanceDir = instanceDir;
            errorMessage = null;
        }


        /**
         * Successful, but, because the server is not an OpenRosa-compliant server, we only uploaded
         * media files and there are other non-media attachments that we omitted uploading.
         * 
         * @param instanceDir
         * @param ignored
         */
        public UploadOutcome(String instanceDir, boolean ignored) {
            isSuccessful = true;
            notAllFilesUploaded = true;
            this.instanceDir = instanceDir;
            errorMessage = null;
        }


        /**
         * Error during the upload to the indicated URI. The localized mesage describing the error
         * is in the message parameter.
         * 
         * @param instanceDir
         * @param uri
         * @param message
         */
        public UploadOutcome(String instanceDir, String uri, String message) {
            isSuccessful = false;
            notAllFilesUploaded = true;
            this.instanceDir = instanceDir;
            errorMessage =
                message + Collect.getInstance().getString(R.string.while_sending_to) + uri;
        }


        /**
         * Error during the upload to the indicated URI. The localized mesage describing the error
         * is in the message parameter.
         * 
         * @param instanceDir
         * @param uri
         * @param message
         */
        public UploadOutcome(String instanceDir, URI uri, String message) {
            isSuccessful = false;
            notAllFilesUploaded = true;
            this.instanceDir = instanceDir;
            errorMessage =
                message + Collect.getInstance().getString(R.string.while_sending_to)
                        + uri.toString();
        }
    }


    void uploadingComplete(ArrayList<UploadOutcome> result);


    void progressUpdate(int progress, int total);
}
