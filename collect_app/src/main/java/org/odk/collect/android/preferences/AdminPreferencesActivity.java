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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.javarosa.core.model.FormDef;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static org.odk.collect.android.preferences.AdminKeys.KEY_FORM_PROCESSING_LOGIC;

/**
 * Handles admin preferences, which are password-protectable and govern which app features and
 * general preferences the end user of the app will be able to see.
 *
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com; constraint behavior
 *         option)
 */
public class AdminPreferencesActivity extends PreferenceActivity {

    public static final String ADMIN_PREFERENCES = "admin_prefs";

    // key for this preference screen
    public static final String KEY_ADMIN_PW = "admin_pw";

    // keys for each preference
    // main menu
    public static final String KEY_EDIT_SAVED = "edit_saved";
    public static final String KEY_SEND_FINALIZED = "send_finalized";
    public static final String KEY_VIEW_SENT = "view_sent";
    public static final String KEY_GET_BLANK = "get_blank";
    public static final String KEY_DELETE_SAVED = "delete_saved";
    // server
    public static final String KEY_CHANGE_SERVER = "change_server";
    public static final String KEY_CHANGE_USERNAME = "change_username";
    public static final String KEY_CHANGE_PASSWORD = "change_password";
    public static final String KEY_CHANGE_ADMIN_PASSWORD = "admin_password";
    public static final String KEY_CHANGE_GOOGLE_ACCOUNT = "change_google_account";
    public static final String KEY_CHANGE_PROTOCOL_SETTINGS = "change_protocol_settings";
    // client
    public static final String KEY_CHANGE_FONT_SIZE = "change_font_size";
    public static final String KEY_DEFAULT_TO_FINALIZED = "default_to_finalized";
    public static final String KEY_HIGH_RESOLUTION = "high_resolution";
    public static final String KEY_SHOW_SPLASH_SCREEN = "show_splash_screen";
    public static final String KEY_SELECT_SPLASH_SCREEN = "select_splash_screen";
    public static final String KEY_DELETE_AFTER_SEND = "delete_after_send";
    // form entry
    public static final String KEY_SAVE_MID = "save_mid";
    public static final String KEY_JUMP_TO = "jump_to";
    public static final String KEY_CHANGE_LANGUAGE = "change_language";
    public static final String KEY_ACCESS_SETTINGS = "access_settings";
    public static final String KEY_SAVE_AS = "save_as";
    public static final String KEY_MARK_AS_FINALIZED = "mark_as_finalized";

    public static final String KEY_AUTOSEND_WIFI = "autosend_wifi";
    public static final String KEY_AUTOSEND_NETWORK = "autosend_network";

    public static final String KEY_NAVIGATION = "navigation";
    public static final String KEY_CONSTRAINT_BEHAVIOR = "constraint_behavior";

    public static final String KEY_FORM_PROCESSING_LOGIC = "form_processing_logic";

    public static final String KEY_SHOW_MAP_SDK = "show_map_sdk";
    public static final String KEY_SHOW_MAP_BASEMAP = "show_map_basemap";

    public static final String KEY_ANALYTICS = "analytics";

    private static final int SAVE_PREFS_MENU = Menu.FIRST;
    public static final String ADMIN_PREFERENCES = "admin_prefs";

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
        String formProcessingLoginIndex = adminPreferences.getString(KEY_FORM_PROCESSING_LOGIC,
                context.getString(R.string.default_form_processing_logic));
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
            Log.w("AdminPrefActivity",
                    "Unable to get EvalBehavior -- defaulting to recommended mode");
            mode = FormDef.recommendedMode;
        }

        return mode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new AdminPreferencesFragment()).commit();
        setTitle(getString(R.string.admin_preferences));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger()
                .logAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, SAVE_PREFS_MENU, 0, R.string.save_preferences)
                .setIcon(R.drawable.ic_menu_save)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SAVE_PREFS_MENU:
                File writeDir = new File(Collect.SETTINGS);
                if (!writeDir.exists()) {
                    if (!writeDir.mkdirs()) {
                        ToastUtils.showShortToast("Error creating directory "
                                        + writeDir.getAbsolutePath());
                        return false;
                    }
                }

                File dst = new File(writeDir.getAbsolutePath()
                        + "/collect.settings");
                boolean success = AdminPreferencesActivity.saveSharedPreferencesToFile(dst, this);
                if (success) {
                    ToastUtils.showLongToast("Settings successfully written to "
                            + dst.getAbsolutePath());
                } else {
                    ToastUtils.showLongToast("Error writing settings to " + dst.getAbsolutePath());
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
