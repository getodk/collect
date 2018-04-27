package org.odk.collect.android.tasks.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SentMessageResult;

import javax.inject.Inject;

/***
 * Receives events from the SMSManager when a SMS has been sent.
 * This intent is triggered by the SMSSenderJob that's sending different
 * parts of a form each time it's triggered.
 */
public class SentBroadcastReceiver extends BroadcastReceiver {

    @Inject
    SmsService smsService;

    @Override
    public void onReceive(Context context, Intent intent) {

        Collect.getInstance().getComponent().inject(this);

        SentMessageResult result = new SentMessageResult();

        result.setMessageId(intent.getExtras().getInt(SmsPendingIntents.SMS_MESSAGE_ID));
        result.setInstanceId(intent.getExtras().getString(SmsPendingIntents.SMS_INSTANCE_ID));

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                result.setMessageStatus(MessageStatus.Sent);
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                result.setMessageStatus(MessageStatus.FatalError);
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                result.setMessageStatus(MessageStatus.NoReception);
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                result.setMessageStatus(MessageStatus.FatalError);
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                result.setMessageStatus(MessageStatus.AirplaneMode);

                break;
        }

        smsService.processMessageSentResult(result);
    }
}
