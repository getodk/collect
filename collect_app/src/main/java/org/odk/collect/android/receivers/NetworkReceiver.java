package org.odk.collect.android.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;

import org.odk.collect.android.listeners.DownloadFormsTaskListener;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.NotificationUtils;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.listeners.TaskDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.DownloadTasksTask;

import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import timber.log.Timber;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Responsible for initiating a background refresh when a network connection is found
 * or a form is saved as complete
 */

public class NetworkReceiver extends BroadcastReceiver implements TaskDownloaderListener,
        InstanceUploaderListener,
        DownloadFormsTaskListener {

    // turning on wifi often gets two CONNECTED events. we only want to run one thread at a time
    public static boolean running;
    public DownloadTasksTask mDownloadTasks;
    Context mContext = null;

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

        if /*(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (currentNetworkInfo != null
                    && currentNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                if (isFormAutoSendOptionEnabled(currentNetworkInfo)) {
                    refreshTasks(context);
                }
            }

        } else if */(action.equals("org.odk.collect.android.FormSaved")) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnected()) {
                if (isFormAutoSendOptionEnabled(ni)) {
                    refreshTasks(context);
                }
            }
        }
    }

    private boolean isFormAutoSendOptionEnabled(NetworkInfo currentNetworkInfo) {
        // make sure autosend is enabled on the given connected interface
        String autosend = (String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_AUTOSEND);
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
            Timber.i("=============================================" + " in Network Receiver");
            mDownloadTasks.execute();
        }
    }

    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        // Refresh task list
        Intent intent = new Intent("org.smap.smapTask.refresh");
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
        Timber.i("######## send org.smap.smapTask.refresh from networkReceiver");  // smap
    }

    @Override
    public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
        // Refresh task list
        Intent intent = new Intent("org.smap.smapTask.refresh");
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
        Timber.i("######## send org.smap.smapTask.refresh from networkReceiver2");  // smap
    }

    @Override
    public void formsDownloadingCancelled() {
        // Refresh task list
        Intent intent = new Intent("org.smap.smapTask.refresh");
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
        Timber.i("######## send org.smap.smapTask.refresh from networkReceiver3");  // smap
    }


    public void taskDownloadingComplete(HashMap<String, String> result) {

        running = false;
        Timber.i("Send intent");
        Intent intent = new Intent("org.smap.smapTask.refresh");
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
        Timber.i("######## send org.smap.smapTask.refresh from networkReceiver4");  // smap

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager mNotifyMgr =
                (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        StringBuilder message = Utilities.getUploadMessage(result);

        Intent notifyIntent = new Intent(Collect.getInstance(), NotificationActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, message.toString().trim());

        PendingIntent pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationUtils.showNotification(pendingNotify,
                NotificationActivity.NOTIFICATION_ID,
                R.string.app_name,
                message.toString().trim(), false);  // add start

    }
    @Override
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

        running = false;
    }
}
