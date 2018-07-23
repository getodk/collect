package org.odk.collect.android.tasks.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.events.SmsRxEvent;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

import java.util.ArrayList;

import javax.inject.Inject;

import timber.log.Timber;

public class SmsSender {
    private final Context context;
    private final String instanceId;

    public static final String SMS_SEND_ACTION = "org.odk.collect.android.COLLECT_SMS_SEND_ACTION";
    public static final String SMS_INSTANCE_ID = "COLLECT_SMS_INSTANCE_ID";
    static final String SMS_MESSAGE_ID = "COLLECT_SMS_MESSAGE_ID";
    static final String SMS_RESULT_CODE = "COLLECT_SMS_RESULT_CODE";

    @Inject
    SmsManager smsManager;
    @Inject
    SmsSubmissionManagerContract submissionManager;
    @Inject
    RxEventBus eventBus;

    public SmsSender(Context context, String instanceId) {
        this.context = context;
        this.instanceId = instanceId;

        Collect.getInstance().getComponent().inject(this);
    }

    public boolean send() {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String gateway = settings.getString(PreferenceKeys.KEY_SMS_GATEWAY, null);

        if (!PhoneNumberUtils.isGlobalPhoneNumber(gateway)) {
            eventBus.post(new SmsRxEvent(instanceId, SmsService.RESULT_INVALID_GATEWAY));
            return false;
        }

        SmsSubmission model = submissionManager.getSubmissionModel(instanceId);

        ArrayList<PendingIntent> sentIntents = new ArrayList<>();
        ArrayList<String> messages = new ArrayList<>();

        for (Message message : model.getMessages()) {

            if (message.isSent() || message.isSending()) {
                continue;
            }

            PendingIntent sentPendingIntent = getSentPendingIntent(context, instanceId, message.getId());
            sentIntents.add(sentPendingIntent);

            messages.add(message.getText());

            submissionManager.markMessageAsSending(instanceId, message.getId());
        }

        smsManager.sendMultipartTextMessage(gateway, null, messages, sentIntents, null);

        Timber.i("Sending a SMS of instance id %s", instanceId);

        return true;
    }

    /***
     * Create the intent that's passed to SMS Manager so that the Sent SMS
     * BroadcastReceiver can be triggered.
     * @param context necessary to create pending intent.
     * @param instanceId identifies the instance of the form being targeted.
     * @param messageId identifies the specific messages this intent applies to.
     * @return PendingIntent
     */

    private static PendingIntent getSentPendingIntent(Context context, String instanceId, int messageId) {
        Intent sendIntent = new Intent(SMS_SEND_ACTION);
        sendIntent.putExtra(SMS_INSTANCE_ID, instanceId);
        sendIntent.putExtra(SMS_MESSAGE_ID, messageId);

        return PendingIntent.getBroadcast(context, messageId, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /***
     * Recreates the sent intent that was passed to SMS Manager to check if it exists.
     * Useful in cases such as device restarts.
     * @param context necessary to create pending intent.
     * @param instanceId identifies the instance of the form being targeted.
     * @param messageId identifies the specific messages this intent applies to.
     * @return true if it exists.
     */
    static boolean checkIfSentIntentExists(Context context, String instanceId, int messageId) {
        Intent sendIntent = new Intent(SMS_SEND_ACTION);
        sendIntent.putExtra(SMS_INSTANCE_ID, instanceId);
        sendIntent.putExtra(SMS_MESSAGE_ID, messageId);

        return PendingIntent.getBroadcast(context, messageId, sendIntent, PendingIntent.FLAG_NO_CREATE) != null;
    }
}
