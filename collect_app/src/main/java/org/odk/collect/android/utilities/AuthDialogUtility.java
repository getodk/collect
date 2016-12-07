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

    public AlertDialog createDialog(Context context, final String url,
            final AuthDialogUtilityResultListener resultListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater factory = LayoutInflater.from(context);
        final View dialogView = factory.inflate(R.layout.server_auth_dialog, null);

        // Get the server, username, and password from the settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
        String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);

        EditText username = (EditText) dialogView.findViewById(R.id.username_edit);
        EditText password = (EditText) dialogView.findViewById(R.id.password_edit);

        username.setText(storedUsername);
        password.setText(storedPassword);

        builder.setTitle(context.getString(R.string.server_requires_auth));
        builder.setMessage(context.getString(R.string.server_auth_credentials, url));
        builder.setView(dialogView);
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Collect.getInstance().getActivityLogger().logAction(this, TAG, "OK");

                EditText username = (EditText) dialogView.findViewById(R.id.username_edit);
                EditText password = (EditText) dialogView.findViewById(R.id.password_edit);

                Uri u = Uri.parse(url);

                WebUtils.addCredentials(username.getText().toString(), password.getText()
                        .toString(), u.getHost());

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

    public interface AuthDialogUtilityResultListener {
        void updatedCredentials();
        void cancelledUpdatingCredentials();
    }
}
