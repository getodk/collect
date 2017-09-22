/*
 * Copyright (C) 2017 Smap Consulting Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmListenerService;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.DownloadTasksTask;
import org.odk.collect.android.utilities.Utilities;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by neilpenman on 27/07/2017.
 */

/*
 * Respond to a notification from the server
 */
public class NotificationService extends GcmListenerService {

    @Override
    public void onDeletedMessages() {
        // TODO refresh
    }

    @Override
    public void onMessageReceived(final String from, Bundle data) {
        Timber.i("Message received beginning refresh");

        // make sure sd card is ready, if not don't try to send
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        ConnectivityManager manager = (ConnectivityManager) Collect.getInstance().getBaseContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentNetworkInfo = manager.getActiveNetworkInfo();

        boolean automaticNofification = false;
        if (currentNetworkInfo != null
                && currentNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
            if (isFormAutoSendOptionEnabled(currentNetworkInfo)) {
                completeNotification(mNotifyMgr, uri);
                automaticNofification = true;
            }
        }

        if (!automaticNofification) {
            // Set refresh notification icon
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setLargeIcon(BitmapFactory.decodeResource(Collect.getInstance().getBaseContext().getResources(),
                                    R.drawable.ic_launcher))
                            .setSound(uri)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(getString(R.string.smap_server_changed));
            mNotifyMgr.notify(NotificationActivity.NOTIFICATION_ID, mBuilder.build());
        }


    }

    private void completeNotification(NotificationManager mNotifyMgr, Uri uri) {


        // Set refresh notification icon
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon_go)
                        .setLargeIcon(BitmapFactory.decodeResource(Collect.getInstance().getBaseContext().getResources(),
                                R.drawable.ic_launcher))
                        .setProgress(0, 0, true)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.smap_refresh_started));
        mNotifyMgr.notify(NotificationActivity.NOTIFICATION_ID, mBuilder.build());

        // Refresh
        DownloadTasksTask dt = new DownloadTasksTask();
        HashMap<String, String> result = dt.doInBackground();
        StringBuilder message = Utilities.getUploadMessage(result);

        // Refresh task list
        Intent intent = new Intent("org.smap.smapTask.refresh");
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);

        Intent notifyIntent = new Intent(Collect.getInstance(), NotificationActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_KEY, message.toString().trim());

        PendingIntent pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set refresh done notification icon
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(Collect.getInstance().getBaseContext().getResources(),
                                R.drawable.ic_launcher))
                        .setContentTitle(getString(R.string.app_name))
                        .setProgress(0,0,false)
                        .setSound(uri)
                        .setContentIntent(pendingNotify)
                        .setContentText(message.toString().trim());
        mNotifyMgr.notify(NotificationActivity.NOTIFICATION_ID, mBuilder.build());
    }

    private boolean isFormAutoSendOptionEnabled(NetworkInfo currentNetworkInfo) {
        // make sure autosend is enabled on the given connected interface
        String autosend = (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_AUTOSEND);
        boolean autosend_wifi_override = (Boolean) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_SMAP_AUTOSEND_WIFI);
        boolean autosend_wifi_cell_override = (Boolean) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_SMAP_AUTOSEND_WIFI_CELL);
        boolean sendwifi = autosend.equals("wifi_only") || autosend_wifi_override || autosend_wifi_override;
        boolean sendnetwork = autosend.equals("cellular_only") || autosend_wifi_cell_override;
        if (autosend.equals("wifi_and_cellular")) {
            sendwifi = true;
            sendnetwork = true;
        }

        return (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
                && sendwifi || currentNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                && sendnetwork);
    }
}
