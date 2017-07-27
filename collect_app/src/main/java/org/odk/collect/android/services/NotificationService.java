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

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;

import timber.log.Timber;

/**
 * Created by neilpenman on 25/07/2017.
 */

public class NotificationService extends IntentService {
    // ...

    public NotificationService() {
        super("notifications");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            Timber.i("Sender Id: " + BuildConfig.SENDER_ID);
            String token = instanceID.getToken(BuildConfig.SENDER_ID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Timber.i("Registration Token: " + token);
        } catch (Exception e) {
            Timber.e(e);
        }


        // ...
    }
}

