package org.odk.collect.android.activities;

/*
 * Copyright (C) 2019 Smap Consulting Pty Ltd
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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.SmapLoginListener;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.tasks.SmapLoginTask;
import org.odk.collect.android.utilities.SnackbarUtils;
import org.odk.collect.android.utilities.Validator;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SmapLoginActivity extends CollectAbstractActivity implements SmapLoginListener {

    @BindView(R.id.input_url) EditText urlText;
    @BindView(R.id.input_username) EditText userText;
    @BindView(R.id.input_password) EditText passwordText;
    @BindView(R.id.btn_login) Button loginButton;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.DarkAppTheme);     // override theme for login
        setContentView(R.layout.smap_activity_login);
        ButterKnife.bind(this);

        urlText.setText((String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SERVER_URL));
        userText.setText((String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_USERNAME));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    public void login() {
        Timber.i("Login started");

        String url = urlText.getText().toString();
        String username = userText.getText().toString();
        String password = passwordText.getText().toString();

        if (!validate(url, username, password)) {
            return;
        }

        loginButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        SmapLoginTask smapLoginTask = new SmapLoginTask();
        smapLoginTask.setListener(this);
        smapLoginTask.execute(url, username, password);

    }

    @Override
    public void loginComplete(String status) {
        Timber.i("----------" + status);

        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);

        if(status == null || status.equals("error")) {
            loginFailed();
        } else if(status.equals("success")) {
            loginSuccess();
        } else if (status.equals("unauthorized")) {
            loginNotAuthorized();
        } else {
            loginFailed();
        }
    }

    public void loginSuccess() {

        // Update preferences with login values
        GeneralSharedPreferences prefs = GeneralSharedPreferences.getInstance();
        prefs.save(GeneralKeys.KEY_SERVER_URL, urlText.getText().toString());
        prefs.save(GeneralKeys.KEY_USERNAME, userText.getText().toString());
        prefs.save(GeneralKeys.KEY_PASSWORD, passwordText.getText().toString());

        // Save the login time in case the password policy is set to periodic
        prefs.save(GeneralKeys.KEY_SMAP_LAST_LOGIN, String.valueOf(System.currentTimeMillis()));

        // Start Main Activity and initiate a refresh
        Intent i = new Intent(SmapLoginActivity.this, SmapMain.class);
        i.putExtra(SmapMain.EXTRA_REFRESH, "yes");
        i.putExtra(SmapMain.LOGIN_STATUS, "success");
        startActivity(i);  //smap
        finish();
    }

    public void loginFailed() {

        // Attempt to login by comparing values agains stored preferences
        String url = urlText.getText().toString();
        String username = userText.getText().toString();
        String password = passwordText.getText().toString();

        String prefUrl = (String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SERVER_URL);
        String prefUsername = (String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_USERNAME);
        String prefPassword = (String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_PASSWORD);

        if(url.equals(prefUrl) && username.equals(prefUsername) && password.equals(prefPassword)) {
            // Start Main Activity no refresh as presumably there is no network
            Intent i = new Intent(SmapLoginActivity.this, SmapMain.class);
            i.putExtra(SmapMain.EXTRA_REFRESH, "no");
            i.putExtra(SmapMain.LOGIN_STATUS, "failed");
            startActivity(i);  //smap
            finish();
        } else {
            loginNotAuthorized();   // Credentials do not match
        }

    }

    public void loginNotAuthorized() {
        SnackbarUtils.showShortSnackbar(findViewById(R.id.loginMain), Collect.getInstance().getString(R.string.smap_login_unauthorized));
    }

    public boolean validate(String url, String username, String pw) {
        boolean valid = true;

        // remove all trailing "/"s
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if (!Validator.isUrlValid(url)) {
            urlText.setError(Collect.getInstance().getString(R.string.url_error));
            valid = false;
        } else {
            urlText.setError(null);
        }

        if (pw.isEmpty() || !pw.equals(pw.trim())) {
            passwordText.setError(Collect.getInstance().getString(R.string.password_error_whitespace));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (username.isEmpty() || !username.equals(username.trim())) {
            userText.setError(Collect.getInstance().getString(R.string.username_error_whitespace));
            valid = false;
        } else {
            userText.setError(null);
        }

        return valid;
    }
}
