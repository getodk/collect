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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.SharedPreferencesUtils;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.odk.collect.android.preferences.AdminKeys.KEY_EDIT_SAVED;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_COMPLETED_DEFAULT;

@RunWith(AndroidJUnit4.class)
public class SharedPreferencesTest {

    @Test
    public void generalDefaultSharedPreferencesTest() {
        GeneralSharedPreferences.getInstance().loadDefaultPreferences();
        HashMap<String, Object> defaultValues = PreferenceKeys.GENERAL_KEYS;

        GeneralSharedPreferences generalSharedPreferences = GeneralSharedPreferences.getInstance();
        for (String key : SharedPreferencesUtils.getAllGeneralKeys()) {
            assertEquals(generalSharedPreferences.get(key), defaultValues.get(key));
        }
    }

    @Test
    public void adminDefaultSharedPreferencesTest() {
        AdminSharedPreferences.getInstance().loadDefaultPreferences();

        AdminSharedPreferences adminSharedPreferences = AdminSharedPreferences.getInstance();
        for (String key : SharedPreferencesUtils.getAllAdminKeys()) {
            assertEquals(adminSharedPreferences.get(key), adminSharedPreferences.getDefault(key));
        }
    }

    @Test
    public void generalSharedPreferencesUpgradeTest() {
        GeneralSharedPreferences.getInstance().save(KEY_COMPLETED_DEFAULT, false);

        GeneralSharedPreferences.getInstance().reloadPreferences();
        HashMap<String, Object> defaultValues = PreferenceKeys.GENERAL_KEYS;

        GeneralSharedPreferences generalSharedPreferences = GeneralSharedPreferences.getInstance();
        for (String key : SharedPreferencesUtils.getAllGeneralKeys()) {
            if (key.equals(KEY_COMPLETED_DEFAULT)) {
                assertFalse((boolean) generalSharedPreferences.get(key));
            } else {
                assertEquals(generalSharedPreferences.get(key), defaultValues.get(key));
            }
        }
    }

    @Test
    public void adminSharedPreferencesUpgradeTest() {
        AdminSharedPreferences.getInstance().save(KEY_EDIT_SAVED, false);
        AdminSharedPreferences.getInstance().reloadPreferences();

        AdminSharedPreferences adminSharedPreferences = AdminSharedPreferences.getInstance();
        for (String key : SharedPreferencesUtils.getAllAdminKeys()) {
            if (key.equals(KEY_EDIT_SAVED)) {
                assertFalse((boolean) adminSharedPreferences.get(key));
            } else {
                assertEquals(adminSharedPreferences.get(key), adminSharedPreferences.getDefault(key));
            }
        }
    }
}