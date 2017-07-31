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

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by neilpenman on 27/07/2017.
 */

public class InstanceIDService extends InstanceIDListenerService {
    public void onTokenRefresh() {
        refreshAllTokens();
    }

    private void refreshAllTokens() {

        // Clear the existing token
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.KEY_SMAP_REGISTRATION_ID, null);

        // Request and register a new token
        Intent intent = new Intent(this, NotificationRegistrationService.class);
        startService(intent);
        Timber.i("Refresh tokens");
    }
};
