package org.odk.collect.android.workers;

import android.support.annotation.NonNull;

import org.odk.collect.android.tasks.sms.SmsSender;

import androidx.work.Worker;

/***
 * Sends a SMS submission to a destination.
 */
public class SmsSenderWorker extends Worker {
    public static final String TAG = "smsSenderJob";

    @NonNull
    @Override
    public Result doWork() {
        SmsSender sender = new SmsSender(getApplicationContext(), getInputData().getString(SmsSender.SMS_INSTANCE_ID));

        if (sender.send()) {
            return Result.SUCCESS;
        } else {
            return Result.FAILURE;
        }
    }
}
