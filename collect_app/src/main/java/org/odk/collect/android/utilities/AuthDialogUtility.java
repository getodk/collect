/*
 * Copyright 2016 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;

/**
 * Used to present auth dialog and update credentials in the system as needed.
 */
public class AuthDialogUtility {
    private static final String TAG = "AuthDialogUtility";

    public AlertDialog createDialog(final Context context,
            final AuthDialogUtilityResultListener resultListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.server_auth_dialog, null);

        final EditText username = (EditText) dialogView.findViewById(R.id.username_edit);
        final EditText password = (EditText) dialogView.findViewById(R.id.password_edit);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        username.setText(getUserName(settings));
        password.setText(getPassword(settings));

        builder.setTitle(context.getString(R.string.server_requires_auth));
        builder.setMessage(context.getString(R.string.server_auth_credentials));
        builder.setView(dialogView);
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Collect.getInstance().getActivityLogger().logAction(this, TAG, "OK");

                String userNameValue = username.getText().toString();
                String passwordValue = password.getText().toString();

                saveCredentials(settings, userNameValue, passwordValue);
                setWebCredentialsFromPreferences(context);

                resultListener.updatedCredentials();
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Collect.getInstance().getActivityLogger().logAction(this, TAG, "Cancel");

                        resultListener.cancelledUpdatingCredentials();
                    }
                });

        builder.setCancelable(false);

        return builder.create();
    }

    public static void setWebCredentialsFromPreferences(Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String username = getUserName(settings);
        String password = getPassword(settings);

        if (username == null || username.isEmpty())
            return;

        String host = Uri.parse(getServer(settings, context)).getHost();
        WebUtils.addCredentials(username, password, host);
    }

    private static String getServer(SharedPreferences settings, Context context) {
        return settings.getString(PreferencesActivity.KEY_SERVER_URL,
                context.getString(R.string.default_server_url));
    }

    private static String getPassword(SharedPreferences settings) {
        return settings.getString(PreferencesActivity.KEY_PASSWORD, null);
    }

    private static String getUserName(SharedPreferences settings) {
        return settings.getString(PreferencesActivity.KEY_USERNAME, null);
    }

    private void saveCredentials(SharedPreferences settings, String userName, String password) {
        settings
                .edit()
                .putString(PreferencesActivity.KEY_USERNAME, userName)
                .putString(PreferencesActivity.KEY_PASSWORD, password)
                .commit();
    }

    public interface AuthDialogUtilityResultListener {
        void updatedCredentials();
        void cancelledUpdatingCredentials();
    }
}
