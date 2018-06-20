package org.odk.collect.android.tasks.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;

public class SmsUtils {

    public static final String SMS_SEND_ACTION = "org.odk.collect.android.COLLECT_SMS_SEND_ACTION";
    public static final String SMS_DELIVERY_ACTION = "org.odk.collect.android.COLLECT_SMS_DELIVERY_ACTION";
    public static final String SMS_INSTANCE_ID = "COLLECT_SMS_INSTANCE_ID";
    public static final String SMS_MESSAGE_ID = "COLLECT_SMS_MESSAGE_ID";

    private SmsUtils() {

    }

    /***
     * Create the intent that's passed to SMS Manager so that the Sent SMS
     * BroadcastReceiver can be triggered.
     * @param context necessary to create pending intent.
     * @param instanceId identifies the instance of the form being targeted.
     * @param messageId identifies the specific messages this intent applies to.
     * @return PendingIntent
     */

    public static PendingIntent getSentPendingIntent(Context context, String instanceId, int messageId) {
        Intent sendIntent = new Intent(SMS_SEND_ACTION);
        sendIntent.putExtra(SMS_INSTANCE_ID, instanceId);
        sendIntent.putExtra(SMS_MESSAGE_ID, messageId);

        return PendingIntent.getBroadcast(context, messageId, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    /***
     * Create the intent that's passed to SMS Manager so that the Delivery SMS
     * BroadcastReceiver can be triggered.
     * @param context necessary to create pending intent.
     * @param instanceId identifies the instance of the form being targeted.
     * @param messageId identifies the specific messages this intent applies to.
     * @return PendingIntent
     */

    public static PendingIntent getDeliveryPendingIntent(Context context, String instanceId, int messageId) {
        Intent sendIntent = new Intent(SMS_DELIVERY_ACTION);
        sendIntent.putExtra(SMS_INSTANCE_ID, instanceId);
        sendIntent.putExtra(SMS_MESSAGE_ID, messageId);

        return PendingIntent.getBroadcast(context, messageId, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    /***
     * Recreates the sent intent that was passed to SMS Manager to check if it exists.
     * Usefully in cases such as device restarts.
     * @param context necessary to create pending intent.
     * @param instanceId identifies the instance of the form being targeted.
     * @param messageId identifies the specific messages this intent applies to.
     * @return true if it exists.
     */
    public static boolean checkIfSentIntentExists(Context context, String instanceId, int messageId) {
        Intent sendIntent = new Intent(SMS_SEND_ACTION);
        sendIntent.putExtra(SMS_INSTANCE_ID, instanceId);
        sendIntent.putExtra(SMS_MESSAGE_ID, messageId);

        return PendingIntent.getBroadcast(context, messageId, sendIntent, PendingIntent.FLAG_NO_CREATE) != null;
    }

    /***
     * Recreates the delivery intent that was passed to SMS Manager to check if it exists.
     * Usefully in cases such as device restarts.
     * @param context necessary to create pending intent.
     * @param instanceId identifies the instance of the form being targeted.
     * @param messageId identifies the specific messages this intent applies to.
     * @return true if it exists.
     */
    public static boolean checkIfDeliveryIntentExists(Context context, String instanceId, int messageId) {
        Intent sendIntent = new Intent(SMS_DELIVERY_ACTION);
        sendIntent.putExtra(SMS_INSTANCE_ID, instanceId);
        sendIntent.putExtra(SMS_MESSAGE_ID, messageId);

        return PendingIntent.getBroadcast(context, messageId, sendIntent, PendingIntent.FLAG_NO_CREATE) != null;
    }

    /***
     * Checks to see if the current message is the last or not. If it's not the last it sends
     * Sending as the status to the InstanceUploaderList so that it shows the current progress but if it's the last
     * message then that means the submission has been completed so it should show Sent. This has to be done
     * because the MessageStatus is being used to serve SmsSender Layer and it's status is also tied to the UI.
     *
     * @param model The current model that's being submitted.
     * @param status that was received from the broadcast receiver of the message that was just sent.
     * @return the MessageStatus that will be transferred via the event.
     */
    public static MessageStatus checkIfSubmissionSentOrSending(SmsSubmission model, MessageStatus status) {
        if (status.equals(MessageStatus.Sent)) {
            if (model.isSubmissionComplete()) {
                return MessageStatus.Sent;
            } else {
                return MessageStatus.Sending;
            }
        }

        return status;
    }

}
