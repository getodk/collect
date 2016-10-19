package org.odk.collect.android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.LoginCompleteListener;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.tasks.LoginTask;

public class LoginActivity extends Activity implements LoginCompleteListener {
    private ProgressDialog mProgressDialog;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_layout);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Button btnLogin = (Button) findViewById(R.id.bt_login);
        TextView tvGuestUser = (TextView) findViewById(R.id.tv_skip_login);
        final EditText etUsername = (EditText) findViewById(R.id.et_username);
        final EditText etPassword = (EditText) findViewById(R.id.et_password);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_options_group);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Collect.getInstance().getActivityLogger()
                        .logAction(this, "LoginTask", "click");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                //loginApiCall(username, password);
                if (android.text.TextUtils.isEmpty(username)) {
                    Toast.makeText(LoginActivity.this, "Username should not empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (android.text.TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Password should not empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                int selectionId = radioGroup.getCheckedRadioButtonId();
                if (selectionId == -1) {
                    Toast.makeText(LoginActivity.this, "Please select the option", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences.Editor prefEditor = sharedPref.edit();
                /*prefEditor.putString("username", "nlodkadmin");
                prefEditor.putString("password", "nexleaf_odk_test");
                prefEditor.apply();*/
                prefEditor.putInt(PreferencesActivity.KEY_OPTION_SELECTED, selectionId);
                prefEditor.putString("username", username);
                prefEditor.putString("password", "nexleaf_odk_test");
                prefEditor.apply();
                loginApiCall(username, password);
            }
        });

        tvGuestUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectionId = radioGroup.getCheckedRadioButtonId();
                if (selectionId == -1) {
                    Toast.makeText(LoginActivity.this, "Please select the option", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putInt(PreferencesActivity.KEY_OPTION_SELECTED, selectionId);
                prefEditor.apply();
                navigateToHome();
            }
        });
    }

    private void loginApiCall(String username, String password) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

        if (ni == null || !ni.isConnected()) {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
        } else {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.show();
            LoginTask loginTask = new LoginTask();
            loginTask.setLoginCompleteListener(this);
            loginTask.execute(username, password);
        }
    }

    @Override
    public void loginCompleteListener(String result) {
        mProgressDialog.dismiss();
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        try {
            JSONObject obj = new JSONObject(result);
            String token = obj.getString("token");
            prefEditor.putString(PreferencesActivity.KEY_TOKEN, "17aec7a2eca3bde0060769a5fe6264743ad566ef652a");
            /*prefEditor.putString(PreferencesActivity.KEY_TOKEN, token);*/
            prefEditor.apply();
            Log.i("Login response -> ", result + " token -> " + token);
            //downloadFormList();
            navigateToHome();

        } catch (JSONException e) {
            e.printStackTrace();
            prefEditor.putString(PreferencesActivity.KEY_TOKEN, "17aec7a2eca3bde0060769a5fe6264743ad566ef652a");
            prefEditor.putString(PreferencesActivity.KEY_NAVIGATION, "Use forward/backward buttons");
            /*prefEditor.putString(PreferencesActivity.KEY_TOKEN, token);*/
            prefEditor.apply();
            navigateToHome();

        }
    }

    private void navigateToHome() {
        hideKeyboard();
        Intent home = new Intent(LoginActivity.this, MainMenuActivity.class);
        home.putExtra("loginFirst", 1);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(PreferencesActivity.KEY_FROM_LOGIN, 1);
        editor.apply();
        startActivity(home);
        finish();
    }

    private void hideKeyboard()
    {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}