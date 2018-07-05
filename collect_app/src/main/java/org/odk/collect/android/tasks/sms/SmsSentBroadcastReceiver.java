package org.odk.collect.android.tasks.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.odk.collect.android.application.Collect;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.tasks.sms.SmsSender.SMS_INSTANCE_ID;
import static org.odk.collect.android.tasks.sms.SmsSender.SMS_MESSAGE_ID;

/***
 * Receives events from the SMSManager when a SMS has been sent.
 * This intent is triggered by the SMSSenderJob that's sending different
 * parts of a form each time it's triggered.
 */
public class SmsSentBroadcastReceiver extends BroadcastReceiver {

    @Inject
    SmsService smsService;

    @Override
    public void onReceive(Context context, Intent intent) {

        Collect.getInstance().getComponent().inject(this);

        if (intent.getExtras() == null) {
            Timber.e("getExtras returned a null value.");
            return;
        }

        int messageId = intent.getExtras().getInt(SMS_MESSAGE_ID);
        String instanceId = intent.getExtras().getString(SMS_INSTANCE_ID);

        smsService.processMessageSentResult(instanceId, messageId, getResultCode());
    }
}
