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

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.dialogs.ServerAuthDialogFragment;

import javax.inject.Inject;

/**
 * Used to present auth dialog and update credentials in the system as needed.
 */
public class AuthDialogUtility {

    private EditText username;
    private EditText password;

    private String customUsername;
    private String customPassword;

    @Inject
    WebCredentialsUtils webCredentialsUtils;
    @Inject
    PropertyManager propertyManager;

    public AuthDialogUtility() {
        Collect.getInstance().getComponent().inject(this);
    }

    /**
     * @deprecated should use {@link ServerAuthDialogFragment} instead
     */
    @Deprecated
    public AlertDialog createDialog(final Context context,
                                    final AuthDialogUtilityResultListener resultListener, String url) {

        final View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.server_auth_dialog, null);

        String overriddenUrl = null;
        if (url != null) {
            if (!url.startsWith(webCredentialsUtils.getServerUrlFromPreferences())) {
                overriddenUrl = url;
                if (overriddenUrl.contains("?deviceID=")) {
                    overriddenUrl = overriddenUrl.substring(0, overriddenUrl.indexOf("?deviceID="));
                }
            }
        }

        username = dialogView.findViewById(R.id.username_edit);
        password = dialogView.findViewById(R.id.password_edit);

        // The custom username\password take precedence
        username.setText(customUsername != null ? customUsername : overriddenUrl != null ? null : webCredentialsUtils.getUserNameFromPreferences());
        password.setText(customPassword != null ? customPassword : overriddenUrl != null ? null : webCredentialsUtils.getPasswordFromPreferences());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(context.getString(R.string.server_requires_auth));
        builder.setMessage(context.getString(R.string.server_auth_credentials, overriddenUrl != null ? overriddenUrl : webCredentialsUtils.getServerUrlFromPreferences()));
        builder.setView(dialogView);
        String finalOverriddenUrl = overriddenUrl;
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userNameValue = username.getText().toString();
                String passwordValue = password.getText().toString();

                // If custom username, password were passed via intent extras, only keep them for
                // the current submission. If the URL and credentials were set from user preferences,
                // save the credentials provided in the dialog to user preferences.
                if (customUsername != null && customPassword != null) {
                    webCredentialsUtils.saveCredentials(finalOverriddenUrl != null ? finalOverriddenUrl : webCredentialsUtils.getServerUrlFromPreferences(), userNameValue, passwordValue);
                } else if (finalOverriddenUrl == null) {
                    webCredentialsUtils.saveCredentialsPreferences(userNameValue, passwordValue, propertyManager);
                } else {
                    webCredentialsUtils.saveCredentials(finalOverriddenUrl, userNameValue, passwordValue);
                }

                resultListener.updatedCredentials();
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resultListener.cancelledUpdatingCredentials();
                    }
                });

        builder.setCancelable(false);

        return builder.create();
    }

    public void setCustomUsername(String customUsername) {
        this.customUsername = customUsername;
    }

    public void setCustomPassword(String customPassword) {
        this.customPassword = customPassword;
    }

    public interface AuthDialogUtilityResultListener {
        void updatedCredentials();

        void cancelledUpdatingCredentials();
    }
}
