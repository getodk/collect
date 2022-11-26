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

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.tasks.DownloadTasksTask;
import org.odk.collect.android.utilities.Utilities;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by neilpenman on 27/07/2017.
 */

/*
 * Respond to a notification from the server
 */
public class NotificationService extends FirebaseMessagingService {

    @Inject
    Notifier notifier;

    @Override
    public void onDeletedMessages() {
    }

    private void deferDaggerInit() {
        DaggerUtils.getComponent(getApplicationContext()).inject(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage message){
        Timber.i("Message received beginning refresh");
        deferDaggerInit();

        // make sure sd card is ready, if not don't try to send
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        boolean automaticNofification = false;
        if (Utilities.isFormAutoSendOptionEnabled()) {
            // Refresh
            DownloadTasksTask dt = new DownloadTasksTask();
            dt.doInBackground();

            automaticNofification = true;
        }


        if (!automaticNofification) {
            notifier.showNotification(null,
                    NotificationActivity.NOTIFICATION_ID,
                    R.string.app_name,
                    getString(R.string.smap_server_changed), false);    // Add start
        }

    }

    @Override
    public void onNewToken(String token) {
        Timber.i("Refreshed token: %s", token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {

        // Clear the existing token
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GeneralKeys.KEY_SMAP_REGISTRATION_ID, token);
        editor.apply();

        Utilities.updateServerRegistration(true);
    }
}
