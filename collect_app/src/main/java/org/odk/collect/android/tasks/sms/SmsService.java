package org.odk.collect.android.tasks.sms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.telephony.SmsManager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.events.SmsRxEvent;
import org.odk.collect.android.jobs.SmsSenderJob;
import org.odk.collect.android.logic.FormInfo;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.InstanceUploader;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.Message;
import org.odk.collect.android.tasks.sms.models.SentMessageResult;
import org.odk.collect.android.tasks.sms.models.SmsProgress;
import org.odk.collect.android.tasks.sms.models.SmsStatus;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.odk.collect.android.tasks.sms.models.SubmitFormModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.tasks.sms.SmsSender.checkIfSentIntentExists;
import static org.odk.collect.android.utilities.FileUtil.getFileContents;
import static org.odk.collect.android.utilities.FileUtil.getSmsInstancePath;

/**
 * Core class that contains all the business logic and services that are utilized to send,track
 * and store Form SMS data.
 */
public class SmsService {

    private final SmsManager smsManager;
    private final SmsSubmissionManagerContract smsSubmissionManager;
    private final Context context;
    private final RxEventBus rxEventBus;
    private final InstancesDao instancesDao;
    private final FormsDao formsDao;

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

        StringBuilder selection = new StringBuilder();
        String[] selectionArgs = new String[instanceIds.length];
        for (int i = 0; i < instanceIds.length; i++) {
            long id = instanceIds[i];
            selection.append(InstanceProviderAPI.InstanceColumns._ID + "=?");
            selectionArgs[i] = String.valueOf(id);
            if (i < instanceIds.length && instanceIds.length > 1) {
                selection.append(" or ");
            }
        }

        try (Cursor results = new InstancesDao().getInstancesCursor(selection.toString(), selectionArgs)) {
            if (results.getCount() > 0) {
                results.moveToPosition(-1);
                while (results.moveToNext()) {
                    String filePath = results.getString(results
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                    String instanceId = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));
                    String displayName = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
                    String formId = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                    String formVersion = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION));

                    FormInfo info = new FormInfo(filePath,formId,formVersion);

                    submitForm(instanceId,info,displayName);
                }
            }
        }
    }

    /**
     * Responsible for fetching the saved form that adheres to the SMS spec and
     * persisting the form as a group of messages so that they can be sent via a
     * background job.
     */
    public boolean submitForm(String instanceId, FormInfo info, String displayName) {

        String text;

        if (formsDao.isFormEncrypted(info.getFormID(), info.getFormVersion())) {
            SmsRxEvent event = new SmsRxEvent(instanceId, SmsStatus.Encrypted);
            updateInstanceStatusFailedText(instanceId, event);
            rxEventBus.post(event);
            return false;
        }

        File smsFile = new File(getSmsInstancePath(info.getInstancePath()));

        /**
         * No instance.txt file is generated if a form doesn't have sms tags.
         */
        if (!smsFile.exists()) {
            SmsRxEvent event = new SmsRxEvent(instanceId, SmsStatus.NoMessage);
            updateInstanceStatusFailedText(instanceId, event);
            rxEventBus.post(event);
            return false;
        }

        try {
            text = getFileContents(smsFile);
        } catch (IOException e) {

            SmsRxEvent event = new SmsRxEvent(instanceId, SmsStatus.FatalError);
            updateInstanceStatusFailedText(instanceId, event);
            rxEventBus.post(event);
            Timber.e(e);
            return false;
        }

        SmsSubmission model = smsSubmissionManager.getSubmissionModel(instanceId);

        /*
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

                /*
                 * If there's a message that hasn't sent then a job should be added to continue the process.
                 */
                if (!(message.isSending() && checkIfSentIntentExists(context, instanceId, message.getId())) || !message.isSent()) {
                    addMessagesJobToQueue(instanceId);
                    break;
                }
            }
        } else {

            final List<String> parts = smsManager.divideMessage(text);

            model = new SmsSubmission();
            model.setJobId(0);
            model.setNotificationId();
            model.setInstanceId(instanceId);
            model.setLastUpdated(new Date());
            model.setDisplayName(displayName);

            List<Message> messages = new ArrayList<>();

            for (String part : parts) {

                Message message = new Message();
                message.setPartNumber(parts.indexOf(part) + 1);
                message.setText(part);
                message.setSmsStatus(SmsStatus.Ready);
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

        smsSubmissionManager.forgetSubmission(instanceId);

        JobManager.instance().cancel(model.getJobId());

        SmsRxEvent event = new SmsRxEvent();
        event.setInstanceId(instanceId);
        event.setLastUpdated(model.getLastUpdated());
        event.setStatus(SmsStatus.Canceled);

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

        smsSubmissionManager.updateMessageStatus(sentMessageResult.getSmsSendResultStatus().toMessageStatus(), sentMessageResult.getInstanceId(), sentMessageResult.getMessageId());

        SmsSubmission model = smsSubmissionManager.getSubmissionModel(sentMessageResult.getInstanceId());

        SmsRxEvent event = new SmsRxEvent();
        event.setInstanceId(sentMessageResult.getInstanceId());
        event.setLastUpdated(model.getLastUpdated());
        SmsStatus status = sentMessageResult.getSmsSendResultStatus().toMessageStatus();
        event.setStatus(model.isSentOrSending(status));
        event.setProgress(model.getCompletion());

        rxEventBus.post(event);

        /*
         * If the submission has completed then the instance is marked as submitted.
         * If not and the message isn't still sending then that means an error occurred
         * so that error message is persisted along with SUBMISSION_STATUS_FAILED
         * */
        if (model.isSubmissionComplete()) {
            markInstanceAsSubmittedOrDelete(sentMessageResult.getInstanceId());
            smsSubmissionManager.forgetSubmission(sentMessageResult.getInstanceId());
        } else if (!status.equals(SmsStatus.Sent)) {
            updateInstanceStatusFailedText(sentMessageResult.getInstanceId(), event);
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
        rxEventBus.post(new SmsRxEvent(instanceId, SmsStatus.Queued));
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
                if (InstanceUploader.isFormAutoDeleteEnabled(formId, isFormAutoDeleteOptionEnabled)) {

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
     * @param instanceId of the instance being updated
     * @param event      with the specific failed status that's gonna be persisted
     */
    private void updateInstanceStatusFailedText(String instanceId, SmsRxEvent event) {
        String where = InstanceProviderAPI.InstanceColumns._ID + "=?";
        String[] whereArgs = {instanceId};

        ContentValues contentValues = new ContentValues();
        contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
        contentValues.put(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT, getDisplaySubtext(event, context));

        instancesDao.updateInstance(contentValues, where, whereArgs);
    }

    public static String getDisplaySubtext(SmsRxEvent event, Context context) {
        Date date = event.getLastUpdated();
        SmsProgress progress = event.getProgress();

        if (event.getStatus() == null) {
            return null;
        }

        try {
            switch (event.getStatus()) {
                case NoReception:
                    return new SimpleDateFormat(context.getString(R.string.sms_no_reception), Locale.getDefault()).format(date);
                case AirplaneMode:
                    return new SimpleDateFormat(context.getString(R.string.sms_airplane_mode), Locale.getDefault()).format(date);
                case FatalError:
                    return new SimpleDateFormat(context.getString(R.string.sms_fatal_error), Locale.getDefault()).format(date);
                case Sending:
                    return context.getResources().getQuantityString(R.plurals.sms_sending, (int) progress.getTotalCount(), progress.getCompletedCount(), progress.getTotalCount());
                case Queued:
                    return context.getString(R.string.sms_submission_queued);
                case Sent:
                    return new SimpleDateFormat(context.getString(R.string.sms_sent_on_date_at_time),
                            Locale.getDefault()).format(date);
                case Delivered:
                    return new SimpleDateFormat(context.getString(R.string.sms_delivered_on_date_at_time),
                            Locale.getDefault()).format(date);
                case NotDelivered:
                    return new SimpleDateFormat(context.getString(R.string.sms_not_delivered_on_date_at_time),
                            Locale.getDefault()).format(date);
                case NoMessage:
                    return context.getString(R.string.sms_no_message);
                case Canceled:
                    return new SimpleDateFormat(context.getString(R.string.sms_last_submission_on_date_at_time),
                            Locale.getDefault()).format(date);
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e);
        }

        return "";
    }
}
