package org.odk.collect.android.tasks.sms;

import android.telephony.SmsManager;

import com.birbit.android.jobqueue.JobManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

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

    public void sendFormAsSms(String instanceId) {
        String text = "";

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

        addMessageJobToQueue(messages.get(0), instanceId, "");

    }

    public void processMessageSentResult(SentMessageResult sentMessageResult) {

        SmsSubmissionModel model = smsSubmissionManager.getSubmissionModelById(sentMessageResult.getInstanceId());

        if (sentMessageResult.getMessageStatus().equals(MessageStatus.Sent)) {

            Message message = model.getNextUnsentMessage();

            if (message != null) {
                addMessageJobToQueue(message, sentMessageResult.getInstanceId(), "");
            }
        }
    }


    private void addMessageJobToQueue(Message message, String instanceId, String gateway) {

        SmsJobMessage jobMessage = new SmsJobMessage();
        jobMessage.setGateway(gateway);
        jobMessage.setInstanceId(instanceId);
        jobMessage.setMessageId(message.getId());
        jobMessage.setText(message.getText());

        jobManager.addJobInBackground(new SmsSenderJob(jobMessage));
    }
}
