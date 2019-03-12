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
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.SmapLoginListener;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.tasks.SmapLoginTask;
import org.odk.collect.android.utilities.Validator;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SmapLoginActivity extends CollectAbstractActivity implements SmapLoginListener {

    @BindView(R.id.input_url) EditText urlText;
    @BindView(R.id.input_username) EditText userText;
    @BindView(R.id.input_password) EditText passwordText;
    @BindView(R.id.btn_login) Button loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        final ProgressDialog progressDialog = new ProgressDialog(SmapLoginActivity.this,
                R.style.DarkAppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(Collect.getInstance().getString(R.string.smap_authenticating));
        progressDialog.show();

        SmapLoginTask smapLoginTask = new SmapLoginTask();
        smapLoginTask.setListener(this);
        smapLoginTask.execute(url, username, password);

    }

    @Override
    public void loginComplete(String status) {
        Timber.i("----------" + status);

        loginButton.setEnabled(true);

        if(status == null || status.equals("failed")) {
            loginFailed();
        } else if(status.equals("success")) {
            loginSuccess();
        } else if (status.equals("unauthorized")) {
            loginNotAuthorized();
        } else {
            loginFailed();
        }
    }

    //@Override
    //public void onBackPressed() {
    //    // Disable going back to the MainActivity
    //    moveTaskToBack(true);
    //}

    public void loginSuccess() {

        // Update preferences with login values
        GeneralSharedPreferences prefs = GeneralSharedPreferences.getInstance();
        prefs.save(GeneralKeys.KEY_SERVER_URL, urlText.getText().toString());
        prefs.save(GeneralKeys.KEY_USERNAME, userText.getText().toString());
        prefs.save(GeneralKeys.KEY_PASSWORD, passwordText.getText().toString());

        // Start Main Activity and initiate a refresh
        Intent i = new Intent(SmapLoginActivity.this, SmapMain.class);
        i.putExtra(SmapMain.EXTRA_REFRESH, "yes");
        startActivity(i);  //smap
        finish();
    }

    public void loginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
    }

    public void loginNotAuthorized() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
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
