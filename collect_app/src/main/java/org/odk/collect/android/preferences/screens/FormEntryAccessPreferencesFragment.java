package org.odk.collect.android.preferences.screens;

import static org.odk.collect.android.fragments.dialogs.MovingBackwardsDialog.MOVING_BACKWARDS_DIALOG_TAG;
import static org.odk.collect.settings.keys.ProjectKeys.CONSTRAINT_BEHAVIOR_ON_SWIPE;
import static org.odk.collect.settings.keys.ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM;
import static org.odk.collect.settings.keys.ProtectedProjectKeys.KEY_EDIT_SAVED;
import static org.odk.collect.settings.keys.ProtectedProjectKeys.KEY_JUMP_TO;
import static org.odk.collect.settings.keys.ProtectedProjectKeys.KEY_MOVING_BACKWARDS;
import static org.odk.collect.settings.keys.ProtectedProjectKeys.KEY_SAVE_MID;

import android.os.Bundle;

import androidx.preference.CheckBoxPreference;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.fragments.dialogs.MovingBackwardsDialog;
import org.odk.collect.android.fragments.dialogs.SimpleDialog;
import org.odk.collect.settings.keys.ProjectKeys;

public class FormEntryAccessPreferencesFragment extends BaseAdminPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.form_entry_access_preferences);

        findPreference(KEY_MOVING_BACKWARDS).setOnPreferenceChangeListener((preference, newValue) -> {
            if (((CheckBoxPreference) preference).isChecked()) {
                new MovingBackwardsDialog().show(getActivity().getSupportFragmentManager(), MOVING_BACKWARDS_DIALOG_TAG);
            } else {
                SimpleDialog.newInstance(getActivity().getString(R.string.moving_backwards_enabled_title), 0, getActivity().getString(R.string.moving_backwards_enabled_message), getActivity().getString(R.string.ok), false).show(((CollectAbstractActivity) getActivity()).getSupportFragmentManager(), SimpleDialog.COLLECT_DIALOG_TAG);
                onMovingBackwardsEnabled();
            }
            return true;
        });
        findPreference(KEY_JUMP_TO).setEnabled(settingsProvider.getProtectedSettings().getBoolean(ALLOW_OTHER_WAYS_OF_EDITING_FORM));
        findPreference(KEY_SAVE_MID).setEnabled(settingsProvider.getProtectedSettings().getBoolean(ALLOW_OTHER_WAYS_OF_EDITING_FORM));
    }

    public void preventOtherWaysOfEditingForm() {
        settingsProvider.getProtectedSettings().save(ALLOW_OTHER_WAYS_OF_EDITING_FORM, false);
        settingsProvider.getProtectedSettings().save(KEY_EDIT_SAVED, false);
        settingsProvider.getProtectedSettings().save(KEY_SAVE_MID, false);
        settingsProvider.getProtectedSettings().save(KEY_JUMP_TO, false);
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_CONSTRAINT_BEHAVIOR, CONSTRAINT_BEHAVIOR_ON_SWIPE);

        findPreference(KEY_JUMP_TO).setEnabled(false);
        findPreference(KEY_SAVE_MID).setEnabled(false);

        ((CheckBoxPreference) findPreference(KEY_JUMP_TO)).setChecked(false);
        ((CheckBoxPreference) findPreference(KEY_SAVE_MID)).setChecked(false);
    }

    private void onMovingBackwardsEnabled() {
        settingsProvider.getProtectedSettings().save(ALLOW_OTHER_WAYS_OF_EDITING_FORM, true);
        findPreference(KEY_JUMP_TO).setEnabled(true);
        findPreference(KEY_SAVE_MID).setEnabled(true);
    }
}
