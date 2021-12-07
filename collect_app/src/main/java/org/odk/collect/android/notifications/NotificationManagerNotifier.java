package org.odk.collect.android.notifications;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormDownloadListActivity;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.preferences.keys.MetaKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.utilities.IconUtils;
import org.odk.collect.android.utilities.TranslationHandler;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.shared.Settings;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import static android.content.Context.NOTIFICATION_SERVICE;
import static java.util.stream.Collectors.toSet;
import static org.odk.collect.android.activities.FormDownloadListActivity.DISPLAY_ONLY_UPDATED_FORMS;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_UPLOADED_NOTIFICATION;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORM_UPDATES_AVAILABLE_NOTIFICATION;

public class NotificationManagerNotifier implements Notifier {

    public static final String COLLECT_NOTIFICATION_CHANNEL = "collect_notification_channel";
    private final Application application;
    private final NotificationManager notificationManager;
    private final SettingsProvider settingsProvider;
    private final FormUpdatesDownloadedNotificationBuilder formUpdatesDownloadedNotificationBuilder;
    private final FormsSyncFailedNotificationBuilder formsSyncFailedNotificationBuilder;

    public static final int FORM_UPDATE_NOTIFICATION_ID = 0;
    public static final int FORM_SYNC_NOTIFICATION_ID = 1;
    private static final int AUTO_SEND_RESULT_NOTIFICATION_ID = 1328974928;

    public NotificationManagerNotifier(Application application,
                                       SettingsProvider settingsProvider,
                                       FormUpdatesDownloadedNotificationBuilder formUpdatesDownloadedNotificationBuilder,
                                       FormsSyncFailedNotificationBuilder formsSyncFailedNotificationBuilder) {
        this.application = application;
        notificationManager = (NotificationManager) application.getSystemService(NOTIFICATION_SERVICE);
        this.settingsProvider = settingsProvider;
        this.formUpdatesDownloadedNotificationBuilder = formUpdatesDownloadedNotificationBuilder;
        this.formsSyncFailedNotificationBuilder = formsSyncFailedNotificationBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(new NotificationChannel(
                        COLLECT_NOTIFICATION_CHANNEL,
                        TranslationHandler.getString(application, R.string.notification_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT)
                );
            }
        }
    }

    @Override
    public void onUpdatesAvailable(List<ServerFormDetails> updates) {
        Settings metaPrefs = settingsProvider.getMetaSettings();
        Set<String> updateId = updates.stream().map(f -> f.getFormId() + f.getHash() + (f.getManifest() != null ? f.getManifest().getHash() : null)).collect(toSet());
        if (metaPrefs.getStringSet(MetaKeys.LAST_UPDATED_NOTIFICATION).equals(updateId)) {
            return;
        }

        Intent intent = new Intent(application, FormDownloadListActivity.class);
        intent.putExtra(DISPLAY_ONLY_UPDATED_FORMS, true);
        PendingIntent contentIntent = PendingIntent.getActivity(application, FORM_UPDATES_AVAILABLE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(application, COLLECT_NOTIFICATION_CHANNEL)
                .setContentIntent(contentIntent)
                .setContentTitle(TranslationHandler.getString(application, R.string.form_updates_available))
                .setContentText(null)
                .setSmallIcon(IconUtils.getNotificationAppIcon())
                .setAutoCancel(true);

        notificationManager.notify(FORM_UPDATE_NOTIFICATION_ID, builder.build());

        metaPrefs.save(MetaKeys.LAST_UPDATED_NOTIFICATION, updateId);
    }

    @Override
    public void onUpdatesDownloaded(Map<ServerFormDetails, String> result, String projectId) {
        notificationManager.notify(FORM_UPDATE_NOTIFICATION_ID, formUpdatesDownloadedNotificationBuilder.build(result, projectId));
    }

    @Override
    public void onSync(@Nullable FormSourceException exception, String projectId) {
        if (exception != null) {
            notificationManager.notify(FORM_SYNC_NOTIFICATION_ID, formsSyncFailedNotificationBuilder.build(exception, projectId));
        } else {
            notificationManager.cancel(FORM_SYNC_NOTIFICATION_ID);
        }
    }

    @Override
    public void onSubmission(boolean failure, String message) {
        Intent notifyIntent = new Intent(application, NotificationActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_TITLE, TranslationHandler.getString(application, R.string.upload_results));
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, message.trim());

        PendingIntent pendingNotify = PendingIntent.getActivity(application, FORMS_UPLOADED_NOTIFICATION,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String content = failure
                ? TranslationHandler.getString(application, R.string.failures)
                : TranslationHandler.getString(application, R.string.success);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(application, COLLECT_NOTIFICATION_CHANNEL)
                .setContentIntent(pendingNotify)
                .setContentTitle(TranslationHandler.getString(application, R.string.odk_auto_note))
                .setContentText(content)
                .setSmallIcon(IconUtils.getNotificationAppIcon())
                .setAutoCancel(true);

        notificationManager.notify(AUTO_SEND_RESULT_NOTIFICATION_ID, builder.build());
    }
}
