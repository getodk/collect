package org.odk.collect.android.jobs;

import android.support.annotation.NonNull;

import org.odk.collect.android.tasks.sms.SmsSender;

import androidx.work.Worker;

/***
 * Background job that adheres to the fire and forget architecture pattern
 * where it's sole purpose is to send an SMS message to a destination without
 * caring about it's response.
 */
public class SmsSenderWorker extends Worker {
    public static final String TAG = "smsSenderJob";
    public static final String INSTANCE_ID = "instance_id";

    @NonNull
    @Override
    public Result doWork() {
        SmsSender sender = new SmsSender(getApplicationContext(), getInputData().getString(INSTANCE_ID));

        if (sender.send()) {
            return Result.SUCCESS;
        } else {
            return Result.FAILURE;
        }
    }
}
