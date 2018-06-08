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
import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SentMessageResult;
import org.odk.collect.android.tasks.sms.models.SmsProgress;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import static org.odk.collect.android.tasks.sms.Mapper.toMessageStatus;
import static org.odk.collect.android.tasks.sms.SmsUtils.checkIfSubmissionSentOrSending;

public class SmsNotificationReceiver extends BroadcastReceiver {
    public static final String SMS_MESSAGE_RESULT = "sms_message_result";
    public static final String SMS_NOTIFICATION_ACTION = "org.odk.collect.android.COLLECT_SMS_NOTIFICATION_ACTION";
    public static final String SMS_NOTIFICATION_GROUP = "org.odk.collect.android.COLLECT_SMS_NOTIFICATION_GROUP";
    private static final int SUMMARY_ID = 4324;

    @Inject
    SmsSubmissionManagerContract submissionManagerContract;

    private SmsSubmission smsSubmission;
    private SentMessageResult messageResult;
    private NotificationManagerCompat notificationManager;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Collect.getInstance().getComponent().inject(this);

        this.context = context;
        messageResult = intent.getExtras().getParcelable(SMS_MESSAGE_RESULT);
        smsSubmission = submissionManagerContract.getSubmissionModel(messageResult.getInstanceId());
        notificationManager = NotificationManagerCompat.from(context);

        sendBundledNotification();
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
     * Creates a summary notification that's shown there are multiple submissions taking place.
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

    private String getContentText() {
        Date date = smsSubmission.getLastUpdated();
        SmsProgress progress = smsSubmission.getCompletion();

        MessageStatus status = checkIfSubmissionSentOrSending(smsSubmission, toMessageStatus(messageResult.getMessageResultStatus()));

        switch (status) {
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

        return "";
    }
}
