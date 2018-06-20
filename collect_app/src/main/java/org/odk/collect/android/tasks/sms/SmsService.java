package org.odk.collect.android.tasks.sms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.telephony.SmsManager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.events.SmsEvent;
import org.odk.collect.android.jobs.SmsSenderJob;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SentMessageResult;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.odk.collect.android.tasks.sms.models.SubmitFormModel;
import org.odk.collect.android.utilities.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;
import static org.odk.collect.android.tasks.sms.Mapper.toMessageStatus;
import static org.odk.collect.android.tasks.sms.SmsUtils.checkIfSentIntentExists;
import static org.odk.collect.android.tasks.sms.SmsUtils.checkIfSubmissionSentOrSending;
import static org.odk.collect.android.utilities.FileUtil.getFileContents;
import static org.odk.collect.android.utilities.FileUtil.getSmsInstancePath;

/**
 * Core class that contains all the business logic and services that are utilized to send,track
 * and store Form SMS data.
 */
public class SmsService {

    private SmsManager smsManager;
    private SmsSubmissionManagerContract smsSubmissionManager;
    private Context context;
    private RxEventBus rxEventBus;
    private InstancesDao instancesDao;
    private FormsDao formsDao;

    @Inject
    public SmsService(SmsManager smsManager, SmsSubmissionManagerContract smsSubmissionManager, InstancesDao instancesDao, Context context, RxEventBus rxEventBus, FormsDao formsDao) {
        this.smsManager = smsManager;
        this.smsSubmissionManager = smsSubmissionManager;
        this.context = context;
        this.instancesDao = instancesDao;
        this.rxEventBus = rxEventBus;
        this.formsDao = formsDao;
    }

    public void submitForms(long[] instanceIds) {

        Long[] instanceIdObjects = ArrayUtils.toObject(instanceIds);
        List<Long> list = java.util.Arrays.asList(instanceIdObjects);

        StringBuilder selection = new StringBuilder();

        Iterator<Long> it = list.iterator();

        String[] selectionArgs = new String[list.size()];
        int i = 0;
        while (it.hasNext()) {
            String id = it.next().toString();
            selection.append(InstanceProviderAPI.InstanceColumns._ID + "=?");
            selectionArgs[i++] = id;
            if (i != list.size()) {
                selection.append(" or ");
            }
        }

        try (Cursor results = new InstancesDao().getInstancesCursor(selection.toString(), selectionArgs)) {
            if (results.getCount() > 0) {
                results.moveToPosition(-1);
                while (results.moveToNext()) {
                    String filePath = results.getString(results
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                    String id = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));
                    String displayName = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
                    String formId = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                    String formVersion = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION));

                    SubmitFormModel submitFormModel = new SubmitFormModel();
                    submitFormModel.setDisplayName(displayName);
                    submitFormModel.setInstanceFilePath(filePath);
                    submitFormModel.setInstanceId(id);
                    submitFormModel.setFormId(formId);
                    submitFormModel.setFormVersion(formVersion);

                    submitForm(submitFormModel);
                }
            }
        }
    }

    /**
     * Responsible for fetching the saved form that adheres to the SMS spec and
     * persisting the form as a group of messages so that they can be sent via a
     * background job.
     *
     * @param submitFormModel contains all the properties necessary for form submission
     */
    public boolean submitForm(SubmitFormModel submitFormModel) {

        String text;
        String instanceId = submitFormModel.getInstanceId();

        if (formsDao.isFormEncrypted(submitFormModel.getFormId(), submitFormModel.getFormVersion())) {
            rxEventBus.post(new SmsEvent(instanceId, MessageStatus.Encrypted));
            return false;
        }

        File smsFile = new File(getSmsInstancePath(submitFormModel.getInstanceFilePath()));

        /**
         * No instance.txt file is generated if a form doesn't have sms tags.
         */
        if (!smsFile.exists()) {
            rxEventBus.post(new SmsEvent(instanceId, MessageStatus.NoMessage));
            return false;
        }

        try {
            text = getFileContents(smsFile);
        } catch (IOException e) {

            rxEventBus.post(new SmsEvent(instanceId, MessageStatus.FatalError));
            Timber.e(e);
            return false;
        }

        SmsSubmission model = smsSubmissionManager.getSubmissionModel(instanceId);


        boolean allMessagesAreNotSending = false;

        /**
         * If the model exists that means this instance was probably sent in the pasty.
         */
        if (model != null) {

            /**
             * If the background job for this instance is running then the messages won't be sent again.
             */
            if (model.getJobId() != 0) {
                Job job = JobManager.instance().getJob(model.getJobId());
                if (job != null) {
                    if (!job.isFinished()) {
                        return false;
                    }
                }
            }

            for (Message message : model.getMessages()) {

                /**
                 * If there's a message that hasn't sent then a job should be added to continue the process.
                 */
                if (!(message.isSending() && checkIfSentIntentExists(context, instanceId, message.getId())) || !message.isSent()) {
                    allMessagesAreNotSending = true;
                    break;
                }
            }

            if (allMessagesAreNotSending) {
                addMessagesJobToQueue(instanceId);
            }
        } else {

            final List<String> parts = smsManager.divideMessage(text);

            model = new SmsSubmission();
            model.setJobId(0);
            model.setNotificationId();
            model.setInstanceId(instanceId);
            model.setLastUpdated(new Date());
            model.setDisplayName(submitFormModel.getDisplayName());

            List<Message> messages = new ArrayList<>();

            for (String part : parts) {

                Message message = new Message();
                message.setPart(parts.indexOf(part) + 1);
                message.setText(part);
                message.setMessageStatus(MessageStatus.Ready);
                message.generateRandomMessageID();

                messages.add(message);
            }

            model.setMessages(messages);

            smsSubmissionManager.saveSubmission(model);

            addMessagesJobToQueue(instanceId);
        }

        return true;
    }

    /**
     * Cancels any ongoing job operations and removes the instance's model
     * from storage.
     *
     * @param instanceId id from instanceDao
     */
    public boolean cancelFormSubmission(String instanceId) {

        SmsSubmission model = smsSubmissionManager.getSubmissionModel(instanceId);

        if (model == null) {
            return false;
        }

        smsSubmissionManager.deleteSubmission(instanceId);

        JobManager.instance().cancel(model.getJobId());

        SmsEvent event = new SmsEvent();
        event.setInstanceId(instanceId);
        event.setLastUpdated(model.getLastUpdated());
        event.setStatus(MessageStatus.Canceled);

        rxEventBus.post(event);

        return true;
    }

    /***
     * Receives a model that contains the information received by the intent of the
     * SmsSentBroadcastReceiver that's triggered when a message was sent.
     * This function then determines the next action to perform based on the result it receives.
     * @param sentMessageResult from BroadcastReceiver
     */
    void processMessageSentResult(SentMessageResult sentMessageResult) {

        String log = String.format(Locale.getDefault(), "Received result from broadcast receiver of instance id %s with message id of %d", sentMessageResult.getInstanceId(), sentMessageResult.getMessageId());
        Timber.i(log);

        smsSubmissionManager.updateMessageStatus(toMessageStatus(sentMessageResult.getMessageResultStatus()), sentMessageResult.getInstanceId(), sentMessageResult.getMessageId());

        SmsSubmission model = smsSubmissionManager.getSubmissionModel(sentMessageResult.getInstanceId());

        SmsEvent event = new SmsEvent();
        event.setInstanceId(sentMessageResult.getInstanceId());
        event.setLastUpdated(model.getLastUpdated());
        MessageStatus status = toMessageStatus(sentMessageResult.getMessageResultStatus());
        event.setStatus(checkIfSubmissionSentOrSending(model, status));
        event.setProgress(model.getCompletion());

        rxEventBus.post(event);

        if (model.isSubmissionComplete()) {
            markInstanceAsSubmittedOrDelete(sentMessageResult.getInstanceId());
            smsSubmissionManager.deleteSubmission(sentMessageResult.getInstanceId());
        }
    }

    /***
     * Adds a message as a background job so that it can be sent when necessary.
     * A single message is sent at a time so that progress can be easily tracked and the
     * persisting of form submission's state is more congruent with the amount of message parts.
     *
     * @param instanceId from instanceDao
     */
    protected void addMessagesJobToQueue(String instanceId) {

        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString(SmsSenderJob.INSTANCE_ID, instanceId);

        JobRequest request = new JobRequest.Builder(SmsSenderJob.TAG)
                .addExtras(extras)
                .startNow()
                .build();

        SmsSubmission model = smsSubmissionManager.getSubmissionModel(instanceId);

        int jobId = request.schedule();
        model.setJobId(jobId);

        smsSubmissionManager.saveSubmission(model);
        rxEventBus.post(new SmsEvent(instanceId, MessageStatus.Queued));
    }

    private void markInstanceAsSubmittedOrDelete(String instanceId) {
        String where = InstanceProviderAPI.InstanceColumns._ID + "=?";
        String[] whereArgs = {instanceId};

        ContentValues contentValues = new ContentValues();
        contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);

        try (Cursor cursor = instancesDao.getInstancesCursorForId(instanceId)) {
            cursor.moveToPosition(-1);

            boolean isFormAutoDeleteOptionEnabled = (boolean) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_DELETE_AFTER_SEND);
            String formId;
            while (cursor.moveToNext()) {
                formId = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                if (isFormAutoDeleteEnabled(formId, isFormAutoDeleteOptionEnabled)) {

                    List<String> instancesToDelete = new ArrayList<>();
                    instancesToDelete.add(instanceId);

                    instancesDao.deleteInstancesFromIDs(instancesToDelete);
                } else {
                    instancesDao.updateInstance(contentValues, where, whereArgs);
                }
            }
        }
    }

    /**
     * @param isFormAutoDeleteOptionEnabled represents whether the auto-delete option is enabled at the app level
     *                                      <p>
     *                                      If the form explicitly sets the auto-delete property, then it overrides the preferences.
     */
    private boolean isFormAutoDeleteEnabled(String jrFormId, boolean isFormAutoDeleteOptionEnabled) {
        Cursor cursor = new FormsDao().getFormsCursorForFormId(jrFormId);
        String autoDelete = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int autoDeleteColumnIndex = cursor.getColumnIndex(AUTO_DELETE);
                autoDelete = cursor.getString(autoDeleteColumnIndex);
            } finally {
                cursor.close();
            }
        }
        return autoDelete == null ? isFormAutoDeleteOptionEnabled : Boolean.valueOf(autoDelete);
    }
}
