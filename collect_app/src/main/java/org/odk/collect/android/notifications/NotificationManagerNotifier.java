package org.odk.collect.android.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormChooserListActivity;
import org.odk.collect.android.activities.FormDownloadListActivity;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.matchexactly.SyncException;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;
import static org.odk.collect.android.activities.FormDownloadListActivity.DISPLAY_ONLY_UPDATED_FORMS;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_DOWNLOADED_NOTIFICATION;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORM_UPDATES_AVAILABLE_NOTIFICATION;
import static org.odk.collect.android.utilities.NotificationUtils.showNotification;

public class NotificationManagerNotifier implements Notifier {

    private final Context context;
    private final NotificationManager notificationManager;

    private static final int FORM_UPDATE_NOTIFICATION_ID = 0;
    private static final int FORM_SYNC_NOTIFICATION_ID = 1;

    public NotificationManagerNotifier(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onUpdatesAvailable() {
        Intent intent = new Intent(context, FormDownloadListActivity.class);
        intent.putExtra(DISPLAY_ONLY_UPDATED_FORMS, true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, FORM_UPDATES_AVAILABLE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        showNotification(
                context,
                notificationManager,
                R.string.form_updates_available,
                null,
                contentIntent,
                FORM_UPDATE_NOTIFICATION_ID
        );
    }

    @Override
    public void onUpdatesDownloaded(HashMap<ServerFormDetails, String> result) {
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra(NotificationActivity.NOTIFICATION_TITLE, context.getString(R.string.download_forms_result));
        intent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, FormDownloadListActivity.getDownloadResultMessage(result));
        PendingIntent contentIntent = PendingIntent.getActivity(context, FORMS_DOWNLOADED_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        showNotification(
                context,
                notificationManager,
                R.string.odk_auto_download_notification_title,
                context.getString(allFormsDownloadedSuccessfully(context, result) ?
                        R.string.success :
                        R.string.failures),
                contentIntent,
                FORM_UPDATE_NOTIFICATION_ID
        );
    }

    @Override
    public void onSyncFailure(SyncException exception) {
        Intent intent = new Intent(context, FormChooserListActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, FORM_SYNC_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        showNotification(
                context,
                notificationManager,
                R.string.sync_failure,
                new SyncExceptionMapper().getMessage(exception),
                contentIntent,
                FORM_SYNC_NOTIFICATION_ID
        );
    }

    private boolean allFormsDownloadedSuccessfully(Context context, HashMap<ServerFormDetails, String> result) {
        for (Map.Entry<ServerFormDetails, String> item : result.entrySet()) {
            if (!item.getValue().equals(context.getString(R.string.success))) {
                return false;
            }
        }
        return true;
    }
}
