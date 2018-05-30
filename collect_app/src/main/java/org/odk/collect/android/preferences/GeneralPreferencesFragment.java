/*
 * Copyright 2018 Nafundi
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

package org.odk.collect.android.preferences;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.view.View;

import org.odk.collect.android.R;

import java.util.Collection;

import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class GeneralPreferencesFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener {

    public static GeneralPreferencesFragment newInstance(boolean adminMode) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(INTENT_KEY_ADMIN_MODE, adminMode);

        GeneralPreferencesFragment generalPreferencesFragment = new GeneralPreferencesFragment();
        generalPreferencesFragment.setArguments(bundle);
        return generalPreferencesFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_preferences);

        findPreference("protocol").setOnPreferenceClickListener(this);
        findPreference("user_interface").setOnPreferenceClickListener(this);
        findPreference("form_management").setOnPreferenceClickListener(this);
        findPreference("user_and_device_identity").setOnPreferenceClickListener(this);

        if (!getArguments().getBoolean(INTENT_KEY_ADMIN_MODE)) {
            setPreferencesVisibility();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setTitle(R.string.general_preferences);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Fragment fragment = null;
        switch (preference.getKey()) {
            case "protocol":
                fragment = new ServerPreferences();
                break;
            case "user_interface":
                fragment = new UserInterfacePreferences();
                break;
            case "form_management":
                fragment = new FormManagementPreferences();
                break;
            case "user_and_device_identity":
                fragment = new IdentityPreferences();
                break;
        }

        if (fragment != null) {
            getActivity()
                    .getFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        return true;
    }

    private void setPreferencesVisibility() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (!hasAtleastOneSettingEnabled(AdminKeys.serverKeys)) {
            preferenceScreen.removePreference(findPreference("protocol"));
        }

        if (!hasAtleastOneSettingEnabled(AdminKeys.userInterfaceKeys)) {
            preferenceScreen.removePreference(findPreference("user_interface"));
        }

        if (!hasAtleastOneSettingEnabled(AdminKeys.formManagementKeys)) {
            preferenceScreen.removePreference(findPreference("form_management"));
        }

        if (!hasAtleastOneSettingEnabled(AdminKeys.identityKeys)) {
            preferenceScreen.removePreference(findPreference("user_and_device_identity"));
        }
    }

    private boolean hasAtleastOneSettingEnabled(Collection<String> keys) {
        AdminSharedPreferences adminSharedPreferences = AdminSharedPreferences.getInstance();
        for (String key : keys) {
            boolean value = (boolean) adminSharedPreferences.get(key);
            if (value) {
                return true;
            }
        }
        return false;
    }
}
