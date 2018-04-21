package org.odk.collect.android.tasks.sms;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.odk.collect.android.application.Collect;

import javax.inject.Inject;

public class SmsSenderJob extends Job {

    private static final int PRIORITY = 1;
    public static final String SMS_SEND_ACTION = "org.odk.collect.android.COLLECT_SMS_SEND_ACTION";
    public static final String SMS_INSTANCE_ID = "COLLECT_SMS_INSTANCE_ID";
    public static final String SMS_MESSAGE_ID = "COLLECT_SMS_MESSAGE_ID";

    @Inject
    SmsManager smsManager;
    private SmsJobMessage jobMessage;

    public SmsSenderJob(SmsJobMessage jobMessage) {
        super(new Params(PRIORITY)
                .addTags(String.valueOf(jobMessage.getMessageId()),jobMessage.getInstanceId())
                .persist());

        this.jobMessage = jobMessage;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() {
        Collect.getInstance().getComponent().inject(this);

        Intent sendIntent = new Intent(SMS_SEND_ACTION);
        sendIntent.putExtra(SMS_INSTANCE_ID, jobMessage.getInstanceId());
        sendIntent.putExtra(SMS_MESSAGE_ID, jobMessage.getMessageId());

        PendingIntent sendPendingIntent = PendingIntent.getBroadcast(Collect.getInstance().getApplicationContext(), jobMessage.getMessageId(), sendIntent, 0);
        PendingIntent deliveryPendingIntent = PendingIntent.getBroadcast(Collect.getInstance().getApplicationContext(), jobMessage.getMessageId(), null, 0);

        smsManager.sendTextMessage(jobMessage.getGateway(), null, jobMessage.getText(), sendPendingIntent, deliveryPendingIntent);
    }

    @Override
    protected void onCancel(int i, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int i, int i1) {
        return null;
    }
}
