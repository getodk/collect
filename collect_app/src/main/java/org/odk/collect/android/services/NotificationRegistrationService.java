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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.amazonaws.mobile.AWSMobileClient;
import org.odk.collect.android.amazonaws.models.nosql.DevicesDO;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.Utilities;

import timber.log.Timber;

/**
 * Created by neilpenman on 25/07/2017.
 */

public class NotificationRegistrationService extends IntentService {
    // ...

    InstanceID instanceID = null;

    public NotificationRegistrationService() {
        super("notifications");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        Timber.i("================================================== Registration Service");

        if(instanceID == null) {
            instanceID = InstanceID.getInstance(this);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {

            boolean notifyServer = false;
            // Get the token
            String token = sharedPreferences.getString(PreferenceKeys.KEY_SMAP_REGISTRATION_ID, null);
            if(token == null || token.trim().length() == 0) {
                token = instanceID.getToken(BuildConfig.SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                editor.putString(PreferenceKeys.KEY_SMAP_REGISTRATION_ID, token);
                notifyServer = true;
            }
            Timber.i("Registration Token: " + token);

            String username = sharedPreferences.getString(PreferenceKeys.KEY_USERNAME, null);
            String server = Utilities.getSource();

            if(username != null && server != null && token != null) {

                String registeredServer = sharedPreferences.getString(PreferenceKeys.KEY_SMAP_REGISTRATION_SERVER, null);
                String registeredUser = sharedPreferences.getString(PreferenceKeys.KEY_SMAP_REGISTRATION_USER, null);

                // We can notify the server if we need to
                // Update the server if the registrationId is new or the server or username's have changed
                if(notifyServer || registeredServer == null || registeredUser == null ||
                        !username.equals(registeredUser) || !server.equals(registeredServer)) {

                    Timber.i("================================================== Notifying server of update");
                    Timber.i("    token: " + token);
                    Timber.i("    server: " + server);
                    Timber.i("    user: " + username);
                    AWSMobileClient.initializeMobileClientIfNecessary(getApplicationContext());
                    final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
                    DevicesDO devices = new DevicesDO();
                    devices.setRegistrationId(token);
                    devices.setSmapServer(server);
                    devices.setUserIdent(username);
                    mapper.save(devices);

                    editor.putString(PreferenceKeys.KEY_SMAP_REGISTRATION_SERVER, server);
                    editor.putString(PreferenceKeys.KEY_SMAP_REGISTRATION_USER, username);
                } else {
                    Timber.i("================================================== Notification not required");
                }


            } else {
                Timber.i("Did not notify server user: " + username + " token: " + token);
            }
        } catch (Exception e) {
            Timber.e(e);
        }


        // ...
    }
}

