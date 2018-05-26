package org.odk.collect.android.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

import org.odk.collect.android.listeners.DownloadFormsTaskListener;

import android.support.v4.app.NotificationCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.listeners.TaskDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.DownloadTasksTask;

import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NetworkReceiver extends BroadcastReceiver implements TaskDownloaderListener,
        InstanceUploaderListener,
        DownloadFormsTaskListener {  // smap implement task, instance, form list

    // turning on wifi often gets two CONNECTED events. we only want to run one thread at a time
    public static boolean running = false;
    //InstanceServerUploader instanceServerUploader;    // smap
    public DownloadTasksTask mDownloadTasks;    // smap
    Context mContext = null;        // smap

    // GoogleSheetsAutoUploadTask googleSheetsUploadTask;    // smap

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

        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (currentNetworkInfo != null
                    && currentNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                //uploadForms(context);    // smap
                if (isFormAutoSendOptionEnabled(currentNetworkInfo)) {    // smap

                    refreshTasks(context);   // smap
                }
            }

            ServerPollingJob.pollServerIfNeeded();
        } else if (action.equals("org.odk.collect.android.FormSaved")) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnected()) {
                if (isFormAutoSendOptionEnabled(ni)) {    // smap
                    refreshTasks(context);   // smap
                }
            }
        }
    }

    private boolean isFormAutoSendOptionEnabled(NetworkInfo currentNetworkInfo) {
        // make sure autosend is enabled on the given connected interface
        String autosend = (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_AUTOSEND);
        boolean sendwifi = autosend.equals("wifi_only");
        boolean sendnetwork = autosend.equals("cellular_only");
        if (autosend.equals("wifi_and_cellular")) {
            sendwifi = true;
            sendnetwork = true;
        }

        return (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
                && sendwifi || currentNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                && sendnetwork);
    }

    /*
     * Smap initiates a refresh
     * Uploading to google sheets is not supported
     */
    private void refreshTasks(Context context) {
        //mProgressMsg = getString(org.smap.smapTask.android.R.string.smap_synchronising);
        //showDialog(PROGRESS_DIALOG);

        if (!running) {
            running = true;

            String selection = InstanceColumns.SOURCE + "=? and (" + InstanceColumns.STATUS + "=? or " +
                    InstanceColumns.STATUS + "=?)";
            String selectionArgs[] = {
                    Utilities.getSource(),
                    InstanceProviderAPI.STATUS_COMPLETE,
                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED
            };

            ArrayList<Long> toUpload = new ArrayList<Long>();
            Cursor c = null;
            try {
                c = Collect.getInstance().getContentResolver().query(InstanceColumns.CONTENT_URI, null, selection,
                        selectionArgs, null);

                if (c != null && c.getCount() > 0) {
                    c.move(-1);
                    while (c.moveToNext()) {
                        Long l = c.getLong(c.getColumnIndex(InstanceColumns._ID));
                        toUpload.add(Long.valueOf(l));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            if (toUpload.size() < 1) {
                running = false;
                return;
            }

            mContext = context;
            mDownloadTasks = new DownloadTasksTask();
            mDownloadTasks.setDownloaderListener(this, context);
            mDownloadTasks.execute();
        }
    }

    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        // Refresh task list
        Intent intent = new Intent("org.smap.smapTask.refresh");
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
    }

    @Override
    public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
        // Refresh task list
        Intent intent = new Intent("org.smap.smapTask.refresh");
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
    }

    @Override
    public void formsDownloadingCancelled() {
        // Refresh task list
        Intent intent = new Intent("org.smap.smapTask.refresh");
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
    }



    /* smap comment out upload forms
    private void uploadForms(Context context, boolean isFormAutoSendOptionEnabled) {
        if (!running) {
            running = true;

            ArrayList<Long> toUpload = new ArrayList<>();
            Cursor c = new InstancesDao().getFinalizedInstancesCursor();

            try {
                if (c != null && c.getCount() > 0) {
                    c.move(-1);
                    String formId;
                    while (c.moveToNext()) {
                        formId = c.getString(c.getColumnIndex(InstanceColumns.JR_FORM_ID));
                        if (isFormAutoSendEnabled(formId, isFormAutoSendOptionEnabled)) {
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

            if (toUpload.size() < 1) {
                running = false;
                return;
            }

            Long[] toSendArray = new Long[toUpload.size()];
            toUpload.toArray(toSendArray);

            GeneralSharedPreferences settings = GeneralSharedPreferences.getInstance();
            String protocol = (String) settings.get(PreferenceKeys.KEY_PROTOCOL);

            if (protocol.equals(context.getString(R.string.protocol_google_sheets))) {
                String googleUsername = (String) settings.get(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT);
                if (googleUsername == null || googleUsername.isEmpty()) {
                    // just quit if there's no username
                    running = false;
                    return;
                }
                GoogleAccountsManager accountsManager = new GoogleAccountsManager(Collect.getInstance());
                accountsManager.getCredential().setSelectedAccountName(googleUsername);
                instanceGoogleSheetsUploader = new InstanceGoogleSheetsUploader(accountsManager);
                instanceGoogleSheetsUploader.setUploaderListener(this);
                instanceGoogleSheetsUploader.execute(toSendArray);
            } else {
                // get the username, password, and server from preferences

                String storedUsername = (String) settings.get(PreferenceKeys.KEY_USERNAME);
                String storedPassword = (String) settings.get(PreferenceKeys.KEY_PASSWORD);
                String server = (String) settings.get(PreferenceKeys.KEY_SERVER_URL);
                String url = server + settings.get(PreferenceKeys.KEY_FORMLIST_URL);

                Uri u = Uri.parse(url);
                WebUtils.addCredentials(storedUsername, storedPassword, u.getHost());

                instanceServerUploader = new InstanceServerUploader();
                instanceServerUploader.setUploaderListener(this);

                instanceUploaderTask.execute(toSendArray);
	                }
	            }
	            }



    /**
     * @param isFormAutoSendOptionEnabled represents whether the auto-send option is enabled at the app level
     *
     * If the form explicitly sets the auto-send property, then it overrides the preferences.
     *
    private boolean isFormAutoSendEnabled(String jrFormId, boolean isFormAutoSendOptionEnabled) {
        Cursor cursor = new FormsDao().getFormsCursorForFormId(jrFormId);
        String autoSend = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int autoSendColumnIndex = cursor.getColumnIndex(AUTO_SEND);
                autoSend = cursor.getString(autoSendColumnIndex);
            } finally {
                cursor.close();
            }
        }
        return autoSend == null ? isFormAutoSendOptionEnabled : Boolean.valueOf(autoSend);
    }

    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        // No need to reset uploader tasks as refresh was used
        if (instanceUploaderTask != null) {
            instanceUploaderTask.setUploaderListener(null);
        }
        if (instanceGoogleSheetsUploader != null) {
            instanceGoogleSheetsUploader.setUploaderListener(null);
        }
        running = false;

        StringBuilder message = new StringBuilder();
        message
                .append(Collect.getInstance().getString(R.string.forms_sent))
                .append("\n\n");

        if (result == null) {
            message.append(Collect.getInstance().getString(R.string.odk_auth_auth_fail));
        } else {
            StringBuilder selection = new StringBuilder();
            Set<String> keys = result.keySet();
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

            Cursor results = null;
            try {
                results = new InstancesDao().getInstancesCursor(selection.toString(), selectionArgs);
                if (results.getCount() > 0) {
                    results.moveToPosition(-1);
                    while (results.moveToNext()) {
                        String name = results.getString(results
                                .getColumnIndex(InstanceColumns.DISPLAY_NAME));
                        String id = results.getString(results
                                .getColumnIndex(InstanceColumns._ID));
                        message
                                .append(name)
                                .append(" - ")
                                .append(result.get(id))
                                .append("\n\n");
                    }
                }
            } finally {
                if (results != null) {
                    results.close();
                }
            }
        }

        Intent notifyIntent = new Intent(Collect.getInstance(), NotificationActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_KEY, message.toString().trim());

        PendingIntent pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(Collect.getInstance())
                .setSmallIcon(R.drawable.notes)
                .setContentTitle(Collect.getInstance().getString(R.string.odk_auto_note))
                .setContentIntent(pendingNotify)
                .setContentText(message.toString().trim())
                .setAutoCancel(true)
                .setLargeIcon(
                        BitmapFactory.decodeResource(Collect.getInstance().getResources(),
                                android.R.drawable.ic_dialog_info));

        NotificationManager notificationManager = (NotificationManager) Collect.getInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1328974928, builder.build());
    }
    */

    // smap
    public void taskDownloadingComplete(HashMap<String, String> result) {

        running = false;
        Timber.i("Send intent");
        Intent intent = new Intent("org.smap.smapTask.refresh");   // smap
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);  // smap

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager mNotifyMgr =
                (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        StringBuilder message = Utilities.getUploadMessage(result);

        Intent notifyIntent = new Intent(Collect.getInstance(), NotificationActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_KEY, message.toString().trim());

        PendingIntent pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(Collect.getInstance().getBaseContext().getResources(),
                                R.drawable.ic_launcher))
                        .setSound(uri)
                        .setContentIntent(pendingNotify)
                        .setContentTitle(mContext.getString(R.string.app_name))
                        .setContentText(message.toString().trim());
        mNotifyMgr.notify(NotificationActivity.NOTIFICATION_ID, mBuilder.build());
    }


    @Override
    //public void progressUpdate(int progress, int total) {    // smap
    public void progressUpdate(String progress) {    // Replace with String parameter
        // do nothing
    }

    @Override
    public void progressUpdate(int progress, int total) {
        // do nothing
    }

    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
      // do nothing
    }



    @Override
    public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
        // if we get an auth request, just fail
        if (mDownloadTasks != null) {    // smap
            mDownloadTasks.setDownloaderListener(null, mContext);
        }
        /* smap
        if (instanceUploaderTask != null) {
            instanceUploaderTask.setUploaderListener(null);
        }
        if (instanceGoogleSheetsUploader != null) {
            instanceGoogleSheetsUploader.setUploaderListener(null);
        }
        */
        running = false;
    }

    /* smap
    private class InstanceGoogleSheetsAutoUploadTask extends InstanceGoogleSheetsUploader {
        private Context context;

        InstanceGoogleSheetsAutoUploadTask(Context context, GoogleAccountCredential credential) {
            super(credential);
            this.context = context;
        }

        @Override
        protected Outcome doInBackground(Long... values) {
            outcome = new Outcome();

            String selection = InstanceColumns._ID + "=?";
            String[] selectionArgs = new String[(values == null) ? 0 : values.length];
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    if (i != values.length - 1) {
                        selection += " or " + InstanceColumns._ID + "=?";
                    }
                    selectionArgs[i] = values[i].toString();
                }
            }

            String token;
            try {
                token = credential.getToken();
                GoogleAuthUtil.invalidateToken(context, token);

                getIDOfFolderWithName(GOOGLE_DRIVE_ROOT_FOLDER, null);


            } catch (IOException | GoogleAuthException | MultipleFoldersFoundException e) {
                Timber.e(e);
                return null;
            }
            context = null;

            if (token == null) {
                // failed, so just return
                return null;
            }

            uploadInstances(selection, selectionArgs, token);
            return outcome;
        }
    }
    */
}
