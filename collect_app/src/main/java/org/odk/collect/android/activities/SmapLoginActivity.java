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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.SmapLoginListener;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.SmapLoginTask;
import org.odk.collect.android.utilities.Validator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SmapLoginActivity extends CollectAbstractActivity implements SmapLoginListener {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.input_url) EditText _url;
    @BindView(R.id.input_username) EditText _user;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smap_activity_login);
        ButterKnife.bind(this);

        _url.setText((String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SERVER_URL));
        _user.setText((String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_USERNAME));

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        String url = _url.getText().toString();
        String username = _user.getText().toString();
        String pw = _passwordText.getText().toString();

        if (!validate(url, username, pw)) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SmapLoginActivity.this,
                R.style.DarkAppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(Collect.getInstance().getString(R.string.smap_authenticating));
        progressDialog.show();

        SmapLoginTask smapLoginTask = new SmapLoginTask();
        smapLoginTask.setListener(this);
        smapLoginTask.execute(url, username, pw);

    }

    @Override
    public void loginComplete(String status) {
        Log.d(TAG, "----------" + status);
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate(String url, String username, String pw) {
        boolean valid = true;

        // remove all trailing "/"s
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if (!Validator.isUrlValid(url)) {
            _url.setError(Collect.getInstance().getString(R.string.url_error));
            valid = false;
        } else {
            _url.setError(null);
        }

        if (pw.isEmpty() || !pw.equals(pw.trim())) {
            _passwordText.setError(Collect.getInstance().getString(R.string.password_error_whitespace));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (username.isEmpty() || !username.equals(username.trim())) {
            _user.setError(Collect.getInstance().getString(R.string.username_error_whitespace));
            valid = false;
        } else {
            _user.setError(null);
        }

        return valid;
    }
}
