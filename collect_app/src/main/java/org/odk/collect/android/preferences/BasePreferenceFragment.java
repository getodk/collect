package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

import org.odk.collect.android.activities.CollectAbstractActivity;

import androidx.annotation.Nullable;

import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class BasePreferenceFragment extends PreferenceFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((CollectAbstractActivity) getActivity()).initToolbar(getPreferenceScreen().getTitle());
        removeDisabledPrefs();

        super.onViewCreated(view, savedInstanceState);
    }

    void removeDisabledPrefs() {
        // removes disabled preferences if in general settings
        if (getActivity() instanceof PreferencesActivity) {
            Bundle args = getArguments();
            if (args != null) {
                final boolean adminMode = getArguments().getBoolean(INTENT_KEY_ADMIN_MODE, false);
                if (!adminMode) {
                    removeAllDisabledPrefs();
                }
            } else {
                removeAllDisabledPrefs();
            }
        }
    }

    private void removeAllDisabledPrefs() {
        DisabledPreferencesRemover preferencesRemover = new DisabledPreferencesRemover((PreferencesActivity) getActivity(), this);
        preferencesRemover.remove(AdminKeys.adminToGeneral);
        preferencesRemover.removeEmptyCategories();
    }
}
