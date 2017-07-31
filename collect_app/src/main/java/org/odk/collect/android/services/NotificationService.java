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

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

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
        DownloadTasksTask dt = new DownloadTasksTask();
        dt.doInBackground();
    }
}
