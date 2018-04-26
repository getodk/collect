package org.odk.collect.android.tasks.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class SmsPendingIntents {

    public static final String SMS_SEND_ACTION = "org.odk.collect.android.COLLECT_SMS_SEND_ACTION";
    public static final String SMS_INSTANCE_ID = "COLLECT_SMS_INSTANCE_ID";
    public static final String SMS_MESSAGE_ID = "COLLECT_SMS_MESSAGE_ID";

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
     * Recreates the intent that was passed to SMS Manager to check if it exists.
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
}
