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
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmListenerService;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.DownloadTasksTask;

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

        int mNotificationId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Set refresh notification icon
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon_go)
                        .setLargeIcon(BitmapFactory.decodeResource(Collect.getInstance().getBaseContext().getResources(),
                                R.drawable.ic_launcher))
                        .setProgress(0, 0, true)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.smap_refresh_started));
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        // Refresh
        DownloadTasksTask dt = new DownloadTasksTask();
        dt.doInBackground();

        // Refresh task list
        Intent intent = new Intent("org.smap.smapTask.refresh");
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);

        // Set refresh done notification icon
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(Collect.getInstance().getBaseContext().getResources(),
                                R.drawable.ic_launcher))
                        .setContentTitle(getString(R.string.app_name))
                        .setProgress(0,0,false)
                        .setContentText(getString(R.string.smap_refresh_finished));
        mNotifyMgr.notify(mNotificationId, mBuilder.build());


    }
}
