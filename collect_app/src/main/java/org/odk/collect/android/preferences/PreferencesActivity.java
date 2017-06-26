/*
 * Copyright (C) 2017 Shobhit
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

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import org.javarosa.core.services.IPropertyManager;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.PropertyManager;

import java.util.Collection;
import java.util.List;

/**
 * Handles general preferences.
 */
public class PreferencesActivity extends PreferenceActivity {
    public static final String INTENT_KEY_ADMIN_MODE = "adminMode";

    private AdminSharedPreferences sharedPreferences;

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);

        sharedPreferences = AdminSharedPreferences.getInstance();

        final boolean adminMode = getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);

        if (adminMode) {
            loadHeadersFromResource(R.xml.general_preference_headers, target);
        } else {

            if (hasAtleastOneSettingEnabled(AdminKeys.serverKeys)) {
                loadHeadersFromResource(R.xml.server_preference_headers, target);
            }

            if (hasAtleastOneSettingEnabled(AdminKeys.userInterfaceKeys)) {
                loadHeadersFromResource(R.xml.user_interface_preference_headers, target);
            }

            if (hasAtleastOneSettingEnabled(AdminKeys.formManagementKeys)) {
                loadHeadersFromResource(R.xml.form_management_preference_headers, target);
            }

            if (hasAtleastOneSettingEnabled(AdminKeys.identityKeys)) {
                loadHeadersFromResource(R.xml.user_device_identity_preference_header, target);
            }
        }
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        final boolean adminMode = getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);

        if (adminMode) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(INTENT_KEY_ADMIN_MODE, true);
            header.fragmentArguments = bundle;
        }

        super.onHeaderClick(header, position);
    }

    private boolean hasAtleastOneSettingEnabled(Collection<String> keys) {
        for (String key : keys) {
            boolean value = (boolean) sharedPreferences.get(key);
            if (value) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ViewGroup root = getRootView();
        Toolbar toolbar = (Toolbar) View.inflate(this, R.layout.toolbar, null);
        toolbar.setTitle(R.string.general_preferences);
        View shadow = View.inflate(this, R.layout.toolbar_action_bar_shadow, null);

        root.addView(toolbar, 0);
        root.addView(shadow, 1);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // the property manager should be re-assigned, as properties
        // may have changed.
        IPropertyManager mgr = new PropertyManager(this);
        FormController.initializeJavaRosa(mgr);
    }

    private ViewGroup getRootView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return (ViewGroup) findViewById(android.R.id.list).getParent().getParent().getParent();
        } else {
            return (ViewGroup) findViewById(android.R.id.list).getParent();
        }
    }
}
