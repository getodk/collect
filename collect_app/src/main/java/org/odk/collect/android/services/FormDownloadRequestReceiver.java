package org.odk.collect.android.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.odk.collect.android.utilities.ApplicationConstants;

public class FormDownloadRequestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent =  new Intent(context, FormDownloadService.class);
        if (intent.hasExtra(ApplicationConstants.BundleKeys.FORM_ID) && intent.hasExtra(ApplicationConstants.BundleKeys.TRANSACTION_ID)) {
            serviceIntent.putExtra(ApplicationConstants.BundleKeys.FORM_ID, intent.getStringExtra(ApplicationConstants.BundleKeys.FORM_ID));
            serviceIntent.putExtra(ApplicationConstants.BundleKeys.TRANSACTION_ID, intent.getStringExtra(ApplicationConstants.BundleKeys.TRANSACTION_ID));

            context.startService(serviceIntent);
        }
    }
}
