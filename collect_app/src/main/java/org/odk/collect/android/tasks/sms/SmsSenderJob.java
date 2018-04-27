package org.odk.collect.android.tasks.sms;

import android.app.PendingIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.SmsJobMessage;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.tasks.sms.SmsPendingIntents.getSentPendingIntent;

/***
 * Background job that adheres to the fire and forget architecture pattern
 * where it's sole purpose is to send an SMS message to a destination without
 * caring about it's response.
 */
public class SmsSenderJob extends Job {

    private static final int PRIORITY = 1;

    @Inject
    transient SmsManager smsManager;
    @Inject
    transient SmsSubmissionManagerContract submissionManager;
    private SmsJobMessage jobMessage;

    public SmsSenderJob(SmsJobMessage jobMessage) {
        super(new Params(PRIORITY)
                .addTags(String.valueOf(jobMessage.getMessageId()), jobMessage.getInstanceId())
                .persist());

        this.jobMessage = jobMessage;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() {
        Collect.getInstance().getComponent().inject(this);

        submissionManager.markMessageAsSending(jobMessage.getInstanceId(), jobMessage.getMessageId());

        PendingIntent sentPendingIntent = getSentPendingIntent(getApplicationContext(), jobMessage.getInstanceId(), jobMessage.getMessageId());

        smsManager.sendTextMessage(jobMessage.getGateway(), null, jobMessage.getText(), sentPendingIntent, null);

        Timber.i(String.format("Sending a SMS of instance id %s & message id of %d", jobMessage.getInstanceId(), jobMessage.getMessageId()));
    }

    @Override
    protected void onCancel(int i, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int i, int i1) {
        return null;
    }
}
