package org.odk.collect.android.upload;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.http.HttpClientConnection;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.upload.result.SubmissionUploadResult;
import org.odk.collect.android.utilities.IconUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.android.utilities.InstanceUploaderUtils;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.InstanceGoogleSheetsUploaderTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_UPLOADED_NOTIFICATION;

public class AutoSendWorker extends Worker {
    private static final int AUTO_SEND_RESULT_NOTIFICATION_ID = 1328974928;
    InstanceGoogleSheetsUploaderTask instanceGoogleSheetsUploaderTask;

    private String resultMessage;

    public AutoSendWorker(@NonNull Context c, @NonNull WorkerParameters parameters) {
        super(c, parameters);
    }

    /**
     * If the app-level auto-send setting is enabled, send all finalized forms that don't specify not
     * to auto-send at the form level. If the app-level auto-send setting is disabled, send all
     * finalized forms that specify to send at the form level.
     *
     * Fails immediately if:
     *   - storage isn't ready
     *   - the network type that toggled on is not the desired type AND no form specifies auto-send
     *
     * If the network type doesn't match the auto-send settings, retry next time a connection is
     * available.
     *
     *  TODO: this is where server polling used to happen; need to bring it back elsewhere
     */
    @NonNull
    @Override
    @SuppressLint("WrongThread")
    public Result doWork() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentNetworkInfo = manager.getActiveNetworkInfo();

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !(networkTypeMatchesAutoSendSetting(currentNetworkInfo) || atLeastOneFormSpecifiesAutoSend())) {
            if (!networkTypeMatchesAutoSendSetting(currentNetworkInfo)) {
                return Result.RETRY;
            }

            return Result.FAILURE;
        }

        List<Instance> toUpload = getInstancesToAutoSend(GeneralSharedPreferences.isAutoSendEnabled());

        if (toUpload.isEmpty()) {
            return Result.SUCCESS;
        }

        GeneralSharedPreferences settings = GeneralSharedPreferences.getInstance();
        String protocol = (String) settings.get(PreferenceKeys.KEY_PROTOCOL);

        Map<String, String> resultMessagesByInstanceId = new HashMap<>();
        boolean anyFailure = false;

        if (protocol.equals(getApplicationContext().getString(R.string.protocol_google_sheets))) {
            //sendInstancesToGoogleSheets(getApplicationContext(), toSendArray);
        } else if (protocol.equals(getApplicationContext().getString(R.string.protocol_odk_default))) {
            InstanceServerUploader uploader = new InstanceServerUploader(new HttpClientConnection(),
                    new WebCredentialsUtils());
            String deviceId = new PropertyManager(Collect.getInstance().getApplicationContext())
                    .getSingularProperty(PropertyManager.withUri(PropertyManager.PROPMGR_DEVICE_ID));
            Map<Uri, Uri> uriRemap = new HashMap<>();

            for (Instance instance : toUpload) {
                String urlString = uploader.getUrlToSubmitTo(instance, deviceId);
                SubmissionUploadResult uploadResult = uploader.uploadOneSubmission(instance, urlString, uriRemap);

                resultMessagesByInstanceId.put(instance.getDatabaseId().toString(), uploadResult.getDisplayMessage());

                if (!uploadResult.isSuccess()) {
                    anyFailure = true;
                }

                if (uploadResult.isFatalError()) {
                    String message = formatOverallResultMessage(resultMessagesByInstanceId);
                    showUploadStatusNotification(anyFailure, message);
                    return Result.FAILURE;
                }

                // If the submission was successful, delete the instance if either the app-level
                // delete preference is set or the form definition requests auto-deletion.
                // TODO: this could take some time so might be better to do in a separate thread
                // It also feels like this could fail and if so should be
                // communicated to the user. Maybe successful delete should also be communicated?
                if (uploadResult.isSuccess()) {
                    if (InstanceServerUploader.formShouldBeAutoDeleted(instance.getJrFormId(),
                            (boolean) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_DELETE_AFTER_SEND))) {
                        Uri deleteForm = Uri.withAppendedPath(InstanceColumns.CONTENT_URI,
                                instance.getDatabaseId().toString());
                        Collect.getInstance().getContentResolver().delete(deleteForm, null, null);
                    }
                }
            }
        }

        String message = formatOverallResultMessage(resultMessagesByInstanceId);
        showUploadStatusNotification(anyFailure, message);

        return Result.SUCCESS;
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

    //    private void sendInstancesToGoogleSheets(Context context, Long[] toSendArray) {
    //        if (PermissionUtils.checkIfGetAccountsPermissionGranted(context)) {
    //            GoogleAccountsManager accountsManager = new GoogleAccountsManager(Collect.getInstance());
    //
    //            String googleUsername = accountsManager.getSelectedAccount();
    //            if (googleUsername.isEmpty()) {
    //                return;
    //            }
    //            accountsManager.getCredential().setSelectedAccountName(googleUsername);
    //            instanceGoogleSheetsUploaderTask = new InstanceGoogleSheetsUploaderTask(accountsManager);
    //            // TODO: instanceServerUploaderTask is an AsyncTask so execute should be run off the main
    //            // thread. This seems to work but unclear what behavior guarantees there are. We should
    //            // move away from AsyncTask here. This requires a deeper rethink/refactor of the
    //            // uploaders.
    //            instanceGoogleSheetsUploaderTask.execute(toSendArray);
    //        } else {
    //            resultMessage = Collect.getInstance().getString(R.string.odk_permissions_fail);
    //        }
    //    }

    /**
     * Returns instances that need to be auto-sent.
     */
    @NonNull
    private List<Instance> getInstancesToAutoSend(boolean isAutoSendAppSettingEnabled) {
        InstancesDao dao = new InstancesDao();
        Cursor c = dao.getFinalizedInstancesCursor();
        List<Instance> allFinalized = dao.getInstancesFromCursor(c);

        List<Instance> toUpload = new ArrayList<>();
        for (Instance instance : allFinalized) {
            if (formShouldBeAutoSent(instance.getJrFormId(), isAutoSendAppSettingEnabled)) {
                toUpload.add(instance);
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
    public static boolean formShouldBeAutoSent(String jrFormId, boolean isAutoSendAppSettingEnabled) {
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

    /**
     * Returns true if at least one form currently on the device specifies that all of its filled
     * forms should auto-send no matter the connection type.
     *
     * TODO: figure out where this should live
     */
    private boolean atLeastOneFormSpecifiesAutoSend() {
        FormsDao dao = new FormsDao();
        Cursor cursor = dao.getFormsCursor();

        try {
            List<Form> forms = dao.getFormsFromCursor(cursor);
            for (Form form : forms) {
                if (Boolean.valueOf(form.getAutoSend())) {
                    return true;
                }
            }
        } finally {
            cursor.close();
        }
        return false;
    }

    private String formatOverallResultMessage(Map<String, String> resultMessagesByInstanceId) {
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

    private void showUploadStatusNotification(boolean anyFailure, String message) {
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
                .setContentText(anyFailure ? Collect.getInstance().getString(R.string.failures)
                        : Collect.getInstance().getString(R.string.success))
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) Collect.getInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(AUTO_SEND_RESULT_NOTIFICATION_ID, builder.build());
    }
}