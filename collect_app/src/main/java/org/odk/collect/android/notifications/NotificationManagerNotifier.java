package org.odk.collect.android.notifications;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import org.odk.collect.android.R;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.preferences.keys.MetaKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.utilities.TranslationHandler;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.shared.Settings;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import static android.content.Context.NOTIFICATION_SERVICE;
import static java.util.stream.Collectors.toSet;

public class NotificationManagerNotifier implements Notifier {

    public static final String COLLECT_NOTIFICATION_CHANNEL = "collect_notification_channel";
    private final NotificationManager notificationManager;
    private final SettingsProvider settingsProvider;
    private final ProjectsRepository projectsRepository;
    private final FormUpdatesDownloadedNotificationBuilder formUpdatesDownloadedNotificationBuilder;
    private final FormsSyncFailedNotificationBuilder formsSyncFailedNotificationBuilder;
    private final FormUpdatesAvailableNotificationBuilder formUpdatesAvailableNotificationBuilder;
    private final FormsSubmissionNotificationBuilder formsSubmissionNotificationBuilder;

    public static final int FORM_UPDATE_NOTIFICATION_ID = 0;
    public static final int FORM_SYNC_NOTIFICATION_ID = 1;
    private static final int AUTO_SEND_RESULT_NOTIFICATION_ID = 1328974928;

    public NotificationManagerNotifier(Application application,
                                       SettingsProvider settingsProvider,
                                       ProjectsRepository projectsRepository,
                                       FormUpdatesDownloadedNotificationBuilder formUpdatesDownloadedNotificationBuilder,
                                       FormsSyncFailedNotificationBuilder formsSyncFailedNotificationBuilder,
                                       FormUpdatesAvailableNotificationBuilder formUpdatesAvailableNotificationBuilder,
                                       FormsSubmissionNotificationBuilder formsSubmissionNotificationBuilder) {
        notificationManager = (NotificationManager) application.getSystemService(NOTIFICATION_SERVICE);
        this.settingsProvider = settingsProvider;
        this.projectsRepository = projectsRepository;
        this.formUpdatesDownloadedNotificationBuilder = formUpdatesDownloadedNotificationBuilder;
        this.formsSyncFailedNotificationBuilder = formsSyncFailedNotificationBuilder;
        this.formUpdatesAvailableNotificationBuilder = formUpdatesAvailableNotificationBuilder;
        this.formsSubmissionNotificationBuilder = formsSubmissionNotificationBuilder;

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
    public void onUpdatesAvailable(List<ServerFormDetails> updates, String projectId) {
        Settings metaPrefs = settingsProvider.getMetaSettings();
        Set<String> updateId = updates.stream().map(f -> f.getFormId() + f.getHash() + (f.getManifest() != null ? f.getManifest().getHash() : null)).collect(toSet());
        if (metaPrefs.getStringSet(MetaKeys.LAST_UPDATED_NOTIFICATION).equals(updateId)) {
            return;
        }

        notificationManager.notify(FORM_UPDATE_NOTIFICATION_ID, formUpdatesAvailableNotificationBuilder.build(projectsRepository.get(projectId).getName()));

        metaPrefs.save(MetaKeys.LAST_UPDATED_NOTIFICATION, updateId);
    }

    @Override
    public void onUpdatesDownloaded(Map<ServerFormDetails, String> result, String projectId) {
        notificationManager.notify(FORM_UPDATE_NOTIFICATION_ID, formUpdatesDownloadedNotificationBuilder.build(result, projectsRepository.get(projectId).getName()));
    }

    @Override
    public void onSync(@Nullable FormSourceException exception, String projectId) {
        if (exception != null) {
            notificationManager.notify(FORM_SYNC_NOTIFICATION_ID, formsSyncFailedNotificationBuilder.build(exception, projectsRepository.get(projectId).getName()));
        } else {
            notificationManager.cancel(FORM_SYNC_NOTIFICATION_ID);
        }
    }

    @Override
    public void onSubmission(boolean failure, String message) {
        notificationManager.notify(AUTO_SEND_RESULT_NOTIFICATION_ID, formsSubmissionNotificationBuilder.build(failure, message));
    }
}
