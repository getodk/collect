package org.odk.collect.android.tasks.sms;

import android.content.Context;
import android.telephony.SmsManager;

import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.jobs.SmsSenderJob;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SentMessageResult;
import org.odk.collect.android.tasks.sms.models.SmsSubmissionModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.tasks.sms.SmsPendingIntents.checkIfSentIntentExists;
import static org.odk.collect.android.utilities.FileUtil.getFileContents;

/**
 * Core class that contains all the business logic and services that are utilized to send,track
 * and store Form SMS data.
 */
public class SmsService {

    private SmsManager smsManager;
    private SmsSubmissionManagerContract smsSubmissionManager;
    private InstancesDao instancesDao;
    private Context context;

    @Inject
    public SmsService(SmsManager smsManager, SmsSubmissionManagerContract smsSubmissionManager, InstancesDao instancesDao, Context context) {
        this.smsManager = smsManager;
        this.smsSubmissionManager = smsSubmissionManager;
        this.instancesDao = instancesDao;
        this.context = context;
    }

    /**
     * Responsible for fetching the saved form that adheres to the SMS spec and
     * persisting the form as a group of messages so that they can be sent via a
     * background job.
     *
     * @param instanceId id from instanceDao
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

            final List<String> parts = smsManager.divideMessage(text);

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

        addMessageJobToQueue(instanceId);

        return true;
    }

    /**
     * Cancels any ongoing job operations and removes the instance's model
     * from storage.
     *
     * @param instanceId id from instanceDao
     */
    public boolean cancelFormSubmission(String instanceId) {

        SmsSubmissionModel model = smsSubmissionManager.getSubmissionModel(instanceId);

        if (model == null) {
            return false;
        }

        smsSubmissionManager.deleteSubmission(instanceId);

        updateInstanceStatus(instanceId);

        return true;

    }

    /***
     * Receives a model that contains the information received by the intent of the
     * SentBroadcastReceiver that's triggered when a message was sent.
     * This function then determines the next action to perform based on the result it receives.
     * @param sentMessageResult from BroadcastReceiver
     */
    void processMessageSentResult(SentMessageResult sentMessageResult) {

        String log = String.format(Locale.getDefault(), "Received result from broadcast receiver of instance id %s with message id of %d", sentMessageResult.getInstanceId(), sentMessageResult.getMessageId());
        Timber.i(log);

        boolean result = smsSubmissionManager.markMessageAsSent(sentMessageResult.getInstanceId(), sentMessageResult.getMessageId());

        if (!result) {
            return;
        }


        if (sentMessageResult.getMessageStatus().equals(MessageStatus.Sent)) {
            addMessageJobToQueue(sentMessageResult.getInstanceId());
        }
    }

    /***
     * Adds a message as a background job so that it can be sent when necessary.
     * A single message is sent at a time so that progress can be easily tracked and the
     * persisting of form submission's state is more congruent with the amount of message parts.
     *
     * @param instanceId from instanceDao
     */
    protected void addMessageJobToQueue(String instanceId) {

        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString(SmsSenderJob.INSTANCE_ID, instanceId);

        JobRequest request = new JobRequest.Builder(SmsSenderJob.TAG)
                .addExtras(extras)
                .startNow()
                .build();

        request.schedule();
    }

    private void updateInstanceStatus(String instanceId) {

        instancesDao.updateInstance(null, null, null);
        //Uri toUpdate = Uri.withAppendedPath(InstanceProviderAPI.InstanceColumns.CONTENT_URI, instanceId);
    }
}
