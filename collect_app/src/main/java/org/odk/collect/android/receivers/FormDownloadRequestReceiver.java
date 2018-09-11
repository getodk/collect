package org.odk.collect.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.evernote.android.job.JobRequest;
import org.odk.collect.android.tasks.FormDownloadJob;
import org.odk.collect.android.utilities.ApplicationConstants;

import timber.log.Timber;


/**
 * This service is the entry point for form download requests mainly from external apps. It then passes
 * over the task to {@link FormDownloadJob}
 * <p>
 * How to call this service from an external app:
 * <p>
 * <code>
 * Intent intent = new Intent("org.odk.collect.android.FORM_DOWNLOAD");
 * intent.putExtra("FORM_ID", "Birds");
 * intent.putExtra("TRANSACTION_ID", UUID.randomUUID().toString());
 *
 * intent.putExtra("URL", "https://opendatakit.appspot.com");
 * intent.putExtra("USERNAME", "johndoe");
 * intent.putExtra("PASSWORD", "8sdkljd9023jksldkjf");
 * context.sendBroadcast(intent)
 * </code>
 *
 *
 * @author Ephraim Kigamba (nek.eam@gmail.com)
 */
public class FormDownloadRequestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(ApplicationConstants.BundleKeys.FORM_ID) && intent.hasExtra(ApplicationConstants.BundleKeys.TRANSACTION_ID)) {
            processRequest(context, intent);
        }
        // This is a dev error and not a user error ?? Not sure if I should return a response. It should fail silently
        Timber.i("Form download request could not be processed since intent does not have either `FORM_ID` or `TRANSACTION_ID`");
    }

    private void processRequest(Context context, Intent intent) {
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

        FormDownloadJob.sendDownloadServiceBroadcastResult(context, transactionId, FormDownloadJob.PROGRESS_REQUEST_RECEIVED, formId, true, null);

        // Start new Job immediately
        // The jobs are queued, therefore return a response immediately communication that the job was received and has been queued
        new JobRequest.Builder(FormDownloadJob.TAG)
                .startNow()
                .setTransientExtras(jobBundle)
                .build()
                // We assume that this request is received on the UI thread
                .scheduleAsync();
    }
}
