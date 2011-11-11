/*
 * Copyright 2011 Google Inc. All rights reserved.
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

package org.odk.collect.android.activities;

import java.io.IOException;

import org.odk.collect.android.preferences.PreferencesActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Activity to authenticate against an account and generate a token into the shared preferences.
 * 
 * @author cswenson@google.com (Christopher Swenson)
 */
public class AccountInfo extends Activity {
    final static int WAITING_ID = 1;
    final static String authString = "gather";
    boolean shownDialog = false;


    /**
     * Activity startup.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * When we resume, try to get an auth token.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        Account account = (Account) intent.getExtras().get("account");
        accountManager.getAuthToken(account, authString, false, new AuthTokenCallback(), null);
        showDialog(WAITING_ID);
    }

    /**
     * Helper class to handle getting the auth token.
     * 
     * @author cswenson@google.com (Christopher Swenson)
     */
    private class AuthTokenCallback implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Bundle bundle;
            try {
                bundle = result.getResult();
                Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);

                // Check to see if the last intent failed.
                if ((intent != null) && shownDialog) {
                    failedAuthToken();
                }
                // We need to call the intent to get the token.
                else if (intent != null) {
                    // Use the bundle dialog.
                    startActivity(intent);
                    shownDialog = true;
                } else {
                    gotAuthToken(bundle);
                }
            } catch (OperationCanceledException e) {
                failedAuthToken();
            } catch (AuthenticatorException e) {
                failedAuthToken();
            } catch (IOException e) {
                failedAuthToken();
            }
        }
    }


    /**
     * If we failed to get an auth token.
     */
    protected void failedAuthToken() {
        SharedPreferences settings =
            PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(PreferencesActivity.KEY_ACCOUNT);
        editor.remove(PreferencesActivity.KEY_AUTH);
        editor.commit();
        dismissDialog(WAITING_ID);
        finish();
    }


    /**
     * If we got one, store it in shared preferences.
     * 
     * @param bundle
     */
    protected void gotAuthToken(Bundle bundle) {
        // Set the authentication token and dismiss the dialog.
        String auth_token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
        SharedPreferences settings =
            PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PreferencesActivity.KEY_AUTH, auth_token);
        editor.commit();
        dismissDialog(WAITING_ID);
        finish();
    }


    /**
     * Let the user know we are waiting on the server to authenticate.
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case WAITING_ID:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Waiting on authentication").setCancelable(false);
                AlertDialog alert = builder.create();
                dialog = alert;
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

}
