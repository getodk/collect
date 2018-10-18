package org.odk.collect.android.workers;

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
import org.odk.collect.android.utilities.IconUtils;
import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;
import org.odk.collect.android.utilities.InstanceUploaderUtils;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.InstanceGoogleSheetsUploaderTask;
import org.odk.collect.android.tasks.InstanceServerUploaderTask;
import org.odk.collect.android.utilities.PermissionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import timber.log.Timber;

import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_UPLOADED_NOTIFICATION;

public class AutoSendWorker extends Worker implements InstanceUploaderListener {
    InstanceServerUploaderTask instanceServerUploaderTask;

    InstanceGoogleSheetsUploaderTask instanceGoogleSheetsUploaderTask;

    private String resultMessage;
    /**
     * Causes the doWork method to wait until the instanceUploader AsyncTask has completed.
     * This strategy assumes that the uploader tasks will always terminate.
     */
    private CountDownLatch countDownLatch;
    private Result workResult;

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

        List<Long> toUpload = getInstancesToAutoSend(GeneralSharedPreferences.isAutoSendEnabled());

        if (toUpload.isEmpty()) {
            return Result.SUCCESS;
        }

        countDownLatch = new CountDownLatch(1);

        Long[] toSendArray = new Long[toUpload.size()];
        toUpload.toArray(toSendArray);

        GeneralSharedPreferences settings = GeneralSharedPreferences.getInstance();
        String protocol = (String) settings.get(PreferenceKeys.KEY_PROTOCOL);

        if (protocol.equals(getApplicationContext().getString(R.string.protocol_google_sheets))) {
            sendInstancesToGoogleSheets(getApplicationContext(), toSendArray);
        } else if (protocol.equals(getApplicationContext().getString(R.string.protocol_odk_default))) {
            instanceServerUploaderTask = new InstanceServerUploaderTask();
            instanceServerUploaderTask.setUploaderListener(this);
            // TODO: instanceServerUploaderTask is an AsyncTask so execute should be run off the main
            // thread. This seems to work but unclear what behavior guarantees there are. We should
            // move away from AsyncTask here.
            instanceServerUploaderTask.execute(toSendArray);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Timber.e(e);
            return Result.FAILURE;
        }

        return workResult;
    }

    @Override
    public void uploadingComplete(HashMap<String, String> resultMessagesByInstanceId) {
        if (instanceServerUploaderTask != null) {
            instanceServerUploaderTask.setUploaderListener(null);
        }
        if (instanceGoogleSheetsUploaderTask != null) {
            instanceGoogleSheetsUploaderTask.setUploaderListener(null);
        }

        String message = formatOverallResultMessage(resultMessagesByInstanceId);
        showUploadStatusNotification(resultMessagesByInstanceId, message);

        // TODO: can we detect recoverable/transient network errors and retry instead? This means
        // that unlike with the implicit intent implementation, there won't be a retry for those
        // kinds of problems until another form is finalized.
        if (resultMessagesByInstanceId == null) {
            workResult = Result.FAILURE;
        } else {
            workResult = Result.SUCCESS;
        }

        countDownLatch.countDown();
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

    private void sendInstancesToGoogleSheets(Context context, Long[] toSendArray) {
        if (PermissionUtils.checkIfGetAccountsPermissionGranted(context)) {
            GoogleAccountsManager accountsManager = new GoogleAccountsManager(Collect.getInstance());

            String googleUsername = accountsManager.getSelectedAccount();
            if (googleUsername.isEmpty()) {
                workResult = Result.FAILURE;
                countDownLatch.countDown();
                return;
            }
            accountsManager.getCredential().setSelectedAccountName(googleUsername);
            instanceGoogleSheetsUploaderTask = new InstanceGoogleSheetsUploaderTask(accountsManager);
            instanceGoogleSheetsUploaderTask.setUploaderListener(this);
            // TODO: instanceServerUploaderTask is an AsyncTask so execute should be run off the main
            // thread. This seems to work but unclear what behavior guarantees there are. We should
            // move away from AsyncTask here. This requires a deeper rethink/refactor of the
            // uploaders.
            instanceGoogleSheetsUploaderTask.execute(toSendArray);
        } else {
            resultMessage = Collect.getInstance().getString(R.string.odk_permissions_fail);
            uploadingComplete(null);
        }
    }

    /**
     * Returns a list of longs representing the database ids of the instances that need to be
     * auto-sent.
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
        if (instanceServerUploaderTask != null) {
            instanceServerUploaderTask.setUploaderListener(null);
        }
        if (instanceGoogleSheetsUploaderTask != null) {
            instanceGoogleSheetsUploaderTask.setUploaderListener(null);
        }

        // TODO: this means if there was an auth failure, there will never be a retry until another
        // form is finalized. This is unlike the implicit intent behavior where a retry would happen
        // next time the connection toggled.
        workResult = Result.FAILURE;
        countDownLatch.countDown();
    }
}