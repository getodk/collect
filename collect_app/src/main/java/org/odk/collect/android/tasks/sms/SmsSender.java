package org.odk.collect.android.tasks.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

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

    public SmsSender(Context context, String instanceId) {
        this.context = context;
        this.instanceId = instanceId;

        Collect.getInstance().getComponent().inject(this);
    }

    public boolean send() {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String gateway = settings.getString(PreferenceKeys.KEY_SMS_GATEWAY, null);

        if (!PhoneNumberUtils.isGlobalPhoneNumber(gateway)) {
            return false;
        }

        SmsSubmission model = submissionManager.getSubmissionModel(instanceId);

        Message message = model.getNextUnsentMessage();

        submissionManager.markMessageAsSending(instanceId, message.getId());

        PendingIntent sentPendingIntent = getSentPendingIntent(context, instanceId, message.getId());

        smsManager.sendTextMessage(String.format(Locale.getDefault(), gateway), null, message.getText(), sentPendingIntent, null);

        String log = String.format(Locale.getDefault(), "Sending a SMS of instance id %s", instanceId);
        Timber.i(log);

        return true;
    }
}
