package org.odk.collect.android.tasks.sms;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.InstanceUploaderList;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.SmsProgress;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

import java.util.Date;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.tasks.sms.SmsSender.SMS_INSTANCE_ID;
import static org.odk.collect.android.tasks.sms.SmsSender.SMS_RESULT_CODE;

public class SmsNotificationReceiver extends BroadcastReceiver {
    public static final String SMS_NOTIFICATION_ACTION = "org.odk.collect.android.COLLECT_SMS_NOTIFICATION_ACTION";
    public static final String SMS_NOTIFICATION_GROUP = "org.odk.collect.android.COLLECT_SMS_NOTIFICATION_GROUP";
    private static final int SUMMARY_ID = 4324;
    private int resultCode;

    @Inject
    SmsSubmissionManagerContract submissionManagerContract;

    private SmsSubmission smsSubmission;
    private NotificationManagerCompat notificationManager;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Collect.getInstance().getComponent().inject(this);

        this.context = context;

        if (intent.getExtras() == null) {
            Timber.e("getExtras returned a null value.");
            return;
        }

        String instanceId = intent.getExtras().getString(SMS_INSTANCE_ID);
        resultCode = intent.getExtras().getInt(SMS_RESULT_CODE);
        smsSubmission = submissionManagerContract.getSubmissionModel(instanceId);
        notificationManager = NotificationManagerCompat.from(context);

        sendBundledNotification();
        deleteIfSubmissionCompleted();
    }

    void sendBundledNotification() {
        Notification notification = buildNotification();
        notificationManager.notify(smsSubmission.getNotificationId(), notification);
        Notification summary = buildSummary();
        notificationManager.notify(SUMMARY_ID, summary);
    }

    private Notification buildNotification() {

        Intent intent = new Intent(context, InstanceUploaderList.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context)
                .setContentTitle(smsSubmission.getDisplayName())
                .setContentText(getContentText())
                .setWhen(smsSubmission.getLastUpdated().getTime())
                .setSmallIcon(R.drawable.ic_message_text_outline_white_24dp)
                .setContentIntent(contentIntent)
                .setShowWhen(true)
                .setGroup(SMS_NOTIFICATION_GROUP)
                .build();
    }

    /***
     * Creates a summary notification that's shown when there are multiple submissions taking place.
     * Will create a collapsed notification list on compatible versions of Android.
     * @return Notification
     */
    private Notification buildSummary() {
        Intent intent = new Intent(context, InstanceUploaderList.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.sms_submissions_notif_title))
                .setContentText(context.getString(R.string.sms_submissions_notif_description))
                .setWhen(smsSubmission.getLastUpdated().getTime())
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_message_text_outline_white_24dp)
                .setShowWhen(true)
                .setGroup(SMS_NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .build();
    }

    /**
     * Once the submission is completed, it's deleted here since this is the final point
     * at which it's data is needed.
     */
    private void deleteIfSubmissionCompleted() {
        if (smsSubmission.isSubmissionComplete()) {
            submissionManagerContract.forgetSubmission(smsSubmission.getInstanceId());
        }
    }

    private String getContentText() {
        Date date = smsSubmission.getLastUpdated();
        SmsProgress progress = smsSubmission.getCompletion();

        return SmsService.getDisplaySubtext(resultCode, date, progress, context);
    }
}
