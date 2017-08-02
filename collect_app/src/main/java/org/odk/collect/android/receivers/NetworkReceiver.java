package org.odk.collect.android.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.InstanceGoogleSheetsUploader;
import org.odk.collect.android.tasks.InstanceServerUploader;
import org.odk.collect.android.utilities.WebUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class NetworkReceiver extends BroadcastReceiver implements InstanceUploaderListener {

    // turning on wifi often gets two CONNECTED events. we only want to run one thread at a time
    public static boolean running = false;
    InstanceServerUploader instanceServerUploader;

    InstanceGoogleSheetsUploader instanceGoogleSheetsUploader;

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
                    && currentNetworkInfo.getState() == NetworkInfo.State.CONNECTED
                    && isFormAutoSendOptionEnabled(currentNetworkInfo)) {
                uploadForms(context);
            }
        } else if (action.equals("org.odk.collect.android.FormSaved")) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnected() && isFormAutoSendOptionEnabled(ni)) {
                uploadForms(context);
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

    private void uploadForms(Context context) {
        if (!running) {
            running = true;

            ArrayList<Long> toUpload = new ArrayList<>();
            Cursor c = new InstancesDao().getFinalizedInstancesCursor();

            try {
                if (c != null && c.getCount() > 0) {
                    c.move(-1);
                    while (c.moveToNext()) {
                        Long l = c.getLong(c.getColumnIndex(InstanceColumns._ID));
                        toUpload.add(l);
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

            GoogleAccountCredential accountCredential;
            // Initialize credentials and service object.
            accountCredential = GoogleAccountCredential.usingOAuth2(
                    Collect.getInstance(), Collections.singleton(DriveScopes.DRIVE))
                    .setBackOff(new ExponentialBackOff());

            GeneralSharedPreferences settings = GeneralSharedPreferences.getInstance();

            String protocol = (String) settings.get(PreferenceKeys.KEY_PROTOCOL);

            if (protocol.equals(context.getString(R.string.protocol_google_sheets))) {
                instanceGoogleSheetsUploader = new InstanceGoogleSheetsUploader(accountCredential, context);
                String googleUsername = (String) settings.get(
                        PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT);
                if (googleUsername == null || googleUsername.equalsIgnoreCase("")) {
                    // just quit if there's no username
                    running = false;
                    return;
                }
                accountCredential.setSelectedAccountName(googleUsername);
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

                instanceServerUploader.execute(toSendArray);
            }
        }
    }

    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        // task is done
        if (instanceServerUploader != null) {
            instanceServerUploader.setUploaderListener(null);
        }
        if (instanceGoogleSheetsUploader != null) {
            instanceGoogleSheetsUploader.setUploaderListener(null);
        }
        running = false;

        StringBuilder message = new StringBuilder();
        message
                .append(Collect.getInstance().getString(R.string.odk_auto_note))
                .append(" :: \n\n");

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
