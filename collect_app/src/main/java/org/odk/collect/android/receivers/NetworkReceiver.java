package org.odk.collect.android.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.utilities.IconUtils;
import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;
import org.odk.collect.android.utilities.InstanceUploaderUtils;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.InstanceGoogleSheetsUploader;
import org.odk.collect.android.tasks.InstanceServerUploader;
import org.odk.collect.android.utilities.PermissionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_UPLOADED_NOTIFICATION;

public class NetworkReceiver extends BroadcastReceiver implements InstanceUploaderListener {

    // turning on wifi often gets two CONNECTED events. we only want to run one thread at a time
    public static boolean running;
    InstanceServerUploader instanceServerUploader;

    InstanceGoogleSheetsUploader instanceGoogleSheetsUploader;

    private String resultMessage;

    @Override
    public void onReceive(Context context, Intent intent) {
        // make sure sd card is ready, if not don't try to send
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        String action = intent.getAction();
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        NetworkInfo currentNetworkInfo = manager.getActiveNetworkInfo();

        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
                || action.equals("org.odk.collect.android.FormSaved")) {
            if (currentNetworkInfo != null && currentNetworkInfo.isConnected()) {
                autoSendInstances(context, networkTypeMatchesAutoSendSetting(currentNetworkInfo));

                // If we just got connectivity, poll the server for form updates if we are due for it
                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    ServerPollingJob.pollServerIfNeeded();
                }
            }
        }
    }

    /**
     * Returns whether the currently-available connection type is included in the app-level auto-send
     * settings.
     *
     * @return true if a connection is available and settings specify it should trigger auto-send,
     * false otherwise.
     */
    private boolean networkTypeMatchesAutoSendSetting(NetworkInfo currentNetworkInfo) {
        if (currentNetworkInfo == null) {
            return false;
        }

        String autosend = (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_AUTOSEND);
        boolean sendwifi = autosend.equals("wifi_only");
        boolean sendnetwork = autosend.equals("cellular_only");
        if (autosend.equals("wifi_and_cellular")) {
            sendwifi = true;
            sendnetwork = true;
        }

        return currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
                && sendwifi || currentNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                && sendnetwork;
    }

    /**
     * If the app-level auto-send setting is enabled, send all finalized forms that don't specify not
     * to auto-send at the form level. If the app-level auto-send setting is disabled, send all
     * finalized forms that specify to send at the form level.
     *
     * @param isAutoSendAppSettingEnabled whether the auto-send option is enabled at the app level
     */
    private void autoSendInstances(Context context, boolean isAutoSendAppSettingEnabled) {
        if (!running) {
            running = true;

            List<Long> toUpload = getInstancesToAutoSend(isAutoSendAppSettingEnabled);

            if (toUpload.isEmpty()) {
                running = false;
                return;
            }

            Long[] toSendArray = new Long[toUpload.size()];
            toUpload.toArray(toSendArray);

            GeneralSharedPreferences settings = GeneralSharedPreferences.getInstance();
            String protocol = (String) settings.get(PreferenceKeys.KEY_PROTOCOL);

            if (protocol.equals(context.getString(R.string.protocol_google_sheets))) {
                sendInstancesToGoogleSheets(context, toSendArray);
            } else if (protocol.equals(context.getString(R.string.protocol_odk_default))) {
                instanceServerUploader = new InstanceServerUploader();
                instanceServerUploader.setUploaderListener(this);
                instanceServerUploader.execute(toSendArray);
            }
        }
    }

    private void sendInstancesToGoogleSheets(Context context, Long[] toSendArray) {
        if (PermissionUtils.checkIfGetAccountsPermissionGranted(context)) {
            GoogleAccountsManager accountsManager = new GoogleAccountsManager(Collect.getInstance());

            String googleUsername = accountsManager.getSelectedAccount();
            if (googleUsername.isEmpty()) {
                // just quit if there's no username
                running = false;
                return;
            }
            accountsManager.getCredential().setSelectedAccountName(googleUsername);
            instanceGoogleSheetsUploader = new InstanceGoogleSheetsUploader(accountsManager);
            instanceGoogleSheetsUploader.setUploaderListener(this);
            instanceGoogleSheetsUploader.execute(toSendArray);
        } else {
            resultMessage = Collect.getInstance().getString(R.string.odk_permissions_fail);
            uploadingComplete(null);
        }
    }

    /**
     * Returns a list of longs representing the database ids of the instances that need to be
     * autosent.
     */
    @NonNull
    private List<Long> getInstancesToAutoSend(boolean isAutoSendAppSettingEnabled) {
        List<Long> toUpload = new ArrayList<>();
        Cursor c = new InstancesDao().getFinalizedInstancesCursor();

        try {
            if (c != null && c.getCount() > 0) {
                c.move(-1);
                String formId;
                while (c.moveToNext()) {
                    formId = c.getString(c.getColumnIndex(InstanceColumns.JR_FORM_ID));
                    if (formShouldBeAutoSent(formId, isAutoSendAppSettingEnabled)) {
                        Long l = c.getLong(c.getColumnIndex(InstanceColumns._ID));
                        toUpload.add(l);
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return toUpload;
    }

    /**
     * Returns whether a form with the specified form_id should be auto-sent given the current
     * app-level auto-send settings. Returns false if there is no form with the specified form_id.
     *
     * A form should be auto-sent if auto-send is on at the app level AND this form doesn't override
     * auto-send settings OR if auto-send is on at the form-level.
     *
     * @param isAutoSendAppSettingEnabled whether the auto-send option is enabled at the app level
     */
    private boolean formShouldBeAutoSent(String jrFormId, boolean isAutoSendAppSettingEnabled) {
        Cursor cursor = new FormsDao().getFormsCursorForFormId(jrFormId);
        String formLevelAutoSend = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int autoSendColumnIndex = cursor.getColumnIndex(AUTO_SEND);
                formLevelAutoSend = cursor.getString(autoSendColumnIndex);
            } finally {
                cursor.close();
            }
        }

        return formLevelAutoSend == null ? isAutoSendAppSettingEnabled
                : Boolean.valueOf(formLevelAutoSend);
    }

    @Override
    public void uploadingComplete(HashMap<String, String> resultMessagesByInstanceId) {
        // task is done
        if (instanceServerUploader != null) {
            instanceServerUploader.setUploaderListener(null);
        }
        if (instanceGoogleSheetsUploader != null) {
            instanceGoogleSheetsUploader.setUploaderListener(null);
        }
        running = false;

        String message = formatOverallResultMessage(resultMessagesByInstanceId);

        showUploadStatusNotification(resultMessagesByInstanceId, message);
    }

    private String formatOverallResultMessage(HashMap<String, String> resultMessagesByInstanceId) {
        String message;

        if (resultMessagesByInstanceId == null) {
            message = resultMessage != null
                    ? resultMessage
                    : Collect.getInstance().getString(R.string.odk_auth_auth_fail);
        } else {
            StringBuilder selection = new StringBuilder();
            Set<String> keys = resultMessagesByInstanceId.keySet();
            Iterator<String> it = keys.iterator();

            String[] selectionArgs = new String[keys.size()];
            int i = 0;
            while (it.hasNext()) {
                String id = it.next();
                selection.append(InstanceColumns._ID + "=?");
                selectionArgs[i++] = id;
                if (i != keys.size()) {
                    selection.append(" or ");
                }
            }

            Cursor cursor = new InstancesDao().getInstancesCursor(selection.toString(), selectionArgs);
            message = InstanceUploaderUtils.getUploadResultMessage(cursor, resultMessagesByInstanceId);
        }
        return message;
    }

    private void showUploadStatusNotification(HashMap<String, String> resultMessagesByInstanceId, String message) {
        Intent notifyIntent = new Intent(Collect.getInstance(), NotificationActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_TITLE, Collect.getInstance().getString(R.string.upload_results));
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, message.trim());

        PendingIntent pendingNotify = PendingIntent.getActivity(Collect.getInstance(), FORMS_UPLOADED_NOTIFICATION,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(Collect.getInstance())
                .setSmallIcon(IconUtils.getNotificationAppIcon())
                .setContentTitle(Collect.getInstance().getString(R.string.odk_auto_note))
                .setContentIntent(pendingNotify)
                .setContentText(getContentText(resultMessagesByInstanceId))
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) Collect.getInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1328974928, builder.build());
    }

    private String getContentText(Map<String, String> resultsMessagesByInstanceId) {
        return resultsMessagesByInstanceId != null && allFormsUploadedSuccessfully(resultsMessagesByInstanceId)
                ? Collect.getInstance().getString(R.string.success)
                : Collect.getInstance().getString(R.string.failures);
    }

    /**
     * Uses the messages returned for each finalized form that was attempted to be sent to determine
     * whether all forms were successfully sent.
     *
     * TODO: Verify that this works with localization and that there really are no other messages
     * that can indicate success (e.g. a custom server message).
     */
    private boolean allFormsUploadedSuccessfully(Map<String, String> resultsMessagesByInstanceId) {
        for (String formId : resultsMessagesByInstanceId.keySet()) {
            String formResultMessage = resultsMessagesByInstanceId.get(formId);
            if (!formResultMessage.equals(InstanceUploaderUtils.DEFAULT_SUCCESSFUL_TEXT)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void progressUpdate(int progress, int total) {
        // do nothing
    }

    @Override
    public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
        // if we get an auth request, just fail
        if (instanceServerUploader != null) {
            instanceServerUploader.setUploaderListener(null);
        }
        if (instanceGoogleSheetsUploader != null) {
            instanceGoogleSheetsUploader.setUploaderListener(null);
        }
        running = false;
    }
}
