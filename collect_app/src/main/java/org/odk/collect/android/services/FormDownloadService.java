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
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
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
