/*
Copyright 2018 Ephraim Kigamba

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License
*/

package org.odk.collect.android.utilities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This helper broadcasts the result of the form download initiated in {@link org.odk.collect.android.services.FormDownloadService}
 *
 * @author Ephraim Kigamba (nek.eam@gmail.com)
 */

public final class FormDownloadBroadcastHelper {

    private FormDownloadBroadcastHelper() {}

    public static final String ACTION = "org.odk.collect.FORM_DOWNLOAD.COMPLETE";

    /**
     * This method sends a broadcast of ACTION {@link #ACTION} communicating whether the form download task initiated
     * at {@link org.odk.collect.android.services.FormDownloadService} was successful or a failure.
     *
     * The broadcast contains the following extras:
     *
     *  - {@link ApplicationConstants.BundleKeys#SUCCESS_KEY}(Boolean) - Mandatory extra. Was the form download successful
     *  - {@link ApplicationConstants.BundleKeys#ERROR_REASON}(String) - Optional. In case of an error, why did the error occur
     *  - {@link ApplicationConstants.BundleKeys#FORM_ID}(Integer) - Optional. Some errors might be because the form id was not passed. This is the FORM_ID for which
     *  the error occurred
     *
     * @param context Android context which should not be null for accessing {@link Context#sendBroadcast(Intent)}
     * @param formId The FORM ID for which the form download result is being communicated
     * @param success Is the form download a success
     * @param errorReason Reason why the form download was a failure
     */
    public static void sendDownloadServiceBroadcastResult(@NonNull Context context, @Nullable String formId, boolean success, @Nullable String errorReason) {
        Intent intent = new Intent(ACTION);
        intent.putExtra(ApplicationConstants.BundleKeys.SUCCESS_KEY, success);

        if (!success && errorReason != null) {
            intent.putExtra(ApplicationConstants.BundleKeys.ERROR_REASON, errorReason);
        }

        if (formId != null) {
            intent.putExtra(ApplicationConstants.BundleKeys.FORM_ID, formId);
        }

        context.sendBroadcast(intent);
    }
}
