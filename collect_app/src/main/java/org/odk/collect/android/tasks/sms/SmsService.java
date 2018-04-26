package org.odk.collect.android.tasks.sms;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import timber.log.Timber;

import static org.odk.collect.android.tasks.sms.SmsPendingIntents.checkIfSentIntentExists;
import static org.odk.collect.android.utilities.FileUtil.getFileContents;
import static org.odk.collect.android.utilities.GeneralUtils.makeCollection;

/**
 * Core class that contains all the business logic and services that are utilized to send,track
 * and store Form SMS data.
 */
public class SmsService {

    @Inject
    SmsManager smsManager;
    @Inject
    JobManager jobManager;
    @Inject
    SmsSubmissionManagerContract smsSubmissionManager;

    private Context context;

    public SmsService(Context context) {
        this.context = context;

        Collect.getInstance().getComponent().inject(this);
    }


    /**
     * Responsible for fetching the saved form that adheres to the SMS spec and
     * persisting the form as a group of messages so that they can be sent via a
     * background job.
     *
     * @param instanceId
     */
    public boolean submitForm(String instanceId, String instanceFilePath) {
        String text;
        try {
            text = getFileContents(new File(instanceFilePath));
        } catch (IOException e) {

            Timber.e(e);
            return false;
        }

        SmsSubmissionModel model = smsSubmissionManager.getSubmissionModel(instanceId);

        if (model != null) {

            Message message = model.getNextUnsentMessage();

            if (message.isSending() && checkIfSentIntentExists(context, instanceId, message.getId())) {
                return false;
            }
        } else {

            List<String> parts = smsManager.divideMessage(text);

            model = new SmsSubmissionModel();
            model.setInstanceId(instanceId);
            model.setDateAdded(new Date());

            List<Message> messages = new ArrayList<>();

            for (String part : parts) {

                Message message = new Message();
                message.setPart(parts.indexOf(part) + 1);
                message.setText(part);
                message.setSent(false);
                message.generateRandomMessageID();

                messages.add(message);
            }

            model.setMessages(messages);

            smsSubmissionManager.saveSubmission(model);
        }

        addMessageJobToQueue(model.getNextUnsentMessage(), instanceId);

        return true;
    }

    /**
     * Cancels any ongoing job operations and removes the instance's model
     * from storage.
     *
     * @param instanceId
     */
    public void cancelFormSubmission(String instanceId) {

        SmsSubmissionModel model = smsSubmissionManager.getSubmissionModel(instanceId);

        if (model == null) {
            return;
        }

        List<String> jobTags = makeCollection(Observable.fromIterable(model.getMessages())
                .flatMap(message -> Observable.just(String.valueOf(message.getId())))
                .blockingIterable());

        jobManager.cancelJobs(TagConstraint.ANY, jobTags.toArray(new String[0]));

        smsSubmissionManager.deleteSubmission(instanceId);
    }

    /***
     * Receives a model that contains the information received by the intent of the
     * SentBroadcastReceiver that's triggered when a message was sent.
     * This function then determines the next action to perform based on the result
     * it receives.
     * @param sentMessageResult
     */
    void processMessageSentResult(SentMessageResult sentMessageResult) {

        Timber.i(String.format("Received result from broadcast receiver of instance id %s with message id of %d", sentMessageResult.getInstanceId(), sentMessageResult.getMessageId()));

        boolean result = smsSubmissionManager.markMessageAsSent(sentMessageResult.getInstanceId(), sentMessageResult.getMessageId());

        if (!result) {
            return;
        }

        SmsSubmissionModel model = smsSubmissionManager.getSubmissionModel(sentMessageResult.getInstanceId());

        if (sentMessageResult.getMessageStatus().equals(MessageStatus.Sent)) {

            Message message = model.getNextUnsentMessage();

            if (message != null) {
                addMessageJobToQueue(message, sentMessageResult.getInstanceId());
            }
        }
    }

    /***
     * Adds a message as a background job so that it can be sent when necessary.
     * A single message is sent at a time so that progress can be easily tracked and the
     * persisting of form submission's state is more congruent with the amount of message parts.
     *
     * @param message
     * @param instanceId
     */
    private void addMessageJobToQueue(Message message, String instanceId) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());

        String gateway = settings.getString(PreferenceKeys.KEY_SMS_GATEWAY, null);

        if (!PhoneNumberUtils.isGlobalPhoneNumber(gateway)) {
            return;
        }

        SmsJobMessage jobMessage = new SmsJobMessage();
        jobMessage.setGateway(gateway);
        jobMessage.setInstanceId(instanceId);
        jobMessage.setMessageId(message.getId());
        jobMessage.setText(message.getText());

        Timber.i(String.format("Adding message with instance id %s & message id of %d to job queue.", jobMessage.getInstanceId(), jobMessage.getMessageId()));

        jobManager.addJobInBackground(new SmsSenderJob(jobMessage));

    }
}
