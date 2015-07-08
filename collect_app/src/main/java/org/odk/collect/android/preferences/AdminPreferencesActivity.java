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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.preference.ListPreference;
import android.preference.Preference;

import org.javarosa.core.model.FormDef;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.CompatibilityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Handles admin preferences, which are password-protectable and govern which app features and
 * general preferences the end user of the app will be able to see.
 *
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com; constraint behavior option)
 */
public class AdminPreferencesActivity extends PreferenceActivity {

    public static String ADMIN_PREFERENCES = "admin_prefs";

    // key for this preference screen
    public static String KEY_ADMIN_PW = "admin_pw";

    // keys for each preference
    // main menu
    public static String KEY_EDIT_SAVED = "edit_saved";
    public static String KEY_SEND_FINALIZED = "send_finalized";
    public static String KEY_GET_BLANK = "get_blank";
    public static String KEY_DELETE_SAVED = "delete_saved";
    // server
    public static String KEY_CHANGE_SERVER = "change_server";
    public static String KEY_CHANGE_USERNAME = "change_username";
    public static String KEY_CHANGE_PASSWORD = "change_password";
    public static String KEY_CHANGE_GOOGLE_ACCOUNT = "change_google_account";
    public static String KEY_CHANGE_PROTOCOL_SETTINGS = "change_protocol_settings";
    // client
    public static String KEY_CHANGE_FONT_SIZE = "change_font_size";
    public static String KEY_DEFAULT_TO_FINALIZED = "default_to_finalized";
    public static String KEY_HIGH_RESOLUTION = "high_resolution";
    public static String KEY_SHOW_SPLASH_SCREEN = "show_splash_screen";
    public static String KEY_SELECT_SPLASH_SCREEN = "select_splash_screen";
    public static String KEY_DELETE_AFTER_SEND = "delete_after_send";
    // form entry
    public static String KEY_SAVE_MID = "save_mid";
    public static String KEY_JUMP_TO = "jump_to";
    public static String KEY_CHANGE_LANGUAGE = "change_language";
    public static String KEY_ACCESS_SETTINGS = "access_settings";
    public static String KEY_SAVE_AS = "save_as";
    public static String KEY_MARK_AS_FINALIZED = "mark_as_finalized";

    public static String KEY_AUTOSEND_WIFI = "autosend_wifi";
    public static String KEY_AUTOSEND_NETWORK = "autosend_network";

    public static String KEY_NAVIGATION = "navigation";
    public static String KEY_CONSTRAINT_BEHAVIOR = "constraint_behavior";

    public static String KEY_FORM_PROCESSING_LOGIC = "form_processing_logic";

    private static final int SAVE_PREFS_MENU = Menu.FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name) + " > "
                + getString(R.string.admin_preferences));

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(ADMIN_PREFERENCES);
        prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);

        addPreferencesFromResource(R.xml.admin_preferences);

        ListPreference mFormProcessingLogicPreference = (ListPreference) findPreference(KEY_FORM_PROCESSING_LOGIC);
        mFormProcessingLogicPreference.setSummary(mFormProcessingLogicPreference.getEntry());
        mFormProcessingLogicPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            }
        });
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Collect.getInstance().getActivityLogger()
			.logAction(this, "onCreateOptionsMenu", "show");
		super.onCreateOptionsMenu(menu);

		CompatibilityUtils.setShowAsAction(
    		menu.add(0, SAVE_PREFS_MENU, 0, R.string.save_preferences)
				.setIcon(R.drawable.ic_menu_save),
			MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case SAVE_PREFS_MENU:
			File writeDir = new File(Collect.ODK_ROOT + "/settings");
			if (!writeDir.exists()) {
				if (!writeDir.mkdirs()) {
					Toast.makeText(
							this,
							"Error creating directory "
									+ writeDir.getAbsolutePath(),
							Toast.LENGTH_SHORT).show();
					return false;
				}
			}

			File dst = new File(writeDir.getAbsolutePath()
					+ "/collect.settings");
			boolean success = AdminPreferencesActivity.saveSharedPreferencesToFile(dst, this);
			if (success) {
				Toast.makeText(
						this,
						"Settings successfully written to "
								+ dst.getAbsolutePath(), Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(this,
						"Error writing settings to " + dst.getAbsolutePath(),
						Toast.LENGTH_LONG).show();
			}
			return true;

		}
		return super.onOptionsItemSelected(item);
	}


	public static boolean saveSharedPreferencesToFile(File dst, Context context) {
		// this should be in a thread if it gets big, but for now it's tiny
		boolean res = false;
		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(dst));
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(context);
			SharedPreferences adminPreferences = context.getSharedPreferences(
					AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

			output.writeObject(pref.getAll());
			output.writeObject(adminPreferences.getAll());

			res = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return res;
	}

    public static FormDef.EvalBehavior getConfiguredFormProcessingLogic(Context context) {
        FormDef.EvalBehavior mode;

        SharedPreferences adminPreferences = context.getSharedPreferences(ADMIN_PREFERENCES, 0);
        String formProcessingLoginIndex = adminPreferences.getString(KEY_FORM_PROCESSING_LOGIC, context.getString(R.string.default_form_processing_logic));
        try {
            if ("-1".equals(formProcessingLoginIndex)) {
                mode = FormDef.recommendedMode;
            } else {
					int preferredModeIndex = Integer.parseInt(formProcessingLoginIndex);
					switch (preferredModeIndex) {
						case 0: {
							mode = FormDef.EvalBehavior.Fast_2014;
							break;
						}
						case 1: {
							mode = FormDef.EvalBehavior.Safe_2014;
							break;
						}
						case 2: {
							mode = FormDef.EvalBehavior.April_2014;
							break;
						}
						case 3: {
							mode = FormDef.EvalBehavior.Legacy;
							break;
						}
						default: {
							mode = FormDef.recommendedMode;
							break;
						}
					}
				}
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("AdminPreferencesActivity", "Unable to get EvalBehavior -- defaulting to recommended mode");
            mode = FormDef.recommendedMode;
        }

        return mode;
    }
}
