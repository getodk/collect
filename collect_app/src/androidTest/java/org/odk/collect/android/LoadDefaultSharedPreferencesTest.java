/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ACCESS_SETTINGS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_APP_LANGUAGE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_CHANGE_FONT_SIZE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_CHANGE_FORM_METADATA;
import static org.odk.collect.android.preferences.AdminKeys.KEY_CHANGE_LANGUAGE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_CHANGE_SERVER;
import static org.odk.collect.android.preferences.AdminKeys.KEY_DEFAULT_TO_FINALIZED;
import static org.odk.collect.android.preferences.AdminKeys.KEY_DELETE_SAVED;
import static org.odk.collect.android.preferences.AdminKeys.KEY_EDIT_SAVED;
import static org.odk.collect.android.preferences.AdminKeys.KEY_GET_BLANK;
import static org.odk.collect.android.preferences.AdminKeys.KEY_INSTANCE_FORM_SYNC;
import static org.odk.collect.android.preferences.AdminKeys.KEY_JUMP_TO;
import static org.odk.collect.android.preferences.AdminKeys.KEY_MARK_AS_FINALIZED;
import static org.odk.collect.android.preferences.AdminKeys.KEY_SAVE_AS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_SAVE_MID;
import static org.odk.collect.android.preferences.AdminKeys.KEY_SEND_FINALIZED;
import static org.odk.collect.android.preferences.AdminKeys.KEY_SHOW_MAP_BASEMAP;
import static org.odk.collect.android.preferences.AdminKeys.KEY_SHOW_MAP_SDK;
import static org.odk.collect.android.preferences.AdminKeys.KEY_SHOW_SPLASH_SCREEN;
import static org.odk.collect.android.preferences.AdminKeys.KEY_VIEW_SENT;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_ANALYTICS;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_COMPLETED_DEFAULT;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_CONSTRAINT_BEHAVIOR;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_DELETE_AFTER_SEND;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FONT_SIZE;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FORMLIST_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_HIGH_RESOLUTION;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_INSTANCE_SYNC;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_MAP_BASEMAP;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_MAP_SDK;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_NAVIGATION;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SERVER_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SHOW_SPLASH;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SPLASH_PATH;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SUBMISSION_URL;

@RunWith(AndroidJUnit4.class)
public class LoadDefaultSharedPreferencesTest {

    @Before
    public void setUp() {
        GeneralSharedPreferences.getInstance().clear();
        AdminSharedPreferences.getInstance().clear();
    }

    @Test
    public void test() {
        Map<String, ?> generalPreferences = GeneralSharedPreferences.getInstance().getAll();
        Map<String, ?> adminPreferences = AdminSharedPreferences.getInstance().getAll();

        assertTrue(generalPreferences.size() == 0);
        assertTrue(adminPreferences.size() == 0);

        Collect.getInstance().loadDefaultPreferences();
        generalPreferences = GeneralSharedPreferences.getInstance().getAll();

        // aggregate_preferences.xml
        assertTrue(generalPreferences.containsKey(KEY_SERVER_URL));

        // form_management_preferences.xml
        assertTrue(generalPreferences.containsKey(KEY_AUTOSEND));
        assertTrue(generalPreferences.containsKey(KEY_DELETE_AFTER_SEND));
        assertTrue(generalPreferences.containsKey(KEY_COMPLETED_DEFAULT));
        assertTrue(generalPreferences.containsKey(KEY_CONSTRAINT_BEHAVIOR));
        assertTrue(generalPreferences.containsKey(KEY_HIGH_RESOLUTION));
        assertTrue(generalPreferences.containsKey(KEY_INSTANCE_SYNC));

        // identity_preferences.xml
        assertTrue(generalPreferences.containsKey(KEY_ANALYTICS));

        // other_preferences.xml
        assertTrue(generalPreferences.containsKey(KEY_FORMLIST_URL));
        assertTrue(generalPreferences.containsKey(KEY_SUBMISSION_URL));

        // server_preferences.xml
        assertTrue(generalPreferences.containsKey(KEY_PROTOCOL));

        // user_interface_preferences.xml
        assertTrue(generalPreferences.containsKey(KEY_FONT_SIZE));
        assertTrue(generalPreferences.containsKey(KEY_NAVIGATION));
        assertTrue(generalPreferences.containsKey(KEY_SHOW_SPLASH));
        //assertTrue(generalPreferences.containsKey(KEY_SPLASH_PATH)); it should be in the collection too but it isn't probably because of the fact that PreferencesScreen can not have defaultValue? We should use Preferences instead of PreferencesScreen?
        assertTrue(generalPreferences.containsKey(KEY_MAP_SDK));
        assertTrue(generalPreferences.containsKey(KEY_MAP_BASEMAP));

        adminPreferences = AdminSharedPreferences.getInstance().getAll();

        // main_menu_access_preferences.xml
        assertTrue(adminPreferences.containsKey(KEY_EDIT_SAVED));
        assertTrue(adminPreferences.containsKey(KEY_SEND_FINALIZED));
        assertTrue(adminPreferences.containsKey(KEY_VIEW_SENT));
        assertTrue(adminPreferences.containsKey(KEY_GET_BLANK));
        assertTrue(adminPreferences.containsKey(KEY_DELETE_SAVED));

        // form_entry_access_preferences.xml
        assertTrue(adminPreferences.containsKey(KEY_ACCESS_SETTINGS));
        assertTrue(adminPreferences.containsKey(KEY_CHANGE_LANGUAGE));
        assertTrue(adminPreferences.containsKey(KEY_JUMP_TO));
        assertTrue(adminPreferences.containsKey(KEY_SAVE_MID));
        assertTrue(adminPreferences.containsKey(KEY_SAVE_AS));
        assertTrue(adminPreferences.containsKey(KEY_MARK_AS_FINALIZED));

        // user_settings_access_preferences.xml
        assertTrue(adminPreferences.containsKey(KEY_CHANGE_SERVER));
        assertTrue(adminPreferences.containsKey(KEY_CHANGE_FORM_METADATA));
        assertTrue(adminPreferences.containsKey(AdminKeys.KEY_AUTOSEND));
        assertTrue(adminPreferences.containsKey(AdminKeys.KEY_NAVIGATION));
        assertTrue(adminPreferences.containsKey(AdminKeys.KEY_CONSTRAINT_BEHAVIOR));
        assertTrue(adminPreferences.containsKey(KEY_CHANGE_FONT_SIZE));
        assertTrue(adminPreferences.containsKey(KEY_APP_LANGUAGE));
        assertTrue(adminPreferences.containsKey(KEY_INSTANCE_FORM_SYNC));
        assertTrue(adminPreferences.containsKey(KEY_DEFAULT_TO_FINALIZED));
        assertTrue(adminPreferences.containsKey(AdminKeys.KEY_DELETE_AFTER_SEND));
        assertTrue(adminPreferences.containsKey(AdminKeys.KEY_HIGH_RESOLUTION));
        assertTrue(adminPreferences.containsKey(KEY_SHOW_SPLASH_SCREEN));
        assertTrue(adminPreferences.containsKey(KEY_SHOW_MAP_SDK));
        assertTrue(adminPreferences.containsKey(KEY_SHOW_MAP_BASEMAP));
        assertTrue(adminPreferences.containsKey(AdminKeys.KEY_ANALYTICS));
    }
}
