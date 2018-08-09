package org.odk.collect.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.evernote.android.job.JobRequest;

import org.odk.collect.android.tasks.FormDownloadJob;
import org.odk.collect.android.utilities.ApplicationConstants;

import timber.log.Timber;

public class FormDownloadRequestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(ApplicationConstants.BundleKeys.FORM_ID) && intent.hasExtra(ApplicationConstants.BundleKeys.TRANSACTION_ID)) {
            processRequest(intent);
        }
        // This is a dev error and not a user error ?? Not sure if I should return a response. It should fail silently
    }

    private void processRequest(Intent intent) {
        Timber.i("RECEIVED FORM DOWNLOAD REQUEST IN RECEIVER");

        Bundle jobBundle = new Bundle();

        String formId = intent.getStringExtra(ApplicationConstants.BundleKeys.FORM_ID);
        String transactionId = intent.getStringExtra(ApplicationConstants.BundleKeys.TRANSACTION_ID);

        if (intent.hasExtra(ApplicationConstants.BundleKeys.URL) && intent.hasExtra(ApplicationConstants.BundleKeys.USERNAME) && intent.hasExtra(ApplicationConstants.BundleKeys.PASSWORD)) {

            // Pass over the nulls if any
            jobBundle.putString(ApplicationConstants.BundleKeys.URL, intent.getStringExtra(ApplicationConstants.BundleKeys.URL));
            jobBundle.putString(ApplicationConstants.BundleKeys.USERNAME, intent.getStringExtra(ApplicationConstants.BundleKeys.USERNAME));
            jobBundle.putString(ApplicationConstants.BundleKeys.PASSWORD, intent.getStringExtra(ApplicationConstants.BundleKeys.PASSWORD));
        }

        jobBundle.putString(ApplicationConstants.BundleKeys.FORM_ID, formId);
        jobBundle.putString(ApplicationConstants.BundleKeys.TRANSACTION_ID, transactionId);

        // Start new Job immediately
        new JobRequest.Builder(FormDownloadJob.TAG)
                .startNow()
                .setTransientExtras(jobBundle)
                .build()
                // We assume that this request is received on the UI thread
                .scheduleAsync();
    }
}
