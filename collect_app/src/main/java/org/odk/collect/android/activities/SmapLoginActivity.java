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

import android.content.Intent;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.SmapLoginListener;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.tasks.SmapLoginTask;
import org.odk.collect.android.utilities.SnackbarUtils;
import org.odk.collect.android.utilities.Validator;

import java.util.ArrayList;

import androidx.appcompat.widget.AppCompatSpinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SmapLoginActivity extends CollectAbstractActivity implements SmapLoginListener {

    @BindView(R.id.input_url) EditText urlText;
    @BindView(R.id.input_username) EditText userText;
    @BindView(R.id.input_password) EditText passwordText;
    @BindView(R.id.btn_login) Button loginButton;
    @BindView(R.id.progressBar) ProgressBar progressBar;


    private String url;
    private boolean useSpinner;
    private AppCompatSpinner urlSpinner;
    private ArrayAdapter<CharSequence> urlAdapter;
    ArrayList<String> urlValues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.DarkAppTheme);     // override theme for login
        setContentView(R.layout.smap_activity_login);
        ButterKnife.bind(this);
        //urlSpinner = findViewById(R.id.urlSpinner);   URL spinner no longer included - need to add back in if it is required

        url = (String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SERVER_URL);

        if(BuildConfig.FLAVOR.equals("kontrolid") && false) {   // Disable this option
            useSpinner = true;
            urlText.setVisibility(View.GONE);

            // Setup the values list this must have the same number of entries as specified in smap_string
            urlValues = new ArrayList<> ();
            urlValues.add("https://app.kontrolid.org");        // https://app.kontrolid.org
            urlValues.add("https://app.kontrolid.com");        // https://app.kontrolid.com

            // Add the choices to the Spinner
            urlAdapter = ArrayAdapter.createFromResource(this,
                    R.array.smap_kontrolid_servers,
                    android.R.layout.simple_spinner_dropdown_item);
            urlAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            urlSpinner.setAdapter(urlAdapter);

            // Set the initial value
            for(int i = 0; i < urlValues.size(); i++) {
                if(urlValues.get(i).equals(url)) {
                    urlSpinner.setSelection(i);
                    break;
                }
            }
            urlSpinner.setPrompt(Collect.getInstance().getString(R.string.change_server_url));
            urlSpinner.setEnabled(true);
            urlSpinner.setFocusable(true);

            // Respond to the spinner value being changes
            urlSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position < urlValues.size()) {
                        url = urlValues.get(urlSpinner.getSelectedItemPosition());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });


        } else {        // Use the text field
            useSpinner = false;
           // urlSpinner.setVisibility(View.GONE);  // smap disable for the moment
            urlText.setText(url);
        }

        userText.setText((String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_USERNAME));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    login();
                }
                return false;
            }
        });

    }

    public void login() {
        Timber.i("Login started");

        if(!useSpinner) {
            url = urlText.getText().toString();
        }
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
        Timber.i("---------- %s", status);

        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);

        if(status == null || status.startsWith("error")) {
            loginFailed(status);
        } else if(status.equals("success")) {
            loginSuccess();
        } else if (status.equals("unauthorized")) {
            loginNotAuthorized(null);
        } else {
            loginFailed(null);
        }
    }

    public void loginSuccess() {

        // Update preferences with login values
        GeneralSharedPreferences prefs = GeneralSharedPreferences.getInstance();
        prefs.save(GeneralKeys.KEY_SERVER_URL, url);
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

    public void loginFailed(String status) {

        // Attempt to login by comparing values agains stored preferences
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
            loginNotAuthorized(status);   // Credentials do not match
        }

    }

    public void loginNotAuthorized(String status) {
        String msg = Collect.getInstance().getString(R.string.smap_login_unauthorized);
        if(status != null && status.startsWith("error:")) {
            msg += "; " + status.substring(5);
        }
        SnackbarUtils.showShortSnackbar(findViewById(R.id.loginMain), msg);
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

    private CharSequence[] getChoices() {
        CharSequence[] choices = new CharSequence[2];
        choices[0] = "https://app.kontrolid.org";
        choices[1] = "https://app.kontrolid.com";
        return choices;
    }
}
