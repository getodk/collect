package org.odk.collect.android.tasks.sms;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

import com.birbit.android.jobqueue.JobManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.utilities.GeneralUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class SmsService {

    @Inject
    SmsManager smsManager;
    @Inject
    JobManager jobManager;
    @Inject
    SmsSubmissionManagerContract smsSubmissionManager;

    public SmsService() {
        Collect.getInstance().getComponent().inject(this);
    }

    public void sendForm(String instanceId) {
        String text = "";

        text= GeneralUtils.randomTextMessage(320);

        List<String> parts = smsManager.divideMessage(text);

        SmsSubmissionModel model = new SmsSubmissionModel();
        model.setInstanceId(instanceId);
        model.setDateAdded(new Date());

        List<Message> messages = new ArrayList<>();

        for (String part : parts) {

            Message message = new Message();
            message.setPart(parts.indexOf(part) + 1);
            message.setText(part);

            messages.add(message);
        }

        model.setMessages(messages);

        smsSubmissionManager.saveSubmissionListModel(model);

        addMessageJobToQueue(messages.get(0), instanceId);

    }

    public void processMessageSentResult(SentMessageResult sentMessageResult) {

        Timber.i(String.format("Received result from broadcast receiver of %s with message id of %d",sentMessageResult.getMessageStatus(),sentMessageResult.getMessageId()));
        Timber.i(String.valueOf(sentMessageResult.getMessageId()),sentMessageResult.getInstanceId());

        smsSubmissionManager.markMessageAsSent(sentMessageResult.getInstanceId(),sentMessageResult.getMessageId());

        SmsSubmissionModel model = smsSubmissionManager.getSubmissionModelById(sentMessageResult.getInstanceId());

        if (sentMessageResult.getMessageStatus().equals(MessageStatus.Sent)) {

            Message message = model.getNextUnsentMessage();

            if (message != null) {
                addMessageJobToQueue(message, sentMessageResult.getInstanceId());
            }
        }
    }


    private void addMessageJobToQueue(Message message, String instanceId) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());

        String gateway = settings.getString(PreferenceKeys.KEY_SMS_GATEWAY,null);

        gateway="5554";

        SmsJobMessage jobMessage = new SmsJobMessage();
        jobMessage.setGateway(gateway);
        jobMessage.setInstanceId(instanceId);
        jobMessage.setMessageId(message.getId());
        jobMessage.setText(message.getText());

        Timber.i(String.format("Adding message with instance id %s & message id of %d to job queue.",jobMessage.getInstanceId(),jobMessage.getMessageId()));

        jobManager.addJobInBackground(new SmsSenderJob(jobMessage));

    }
}
