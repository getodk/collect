
package org.odk.collect.android.preferences;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.UrlUtils;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.MediaStore.Images;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    protected static final int IMAGE_CHOOSER = 0;

    public static String KEY_LAST_VERSION = "lastVersion";
    public static String KEY_FIRST_RUN = "firstRun";
    public static String KEY_SHOW_SPLASH = "showSplash";
    public static String KEY_SPLASH_PATH = "splashPath";

    public static String KEY_SERVER_URL = "server_url";
    public static String KEY_USERNAME = "username";
    public static String KEY_PASSWORD = "password";

    public static String KEY_PROTOCOL = "protocol";
    public static String KEY_FORMLIST_URL = "formlist_url";
    public static String KEY_SUBMISSION_URL = "submission_url";

    private PreferenceScreen mSplashPathPreference;
    private EditTextPreference mSubmissionUrlPreference;
    private EditTextPreference mFormListUrlPreference;
    private EditTextPreference mServerUrlPreference;
    private EditTextPreference mUsernamePreference;
    private EditTextPreference mPasswordPreference;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mContext = this;

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.general_preferences));

        setupSplashPathPreference();

        updateProtocol();

        updateServerUrl();

        updateUsername();
        updatePassword();

        updateFormListUrl();
        updateSubmissionUrl();

        updateShowSplash();
        updateSplashPath();

    }


    private void setupSplashPathPreference() {
        mSplashPathPreference = (PreferenceScreen) findPreference(KEY_SPLASH_PATH);

        if (mSplashPathPreference != null) {
            mSplashPathPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                private void launchImageChooser() {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("image/*");
                    startActivityForResult(i, PreferencesActivity.IMAGE_CHOOSER);
                }


                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // if you have a value, you can clear it or select new.
                    CharSequence cs = mSplashPathPreference.getSummary();
                    if (cs != null && cs.toString().contains("/")) {

                        final CharSequence[] items =
                            {
                                    getString(R.string.select_another_image),
                                    getString(R.string.use_odk_default)
                            };

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(getString(R.string.change_splash_path));
                        builder.setNeutralButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (items[item].equals(getString(R.string.select_another_image))) {
                                    launchImageChooser();
                                } else {
                                    setSplashPath(getString(R.string.default_splash_path));

                                }
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    } else {
                        launchImageChooser();
                    }

                    return true;
                }
            });
        }
    }


    private void setSplashPath(String path) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SPLASH_PATH, path);
        editor.commit();

    }


    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
            this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_CANCELED) {
            // request was canceled, so do nothing
            return;
        }

        ContentValues values;
        Uri imageURI;

        switch (requestCode) {

            case IMAGE_CHOOSER:

                // get gp of chosen file
                Uri uri = intent.getData();
                String[] projection = {
                    Images.Media.DATA
                };

                Cursor c = managedQuery(uri, projection, null, null, null);
                startManagingCursor(c);
                int i = c.getColumnIndexOrThrow(Images.Media.DATA);
                c.moveToFirst();

                // setting image path
                setSplashPath(c.getString(i));
                updateSplashPath();

                break;

        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PROTOCOL)) {
            updateProtocol();
            updateServerUrl();
            updateUsername();
            updatePassword();
            updateFormListUrl();
            updateSubmissionUrl();
        } else if (key.equals(KEY_SERVER_URL)) {
            updateServerUrl();
        } else if (key.equals(KEY_FORMLIST_URL)) {
            updateFormListUrl();
        } else if (key.equals(KEY_SUBMISSION_URL)) {
            updateSubmissionUrl();
        } else if (key.equals(KEY_USERNAME)) {
            updateUsername();
        } else if (key.equals(KEY_PASSWORD)) {
            updatePassword();
        } else if (key.equals(KEY_SHOW_SPLASH)) {
            updateShowSplash();
        } else if (key.equals(KEY_SPLASH_PATH)) {
            updateSplashPath();
        }

    }


    private void validateUrl(EditTextPreference preference) {
        if (preference != null) {
            String url = preference.getText();
            if (UrlUtils.isValidUrl(url)) {
                preference.setText(url);
                preference.setSummary(url);
            } else {
                preference.setText((String) preference.getSummary());
                Toast.makeText(getApplicationContext(), getString(R.string.url_error),
                    Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void updateServerUrl() {
        mServerUrlPreference = (EditTextPreference) findPreference(KEY_SERVER_URL);
        mServerUrlPreference.setSummary(mServerUrlPreference.getText());
    }


    private void updateSplashPath() {
        mSplashPathPreference = (PreferenceScreen) findPreference(KEY_SPLASH_PATH);
        mSplashPathPreference.setSummary(mSplashPathPreference.getSharedPreferences().getString(
            KEY_SPLASH_PATH, getString(R.string.default_splash_path)));
    }


    private void updateShowSplash() {
        CheckBoxPreference cbp = (CheckBoxPreference) findPreference(KEY_SHOW_SPLASH);
    }


    private void updateUsername() {
        mUsernamePreference = (EditTextPreference) findPreference(KEY_USERNAME);
        mUsernamePreference.setSummary(mUsernamePreference.getText());
    }


    private void updatePassword() {
        mPasswordPreference = (EditTextPreference) findPreference(KEY_PASSWORD);
        mPasswordPreference.setSummary("***************");
    }


    private void updateFormListUrl() {
        mFormListUrlPreference = (EditTextPreference) findPreference(KEY_FORMLIST_URL);
        mFormListUrlPreference.setSummary(mFormListUrlPreference.getText());
    }


    private void updateSubmissionUrl() {
        mSubmissionUrlPreference = (EditTextPreference) findPreference(KEY_SUBMISSION_URL);
        mSubmissionUrlPreference.setSummary(mSubmissionUrlPreference.getText());
    }


    private void updateProtocol() {
        ListPreference lp = (ListPreference) findPreference(KEY_PROTOCOL);
        lp.setSummary(lp.getEntry());

        String protocol = lp.getValue();
        if (protocol.equals("aggregate_0_9x")) {
            if (mServerUrlPreference != null) {
                mServerUrlPreference.setEnabled(true);
            }
            if (mUsernamePreference != null) {
                mUsernamePreference.setEnabled(false);
            }
            if (mPasswordPreference != null) {
                mPasswordPreference.setEnabled(false);
            }
            if (mFormListUrlPreference != null) {
                mFormListUrlPreference.setEnabled(false);
            }
            if (mSubmissionUrlPreference != null) {
                mSubmissionUrlPreference.setEnabled(false);
            }

        } else if (protocol.equals("aggregate_1_00")) {
            if (mServerUrlPreference != null) {
                mServerUrlPreference.setEnabled(true);
            }
            if (mUsernamePreference != null) {
                mUsernamePreference.setEnabled(true);
            }
            if (mPasswordPreference != null) {
                mPasswordPreference.setEnabled(true);
            }
            if (mFormListUrlPreference != null) {
                mFormListUrlPreference.setEnabled(false);
            }
            if (mSubmissionUrlPreference != null) {
                mSubmissionUrlPreference.setEnabled(false);
            }

        } else {
            if (mServerUrlPreference != null) {
                mServerUrlPreference.setEnabled(false);
            }
            if (mUsernamePreference != null) {
                mUsernamePreference.setEnabled(true);
            }
            if (mPasswordPreference != null) {
                mPasswordPreference.setEnabled(true);
            }
            if (mFormListUrlPreference != null) {
                mFormListUrlPreference.setEnabled(true);
            }
            if (mSubmissionUrlPreference != null) {
                mSubmissionUrlPreference.setEnabled(true);
            }

        }

    }
}
