package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class BasePreferenceFragment extends PreferenceFragment {

    protected Toolbar toolbar;

    private void removeAllDisabledPrefs() {
        DisabledPreferencesRemover preferencesRemover = new DisabledPreferencesRemover((PreferencesActivity) getActivity(), this);
        preferencesRemover.remove(AdminKeys.adminToGeneral);
        preferencesRemover.removeEmptyCategories();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final ViewGroup viewGroup = (ViewGroup) getActivity().findViewById(android.R.id.content).getParent();
        toolbar = (Toolbar) viewGroup.getChildAt(0);

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

        super.onViewCreated(view, savedInstanceState);
    }
}
