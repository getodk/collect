package org.odk.collect.android.preferences;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.odk.collect.android.R;

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
        setUpNestedScreen(getPreferenceScreen(), view);

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

    public void setUpNestedScreen(PreferenceScreen preferenceScreen, View view) {
        LinearLayout root;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            if (getActivity() instanceof PreferencesActivity) {
                root = (LinearLayout) ((ViewGroup) view.findViewById(android.R.id.list).getRootView()).getChildAt(0);
                toolbar = (Toolbar) root.findViewById(R.id.toolbar);

            } else {
                root = (LinearLayout) view.findViewById(android.R.id.list).getParent().getParent();
                toolbar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.toolbar, root, false);
                toolbar.setTitle(preferenceScreen.getTitle());
                root.addView(toolbar, 0);

                View shadow = LayoutInflater.from(getActivity()).inflate(R.layout.toolbar_action_bar_shadow, root, false);
                root.addView(shadow, 1);
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            root = (LinearLayout) view.findViewById(android.R.id.list).getParent();
            toolbar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.toolbar, root, false);
            toolbar.setTitle(preferenceScreen.getTitle());
            root.addView(toolbar, 0);

            View shadow = LayoutInflater.from(getActivity()).inflate(R.layout.toolbar_action_bar_shadow, root, false);
            root.addView(shadow, 1);
        }
    }
}