/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.collect.android.preferences;

import java.util.ArrayList;

import org.javarosa.core.services.IPropertyManager;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.utilities.MediaUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore.Images;
import android.text.InputFilter;
import android.text.Spanned;

/**
 * Handles general preferences.
 *
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com;
 *         constraint behavior option)
 */
public class PreferencesActivity extends PreferenceActivity implements OnPreferenceChangeListener {

  public static final String INTENT_KEY_ADMIN_MODE = "adminMode";
  protected static final int IMAGE_CHOOSER = 0;

  // PUT ALL PREFERENCE KEYS HERE
  public static final String KEY_INFO = "info";
  public static final String KEY_LAST_VERSION = "lastVersion";
  public static final String KEY_FIRST_RUN = "firstRun";
  public static final String KEY_SHOW_SPLASH = "showSplash";
  public static final String KEY_SPLASH_PATH = "splashPath";
  public static final String KEY_FONT_SIZE = "font_size";
  public static final String KEY_DELETE_AFTER_SEND = "delete_send";

  public static final String KEY_PROTOCOL = "protocol";
  public static final String KEY_PROTOCOL_SETTINGS = "protocol_settings";

  // leaving these in the main screen because username can be used as a
  // pre-fill
  // value in a form
  public static final String KEY_SELECTED_GOOGLE_ACCOUNT = "selected_google_account";
  public static final String KEY_USERNAME = "username";
  public static final String KEY_PASSWORD = "password";

  // AGGREGATE SPECIFIC
  public static final String KEY_SERVER_URL = "server_url";

  // GME SPECIFIC
  public static final String KEY_GME_PROJECT_ID = "gme_project_id";
  public static final String KEY_GME_ID_HASHMAP = "gme_id_hashmap";

  // OTHER SPECIFIC
  public static final String KEY_FORMLIST_URL = "formlist_url";
  public static final String KEY_SUBMISSION_URL = "submission_url";

  public static final String NAVIGATION_SWIPE = "swipe";
  public static final String NAVIGATION_BUTTONS = "buttons";
  public static final String NAVIGATION_SWIPE_BUTTONS = "swipe_buttons";

  public static final String CONSTRAINT_BEHAVIOR_ON_SWIPE = "on_swipe";
  public static final String CONSTRAINT_BEHAVIOR_ON_FINALIZE = "on_finalize";
  public static final String CONSTRAINT_BEHAVIOR_DEFAULT = "on_swipe";

  public static final String KEY_COMPLETED_DEFAULT = "default_completed";

  public static final String KEY_HIGH_RESOLUTION = "high_resolution";

  public static final String KEY_AUTH = "auth";

  public static final String KEY_AUTOSEND_WIFI = "autosend_wifi";
  public static final String KEY_AUTOSEND_NETWORK = "autosend_network";

  public static final String KEY_NAVIGATION = "navigation";
  public static final String KEY_CONSTRAINT_BEHAVIOR = "constraint_behavior";

  private PreferenceScreen mSplashPathPreference;

  private ListPreference mSelectedGoogleAccountPreference;
  private ListPreference mFontSizePreference;
  private ListPreference mNavigationPreference;
  private ListPreference mConstraintBehaviorPreference;

  private CheckBoxPreference mAutosendWifiPreference;
  private CheckBoxPreference mAutosendNetworkPreference;
  private ListPreference mProtocolPreference;

  private PreferenceScreen mProtocolSettings;

  protected EditTextPreference mUsernamePreference;
  protected EditTextPreference mPasswordPreference;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    setTitle(getString(R.string.app_name) + " > " + getString(R.string.general_preferences));

    // not super safe, but we're just putting in this mode to help
    // administrate
    // would require code to access it
    final boolean adminMode = getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);

    SharedPreferences adminPreferences = getSharedPreferences(
        AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

    // assign all the preferences in advance because changing one often
    // affects another
    // also avoids npe
    PreferenceCategory autosendCategory = (PreferenceCategory) findPreference(getString(R.string.autosend));
    mAutosendWifiPreference = (CheckBoxPreference) findPreference(KEY_AUTOSEND_WIFI);
    mAutosendNetworkPreference = (CheckBoxPreference) findPreference(KEY_AUTOSEND_NETWORK);
    PreferenceCategory serverCategory = (PreferenceCategory) findPreference(getString(R.string.server_preferences));

    mProtocolPreference = (ListPreference) findPreference(KEY_PROTOCOL);

    mSelectedGoogleAccountPreference = (ListPreference) findPreference(KEY_SELECTED_GOOGLE_ACCOUNT);
    PreferenceCategory clientCategory = (PreferenceCategory) findPreference(getString(R.string.client));
    mNavigationPreference = (ListPreference) findPreference(KEY_NAVIGATION);
    mFontSizePreference = (ListPreference) findPreference(KEY_FONT_SIZE);
    Preference defaultFinalized = findPreference(KEY_COMPLETED_DEFAULT);
    Preference deleteAfterSend = findPreference(KEY_DELETE_AFTER_SEND);
    mSplashPathPreference = (PreferenceScreen) findPreference(KEY_SPLASH_PATH);
    mConstraintBehaviorPreference = (ListPreference) findPreference(KEY_CONSTRAINT_BEHAVIOR);

    mUsernamePreference = (EditTextPreference) findPreference(PreferencesActivity.KEY_USERNAME);
    mPasswordPreference = (EditTextPreference) findPreference(PreferencesActivity.KEY_PASSWORD);

    mProtocolSettings = (PreferenceScreen) findPreference(KEY_PROTOCOL_SETTINGS);

    boolean autosendWifiAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_AUTOSEND_WIFI, true);
    if (!(autosendWifiAvailable || adminMode)) {
      autosendCategory.removePreference(mAutosendWifiPreference);
    }

    boolean autosendNetworkAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_AUTOSEND_NETWORK, true);
    if (!(autosendNetworkAvailable || adminMode)) {
      autosendCategory.removePreference(mAutosendNetworkPreference);
    }

    if (!(autosendNetworkAvailable || autosendWifiAvailable || adminMode)) {
      getPreferenceScreen().removePreference(autosendCategory);
    }

    mProtocolPreference = (ListPreference) findPreference(KEY_PROTOCOL);
    mProtocolPreference.setSummary(mProtocolPreference.getEntry());
    Intent prefIntent = null;

    if (mProtocolPreference.getValue().equals(getString(R.string.protocol_odk_default))) {
      setDefaultAggregatePaths();
      prefIntent = new Intent(this, AggregatePreferencesActivity.class);
    } else if (mProtocolPreference.getValue().equals(
        getString(R.string.protocol_google_maps_engine))) {
      prefIntent = new Intent(this, GMEPreferencesActivity.class);
    } else {
      // other
      prefIntent = new Intent(this, OtherPreferencesActivity.class);
    }
    prefIntent.putExtra(INTENT_KEY_ADMIN_MODE, adminMode);
    mProtocolSettings.setIntent(prefIntent);

    mProtocolPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        String oldValue = ((ListPreference) preference).getValue();
        int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
        String entry = (String) ((ListPreference) preference).getEntries()[index];
        String value = (String) ((ListPreference) preference).getEntryValues()[index];
        ((ListPreference) preference).setSummary(entry);

        Intent prefIntent = null;
        if (value.equals(getString(R.string.protocol_odk_default))) {
          setDefaultAggregatePaths();
          prefIntent = new Intent(PreferencesActivity.this, AggregatePreferencesActivity.class);
        } else if (value.equals(getString(R.string.protocol_google_maps_engine))) {
          prefIntent = new Intent(PreferencesActivity.this, GMEPreferencesActivity.class);
        } else {
          // other
          prefIntent = new Intent(PreferencesActivity.this, OtherPreferencesActivity.class);
        }
        prefIntent.putExtra(INTENT_KEY_ADMIN_MODE, adminMode);
        mProtocolSettings.setIntent(prefIntent);

        if (!((String) newValue).equals(oldValue)) {
          startActivity(prefIntent);
        }

        return true;
      }
    });

    boolean changeProtocol = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_CHANGE_SERVER, true);
    if (!(changeProtocol || adminMode)) {
      serverCategory.removePreference(mProtocolPreference);
    }
    boolean changeProtocolSettings = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_CHANGE_PROTOCOL_SETTINGS, true);
    if (!(changeProtocolSettings || adminMode)) {
      serverCategory.removePreference(mProtocolSettings);
    }

    // get list of google accounts
    final Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType(
        "com.google");
    ArrayList<String> accountEntries = new ArrayList<String>();
    ArrayList<String> accountValues = new ArrayList<String>();

    for (int i = 0; i < accounts.length; i++) {
      accountEntries.add(accounts[i].name);
      accountValues.add(accounts[i].name);
    }
    accountEntries.add(getString(R.string.no_account));
    accountValues.add("");

    mSelectedGoogleAccountPreference.setEntries(accountEntries.toArray(new String[accountEntries
        .size()]));
    mSelectedGoogleAccountPreference.setEntryValues(accountValues.toArray(new String[accountValues
        .size()]));
    mSelectedGoogleAccountPreference
        .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

          @Override
          public boolean onPreferenceChange(Preference preference, Object newValue) {
            int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
            String value = (String) ((ListPreference) preference).getEntryValues()[index];
            ((ListPreference) preference).setSummary(value);
            return true;
          }
        });
    mSelectedGoogleAccountPreference.setSummary(mSelectedGoogleAccountPreference.getValue());

    boolean googleAccountAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_CHANGE_GOOGLE_ACCOUNT, true);
    if (!(googleAccountAvailable || adminMode)) {
      serverCategory.removePreference(mSelectedGoogleAccountPreference);
    }

    mUsernamePreference.setOnPreferenceChangeListener(this);
    mUsernamePreference.setSummary(mUsernamePreference.getText());
    mUsernamePreference.getEditText().setFilters(new InputFilter[] { getReturnFilter() });

    boolean usernameAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_CHANGE_USERNAME, true);
    if (!(usernameAvailable || adminMode)) {
      serverCategory.removePreference(mUsernamePreference);
    }

    mPasswordPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        String pw = newValue.toString();

        if (pw.length() > 0) {
          mPasswordPreference.setSummary("********");
        } else {
          mPasswordPreference.setSummary("");
        }
        return true;
      }
    });
    if (mPasswordPreference.getText() != null && mPasswordPreference.getText().length() > 0) {
      mPasswordPreference.setSummary("********");
    }
    mPasswordPreference.getEditText().setFilters(new InputFilter[] { getReturnFilter() });

    boolean passwordAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_CHANGE_PASSWORD, true);
    if (!(passwordAvailable || adminMode)) {
      serverCategory.removePreference(mPasswordPreference);
    }

    boolean navigationAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_NAVIGATION, true);
    mNavigationPreference.setSummary(mNavigationPreference.getEntry());
    mNavigationPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
        String entry = (String) ((ListPreference) preference).getEntries()[index];
        ((ListPreference) preference).setSummary(entry);
        return true;
      }
    });
    if (!(navigationAvailable || adminMode)) {
      clientCategory.removePreference(mNavigationPreference);
    }

    boolean constraintBehaviorAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_CONSTRAINT_BEHAVIOR, true);
    mConstraintBehaviorPreference.setSummary(mConstraintBehaviorPreference.getEntry());
    mConstraintBehaviorPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
        String entry = (String) ((ListPreference) preference).getEntries()[index];
        ((ListPreference) preference).setSummary(entry);
        return true;
      }
    });
    if (!(constraintBehaviorAvailable || adminMode)) {
      clientCategory.removePreference(mConstraintBehaviorPreference);
    }

    boolean fontAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_CHANGE_FONT_SIZE, true);
    mFontSizePreference.setSummary(mFontSizePreference.getEntry());
    mFontSizePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
        String entry = (String) ((ListPreference) preference).getEntries()[index];
        ((ListPreference) preference).setSummary(entry);
        return true;
      }
    });
    if (!(fontAvailable || adminMode)) {
      clientCategory.removePreference(mFontSizePreference);
    }

    boolean defaultAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_DEFAULT_TO_FINALIZED, true);

    if (!(defaultAvailable || adminMode)) {
      clientCategory.removePreference(defaultFinalized);
    }

    boolean deleteAfterAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_DELETE_AFTER_SEND, true);
    if (!(deleteAfterAvailable || adminMode)) {
      clientCategory.removePreference(deleteAfterSend);
    }

    boolean resolutionAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_HIGH_RESOLUTION, true);

    Preference highResolution = findPreference(KEY_HIGH_RESOLUTION);
    if (!(resolutionAvailable || adminMode)) {
      clientCategory.removePreference(highResolution);
    }

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

          final CharSequence[] items = { getString(R.string.select_another_image),
              getString(R.string.use_odk_default) };

          AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
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

    mSplashPathPreference.setSummary(mSplashPathPreference.getSharedPreferences().getString(
        KEY_SPLASH_PATH, getString(R.string.default_splash_path)));

    boolean showSplashAvailable = adminPreferences.getBoolean(
        AdminPreferencesActivity.KEY_SHOW_SPLASH_SCREEN, true);

    CheckBoxPreference showSplashPreference = (CheckBoxPreference) findPreference(KEY_SHOW_SPLASH);

    if (!(showSplashAvailable || adminMode)) {
      clientCategory.removePreference(showSplashPreference);
      clientCategory.removePreference(mSplashPathPreference);
    }

    if (!(fontAvailable || defaultAvailable || showSplashAvailable || navigationAvailable
        || adminMode || resolutionAvailable)) {
      getPreferenceScreen().removePreference(clientCategory);
    }

  }

  @Override
  protected void onPause() {
    super.onPause();

    // the property manager should be re-assigned, as properties
    // may have changed.
    IPropertyManager mgr = new PropertyManager(this);
    FormController.initializeJavaRosa(mgr);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // has to go in onResume because it may get updated by
    // a sub-preference screen
    // this just keeps the widgets in sync
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    String account = sp.getString(KEY_SELECTED_GOOGLE_ACCOUNT, "");
    mSelectedGoogleAccountPreference.setSummary(account);
    mSelectedGoogleAccountPreference.setValue(account);

    String user = sp.getString(KEY_USERNAME, "");
    String pw = sp.getString(KEY_PASSWORD, "");
    mUsernamePreference.setSummary(user);
    mUsernamePreference.setText(user);
    if (pw != null && pw.length() > 0) {
      mPasswordPreference.setSummary("********");
      mPasswordPreference.setText(pw);
    }

  }

  private void setSplashPath(String path) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    Editor editor = sharedPreferences.edit();
    editor.putString(KEY_SPLASH_PATH, path);
    editor.commit();

    mSplashPathPreference = (PreferenceScreen) findPreference(KEY_SPLASH_PATH);
    mSplashPathPreference.setSummary(mSplashPathPreference.getSharedPreferences().getString(
        KEY_SPLASH_PATH, getString(R.string.default_splash_path)));
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (resultCode == RESULT_CANCELED) {
      // request was canceled, so do nothing
      return;
    }

    switch (requestCode) {
    case IMAGE_CHOOSER:

      // get gp of chosen file
      Uri selectedMedia = intent.getData();
      String sourceMediaPath = MediaUtils.getPathFromUri(this, selectedMedia, Images.Media.DATA);

      // setting image path
      setSplashPath(sourceMediaPath);
      break;
    }
  }

  private void setDefaultAggregatePaths() {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    Editor editor = sp.edit();
    editor.putString(KEY_FORMLIST_URL, getString(R.string.default_odk_formlist));
    editor.putString(KEY_SUBMISSION_URL, getString(R.string.default_odk_submission));
    editor.commit();
  }

  /**
   * Disallows carriage returns from user entry
   *
   * @return
   */
  protected InputFilter getReturnFilter() {
    InputFilter returnFilter = new InputFilter() {
      public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
          int dend) {
        for (int i = start; i < end; i++) {
          if (Character.getType((source.charAt(i))) == Character.CONTROL) {
            return "";
          }
        }
        return null;
      }
    };
    return returnFilter;
  }

  /**
   * Generic listener that sets the summary to the newly selected/entered value
   */
  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    preference.setSummary((CharSequence) newValue);
    return true;
  }

}
