package org.odk.collect.android.preferences.screens;

import android.os.Bundle;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.FormUpdateMode;

import static org.odk.collect.android.configure.SettingsUtils.getFormUpdateMode;
import static org.odk.collect.android.preferences.keys.AdminKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_EDIT_SAVED;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_GET_BLANK;
import static org.odk.collect.android.preferences.utilities.PreferencesUtils.displayDisabled;

public class MainMenuAccessPreferencesFragment extends BaseAdminPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.main_menu_access_preferences, rootKey);
        findPreference(KEY_EDIT_SAVED).setEnabled(settingsProvider.getAdminSettings().getBoolean(ALLOW_OTHER_WAYS_OF_EDITING_FORM));

        FormUpdateMode formUpdateMode = getFormUpdateMode(requireContext(), settingsProvider.getGeneralSettings());
        if (formUpdateMode == FormUpdateMode.MATCH_EXACTLY) {
            displayDisabled(findPreference(KEY_GET_BLANK), false);
        }
    }
}