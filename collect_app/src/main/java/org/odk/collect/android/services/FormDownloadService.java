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

package org.odk.collect.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.evernote.android.job.JobRequest;

import org.odk.collect.android.tasks.FormDownloadJob;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FormDownloadBroadcastHelper;

import timber.log.Timber;

/**
 * This service is the entry point for form download requests mainly from external apps. It then passes
 * over the task to {@link FormDownloadJob}
 *
 * How to call this service from an external app:
 *
 * <code>
 *  Intent intent = new Intent();
 *  intent.putExtra("FORM_ID", formID);
 *  intent.setClassName("org.odk.collect.android", "org.odk.collect.android.services.FormDownloadService");
 *  context.startService(intent);
 *</code>
 *
 * @author Ephraim Kigamba (nek.eam@gmail.com)
 */
public class FormDownloadService extends IntentService {

    private String formId;

    public FormDownloadService() {
        super("FormDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.i("RECEIVED FORM DOWNLOAD REQUEST IN SERVICE");
        if (intent != null && intent.hasExtra(ApplicationConstants.BundleKeys.FORM_ID)) {
            formId = intent.getStringExtra(ApplicationConstants.BundleKeys.FORM_ID);
            if (!TextUtils.isEmpty(formId)) {
                Bundle jobBundle = new Bundle();
                jobBundle.putString(ApplicationConstants.BundleKeys.FORM_ID, formId);

                // Start new Job immediately
                new JobRequest.Builder(FormDownloadJob.TAG)
                        .startNow()
                        .setTransientExtras(jobBundle)
                        .build()
                        .schedule();
            } else {
                FormDownloadBroadcastHelper.sendDownloadServiceBroadcastResult(this, formId, false, "Null " + ApplicationConstants.BundleKeys.FORM_ID);
            }
        } else {
            FormDownloadBroadcastHelper.sendDownloadServiceBroadcastResult(this, formId,false, "Bundle does not contain the " + ApplicationConstants.BundleKeys.FORM_ID);
        }
    }
}
