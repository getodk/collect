/*
Copyright 2018 Ona

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

package org.odk.collect.android.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.evernote.android.job.Job;

import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.DownloadFormListUtils;
import org.odk.collect.android.utilities.FormDownloader;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

/**
 * This job manages form downloads from external sources. It therefore makes an extra step of verifying that
 * the {@link ApplicationConstants.BundleKeys#FORM_ID} passed points to a form on the forms server.
 * You need to pass {@code transientExtras} with the {@link ApplicationConstants.BundleKeys#FORM_ID}
 * to the {@link com.evernote.android.job.JobRequest.Builder}
 *
 * <code>
 * new JobRequest.Builder(FormDownloadJob.TAG)
 *     .startNow()
 *     .setTransientExtras(jobBundle)
 *     .build()
 *     .schedule();
 * </code>
 *
 * @author by Ephraim Kigamba (nek.eam@gmail.com)
 */

public class FormDownloadJob extends Job {

    public static final String TAG = "FORM_DOWNLOAD_TAG";
    private static final String ACTION = "org.odk.collect.FORM_DOWNLOAD.COMPLETE";
    private String formId;

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Bundle bundle = params.getTransientExtras();
        if (bundle.containsKey(ApplicationConstants.BundleKeys.FORM_ID)) {
            formId = bundle.getString(ApplicationConstants.BundleKeys.FORM_ID);

            if (!TextUtils.isEmpty(formId)) {
                Timber.i("STARTED RUNNING JOB -> Download Form %s", formId);
                HashMap<String, FormDetails> formDetailsHashMap = DownloadFormListUtils.downloadFormList(false);

                if (formDetailsHashMap.containsKey(formId)) {
                    FormDetails formDetails = formDetailsHashMap.get(formId);

                    ArrayList<FormDetails> formDetailsArrayList = new ArrayList<>();
                    formDetailsArrayList.add(formDetails);

                    FormDownloader formDownloader = new FormDownloader();
                    formDownloader.downloadForms(formDetailsArrayList);

                    Timber.i("FINISHED DOWNLOADING FORM : %s", formId);
                    sendDownloadServiceBroadcastResult(getContext(), formId, true, null);
                } else {
                    Timber.e("DOWNLOAD FORM FAILED BECAUSE FORM DOES NOT EXIST ON THE SERVER");
                    sendDownloadServiceBroadcastResult(getContext(), formId, false, "Requested form could not be found");
                }
            } else {
                sendDownloadServiceBroadcastResult(getContext(), formId, false, "Null OR Empty " + ApplicationConstants.BundleKeys.FORM_ID);
            }

            return Result.SUCCESS;
        } else {
            Timber.e("DOWNLOAD FORM FAILED BECAUSE BUNDLE DOES NOT CONTAIN FORM_ID");
            sendDownloadServiceBroadcastResult(getContext(), formId, false, "Bundle does not contain the " + ApplicationConstants.BundleKeys.FORM_ID);
            return Result.FAILURE;
        }
    }

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
    private void sendDownloadServiceBroadcastResult(@NonNull Context context, @Nullable String formId, boolean success, @Nullable String errorReason) {
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
