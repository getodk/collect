package org.odk.collect.android.tasks.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.sms.models.SmsSendResultStatus;
import org.odk.collect.android.tasks.sms.models.SentMessageResult;

import javax.inject.Inject;

import static org.odk.collect.android.tasks.sms.SmsSender.SMS_INSTANCE_ID;
import static org.odk.collect.android.tasks.sms.SmsSender.SMS_MESSAGE_ID;

public class SmsDeliveryBroadcastReceiver extends BroadcastReceiver {

    @Inject
    SmsService smsService;

    @Override
    public void onReceive(Context context, Intent intent) {

        Collect.getInstance().getComponent().inject(this);

        SentMessageResult result = new SentMessageResult();

        result.setMessageId(intent.getExtras().getInt(SMS_MESSAGE_ID));
        result.setInstanceId(intent.getExtras().getString(SMS_INSTANCE_ID));

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                result.setSmsSendResultStatus(SmsSendResultStatus.Delivered);
                break;
            case Activity.RESULT_CANCELED:
                result.setSmsSendResultStatus(SmsSendResultStatus.NotDelivered);
                break;
        }
    }
}
