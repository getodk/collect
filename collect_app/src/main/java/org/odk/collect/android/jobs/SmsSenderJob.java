package org.odk.collect.android.jobs;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import org.odk.collect.android.tasks.sms.SmsSender;

/***
 * Background job that adheres to the fire and forget architecture pattern
 * where it's sole purpose is to send an SMS message to a destination without
 * caring about it's response.
 */
public class SmsSenderJob extends Job {
    public static final String TAG = "smsSenderJob";
    public static final String INSTANCE_ID = "instance_id";

    @Override
    protected Result onRunJob(@NonNull Params params) {
        SmsSender sender = new SmsSender(getContext(), params.getExtras().getString(INSTANCE_ID, ""));

        sender.send();

        return null;
    }
}
