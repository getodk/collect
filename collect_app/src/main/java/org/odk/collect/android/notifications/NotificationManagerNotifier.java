package org.odk.collect.android.notifications;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FillBlankFormActivity;
import org.odk.collect.android.activities.FormDownloadListActivity;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.formmanagement.FormApiExceptionMapper;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.preferences.MetaKeys;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.utilities.NotificationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import static android.content.Context.NOTIFICATION_SERVICE;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.odk.collect.android.activities.FormDownloadListActivity.DISPLAY_ONLY_UPDATED_FORMS;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_DOWNLOADED_NOTIFICATION;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_UPLOADED_NOTIFICATION;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORM_UPDATES_AVAILABLE_NOTIFICATION;
import static org.odk.collect.android.utilities.NotificationUtils.showNotification;

public class NotificationManagerNotifier implements Notifier {

    private final Application application;
    private final NotificationManager notificationManager;
    private final PreferencesProvider preferencesProvider;

    private static final int FORM_UPDATE_NOTIFICATION_ID = 0;
    private static final int FORM_SYNC_NOTIFICATION_ID = 1;
    private static final int AUTO_SEND_RESULT_NOTIFICATION_ID = 1328974928;

    public NotificationManagerNotifier(Application application, PreferencesProvider preferencesProvider) {
        this.application = application;
        notificationManager = (NotificationManager) application.getSystemService(NOTIFICATION_SERVICE);
        this.preferencesProvider = preferencesProvider;
    }

    @Override
    public void onUpdatesAvailable(List<ServerFormDetails> updates) {
        SharedPreferences metaPrefs = preferencesProvider.getMetaSharedPreferences();
        Set<String> updateId = updates.stream().map(f -> f.getFormId() + f.getHash() + f.getManifestFileHash()).collect(toSet());
        if (metaPrefs.getStringSet(MetaKeys.LAST_UPDATED_NOTIFICATION, emptySet()).equals(updateId)) {
            return;
        }

        Intent intent = new Intent(application, FormDownloadListActivity.class);
        intent.putExtra(DISPLAY_ONLY_UPDATED_FORMS, true);
        PendingIntent contentIntent = PendingIntent.getActivity(application, FORM_UPDATES_AVAILABLE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Resources localizedResources = getLocalizedResources(application);
        showNotification(
                application,
                notificationManager,
                localizedResources.getString(R.string.form_updates_available),
                null,
                contentIntent,
                FORM_UPDATE_NOTIFICATION_ID
        );

        metaPrefs.edit()
                .putStringSet(MetaKeys.LAST_UPDATED_NOTIFICATION, updateId)
                .apply();
    }

    @Override
    public void onUpdatesDownloaded(HashMap<ServerFormDetails, String> result) {
        Resources localizedResources = getLocalizedResources(application);

        Intent intent = new Intent(application, NotificationActivity.class);
        intent.putExtra(NotificationActivity.NOTIFICATION_TITLE, localizedResources.getString(R.string.download_forms_result));
        intent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, FormDownloadListActivity.getDownloadResultMessage(result));
        PendingIntent contentIntent = PendingIntent.getActivity(application, FORMS_DOWNLOADED_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        showNotification(
                application,
                notificationManager,
                localizedResources.getString(R.string.odk_auto_download_notification_title),
                localizedResources.getString(allFormsDownloadedSuccessfully(application, result) ?
                        R.string.success :
                        R.string.failures),
                contentIntent,
                FORM_UPDATE_NOTIFICATION_ID
        );
    }

    @Override
    public void onSync(@Nullable FormApiException exception) {
        if (exception != null) {
            Intent intent = new Intent(application, FillBlankFormActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(application, FORM_SYNC_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Resources localizedResources = getLocalizedResources(application);
            showNotification(
                    application,
                    notificationManager,
                    localizedResources.getString(R.string.form_update_error),
                    new FormApiExceptionMapper(localizedResources).getMessage(exception),
                    contentIntent,
                    FORM_SYNC_NOTIFICATION_ID
            );
        } else {
            notificationManager.cancel(FORM_SYNC_NOTIFICATION_ID);
        }
    }

    @Override
    public void onSubmission(boolean failure, String message) {
        Resources localizedResources = getLocalizedResources(application);

        Intent notifyIntent = new Intent(application, NotificationActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_TITLE, localizedResources.getString(R.string.upload_results));
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, message.trim());

        PendingIntent pendingNotify = PendingIntent.getActivity(application, FORMS_UPLOADED_NOTIFICATION,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationUtils.showNotification(
                application, (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE), localizedResources.getString(R.string.odk_auto_note), failure ? localizedResources.getString(R.string.failures)
                        : localizedResources.getString(R.string.success), pendingNotify, AUTO_SEND_RESULT_NOTIFICATION_ID
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

    // The application context will give us the system's locale
    private Resources getLocalizedResources(Context context) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(new Locale(LocaleHelper.getLocaleCode(context)));
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }
}
