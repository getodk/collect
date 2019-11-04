package org.odk.collect.android.preferences;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.odk.collect.android.R;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class BasePreferenceFragment extends PreferenceFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        initToolbar(getPreferenceScreen(), view);
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

    // inflates toolbar in the preference fragments
    public void initToolbar(PreferenceScreen preferenceScreen, View view) {
        LinearLayout root = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                ? (LinearLayout) view.findViewById(android.R.id.list).getParent().getParent()
                : (LinearLayout) view.findViewById(android.R.id.list).getParent();

        Toolbar toolbar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.toolbar_without_progressbar, root, false);
        toolbar.setTitle(preferenceScreen.getTitle());

        root.addView(toolbar, 0);
        View shadow = LayoutInflater.from(getActivity()).inflate(R.layout.toolbar_action_bar_shadow, root, false);
        root.addView(shadow, 1);
    }

    private void removeAllDisabledPrefs() {
        DisabledPreferencesRemover preferencesRemover = new DisabledPreferencesRemover((PreferencesActivity) getActivity(), this);
        preferencesRemover.remove(AdminKeys.adminToGeneral);
        preferencesRemover.removeEmptyCategories();
    }
}
