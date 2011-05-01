
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

    public static String KEY_SERVER = "server";
    public static String KEY_USERNAME = "username";
    public static String KEY_PASSWORD = "password";

    private PreferenceScreen mSplashPathPreference;
    private String mSplashPath;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mContext = this;

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.general_preferences));

        setupSplashPathPreference();

        updateShowSplash();
        updateSplashPath();
        updateServer();
        updateUsername();
        updatePassword();
    }


    private void setupSplashPathPreference() {
        mSplashPathPreference =
            (PreferenceScreen) this.getPreferenceScreen().findPreference(KEY_SPLASH_PATH);

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
                                    getString(R.string.remove_this_image)
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

                // get location of chosen file
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
        if (key.equals(KEY_SERVER)) {
            updateServer();
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


    private void updateServer() {
        EditTextPreference etp =
            (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_SERVER);
        String s = etp.getText().trim();

        if (UrlUtils.isValidUrl(s)) {
            etp.setText(s);
            etp.setSummary(s);
        } else {
            etp.setText((String) etp.getSummary());
            Toast.makeText(getApplicationContext(), getString(R.string.url_error),
                Toast.LENGTH_SHORT).show();
        }
    }


    private void updateSplashPath() {
        System.out.println("on shared pareference updating splash path");
        mSplashPathPreference =
            (PreferenceScreen) this.getPreferenceScreen().findPreference(KEY_SPLASH_PATH);
        mSplashPathPreference.setSummary(mSplashPathPreference.getSharedPreferences().getString(
            KEY_SPLASH_PATH, getString(R.string.default_splash_path)));

    }


    private void updateShowSplash() {
        CheckBoxPreference cbp =
            (CheckBoxPreference) this.getPreferenceScreen().findPreference(KEY_SHOW_SPLASH);
        if (cbp != null && mSplashPathPreference != null) {
            mSplashPathPreference.setEnabled(cbp.isChecked());
        }

    }


    private void updateUsername() {
        EditTextPreference etp =
            (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_USERNAME);
        etp.setSummary(etp.getText());
    }


    private void updatePassword() {
        EditTextPreference etp =
            (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_PASSWORD);
        etp.setSummary("***************");
    }
}
