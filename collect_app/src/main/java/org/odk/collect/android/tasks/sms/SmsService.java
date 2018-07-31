package org.odk.collect.android.tasks.sms;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.SmsManager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.android.gms.analytics.HitBuilders;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
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
import org.odk.collect.android.tasks.sms.models.SmsProgress;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.odk.collect.android.utilities.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE;
import static android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF;
import static org.odk.collect.android.tasks.sms.SmsNotificationReceiver.SMS_NOTIFICATION_ACTION;
import static org.odk.collect.android.tasks.sms.SmsSender.SMS_INSTANCE_ID;
import static org.odk.collect.android.tasks.sms.SmsSender.SMS_MESSAGE_ID;
import static org.odk.collect.android.tasks.sms.SmsSender.SMS_RESULT_CODE;
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

    private static final int RESULT_ENCRYPTED = 100;
    public static final int RESULT_QUEUED = 101;
    private static final int RESULT_NO_MESSAGE = 102;
    public static final int RESULT_OK_OTHERS_PENDING = 103;
    private static final int RESULT_FILE_ERROR = 104;
    public static final int RESULT_INVALID_GATEWAY = 105;
    public static final int RESULT_MESSAGE_READY = 106;
    private static final int RESULT_SUBMISSION_CANCELED = 107;
    public static final int RESULT_SENDING = 108;

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
                    String instanceId = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));
                    String displayName = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
                    String formId = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                    String formVersion = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION));

                    FormInfo info = new FormInfo(filePath, formId, formVersion);

                    submitForm(instanceId, info, displayName);
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
            SmsRxEvent event = new SmsRxEvent(instanceId, RESULT_ENCRYPTED);
            updateInstanceStatusFailedText(instanceId, event);
            rxEventBus.post(event);
            return false;
        }

        File smsFile = new File(getSmsInstancePath(info.getInstancePath()));

        /**
         * No instance.txt file is generated if a form doesn't have sms tags.
         */
        if (!smsFile.exists()) {
            SmsRxEvent event = new SmsRxEvent(instanceId, RESULT_NO_MESSAGE);
            updateInstanceStatusFailedText(instanceId, event);
            rxEventBus.post(event);
            return false;
        }

        try {
            text = getFileContents(smsFile);
        } catch (IOException e) {

            SmsRxEvent event = new SmsRxEvent(instanceId, RESULT_FILE_ERROR);
            updateInstanceStatusFailedText(instanceId, event);
            rxEventBus.post(event);
            Timber.e(e);
            return false;
        }

        SmsSubmission model = smsSubmissionManager.getSubmissionModel(instanceId);

        /*
         * If the model exists that means this instance was probably sent in the past.
         */
        if (model != null) {
            /*
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
                    startSendMessagesJob(instanceId);
                    break;
                }
            }
        } else {
            final List<String> parts = smsManager.divideMessage(text);

            model = new SmsSubmission();
            model.setNotificationId();
            model.setInstanceId(instanceId);
            model.setLastUpdated(new Date());
            model.setDisplayName(displayName);

            List<Message> messages = new ArrayList<>();

            for (String part : parts) {

                Message message = new Message();
                message.setPartNumber(parts.indexOf(part) + 1);
                message.setText(part);
                message.setResultCode(RESULT_MESSAGE_READY);
                message.generateRandomMessageID();

                messages.add(message);
            }

            model.setMessages(messages);

            smsSubmissionManager.saveSubmission(model);

            startSendMessagesJob(instanceId);
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
        event.setResultCode(RESULT_SUBMISSION_CANCELED);

        rxEventBus.post(event);

        return true;
    }

    /***
     * Receives a model that contains the information received by the intent of the
     * SmsSentBroadcastReceiver that's triggered when a message was sent.
     * This function then determines the next action to perform based on the result it receives.
     */
    void processMessageSentResult(String instanceId, int messageId, int resultCode) {

        String log = String.format(Locale.getDefault(), "Received result from broadcast receiver of instance id %s with message id of %d", instanceId, messageId);
        Timber.i(log);

        smsSubmissionManager.updateMessageStatus(resultCode, instanceId, messageId);

        SmsSubmission model = smsSubmissionManager.getSubmissionModel(instanceId);

        resultCode = model.validateResultCode(resultCode);

        SmsRxEvent event = new SmsRxEvent();
        event.setInstanceId(instanceId);
        event.setLastUpdated(model.getLastUpdated());
        event.setResultCode(resultCode);
        event.setProgress(model.getCompletion());

        rxEventBus.post(event);

        Intent notificationIntent = new Intent(SMS_NOTIFICATION_ACTION);
        notificationIntent.putExtra(SMS_MESSAGE_ID, messageId);
        notificationIntent.putExtra(SMS_INSTANCE_ID, instanceId);
        notificationIntent.putExtra(SMS_RESULT_CODE, resultCode);
        context.sendOrderedBroadcast(notificationIntent, null);

        /*
         * If the submission has completed then the instance is marked as submitted.
         * If not and the message isn't still sending then that means an error occurred
         * so that error message is persisted along with SUBMISSION_STATUS_FAILED
         * */
        if (model.isSubmissionComplete()) {
            markInstanceAsSubmittedOrDelete(instanceId, model.getCompletion(), model.getLastUpdated(), context);
        } else if (resultCode != RESULT_OK && resultCode != RESULT_OK_OTHERS_PENDING) {
            updateInstanceStatusFailedText(instanceId, event);
        }
    }

    /***
     * Adds a message as a background job so that it can be sent when necessary.
     * A single message is sent at a time so that progress can be easily tracked and the
     * persisting of form submission's state is more congruent with the amount of message parts.
     *
     * @param instanceId from instanceDao
     */
    protected void startSendMessagesJob(String instanceId) {
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
        rxEventBus.post(new SmsRxEvent(instanceId, RESULT_QUEUED));
    }

    private void markInstanceAsSubmittedOrDelete(String instanceId, SmsProgress progress, Date date, Context context) {
        String where = InstanceProviderAPI.InstanceColumns._ID + "=?";
        String[] whereArgs = {instanceId};

        ContentValues contentValues = new ContentValues();
        contentValues.put(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT, getDisplaySubtext(RESULT_OK, date, progress, context));
        contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);

        Collect.getInstance()
                .getDefaultTracker()
                .send(new HitBuilders.EventBuilder()
                        .setCategory("Submission")
                        .setAction("SMS")
                        .build());

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
        contentValues.put(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT, getDisplaySubtext(event.getResultCode(), event.getLastUpdated(), event.getProgress(), context));

        instancesDao.updateInstance(contentValues, where, whereArgs);
    }

    public static String getDisplaySubtext(int resultCode, Date date, SmsProgress progress, Context context) {
        if (date == null) {
            Timber.e("date is null");
            return context.getString(R.string.error_occured);
        }

        try {
            switch (resultCode) {
                case RESULT_ERROR_NO_SERVICE:
                    return new SimpleDateFormat(context.getString(R.string.sms_no_reception), Locale.getDefault()).format(date);
                case RESULT_ERROR_RADIO_OFF:
                    return new SimpleDateFormat(context.getString(R.string.sms_airplane_mode), Locale.getDefault()).format(date);
                case RESULT_OK_OTHERS_PENDING:
                    return context.getResources().getQuantityString(R.plurals.sms_sending, (int) progress.getTotalCount(), progress.getCompletedCount(), progress.getTotalCount());
                case RESULT_QUEUED:
                    return context.getString(R.string.sms_submission_queued);
                case RESULT_OK:
                    return new SimpleDateFormat(context.getString(R.string.sms_sent_on_date_at_time),
                            Locale.getDefault()).format(date);
                case RESULT_NO_MESSAGE:
                    return context.getString(R.string.sms_no_message);
                case RESULT_SUBMISSION_CANCELED:
                    return new SimpleDateFormat(context.getString(R.string.sms_last_submission_on_date_at_time),
                            Locale.getDefault()).format(date);
                case RESULT_ENCRYPTED:
                    return context.getString(R.string.sms_encrypted_message);

                default:
                    return new SimpleDateFormat(context.getString(R.string.sms_fatal_error), Locale.getDefault()).format(date);
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e);
        }

        return "";
    }
}
