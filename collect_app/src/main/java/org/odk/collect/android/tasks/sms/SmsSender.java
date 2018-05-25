package org.odk.collect.android.tasks.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.events.SmsEvent;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.tasks.sms.SmsPendingIntents.getSentPendingIntent;

public class SmsSender {
    private Context context;
    private String instanceId;

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
            eventBus.post(new SmsEvent(instanceId, MessageStatus.InvalidGateway));
            return false;
        }

        SmsSubmission model = submissionManager.getSubmissionModel(instanceId);

        ArrayList<PendingIntent> sentIntents = new ArrayList<>();
        ArrayList<String> messages = new ArrayList<>();

        for (Message message : model.getMessages()) {

            if (message.isSent() || message.isSent()) {
                continue;
            }

            PendingIntent sentPendingIntent = getSentPendingIntent(context, instanceId, message.getId());
            sentIntents.add(sentPendingIntent);

            messages.add(message.getText());

            submissionManager.markMessageAsSending(instanceId, message.getId());
        }

        smsManager.sendMultipartTextMessage(gateway, null, messages, sentIntents, null);

        String log = String.format(Locale.getDefault(), "Sending a SMS of instance id %s", instanceId);
        Timber.i(log);

        return true;
    }
}
