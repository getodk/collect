package org.odk.collect.android.preferences;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.odk.collect.android.R;


public class BasePreferenceFragment extends PreferenceFragment {

    protected Toolbar toolbar;

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        if (preference != null) {
            if (preference instanceof PreferenceScreen) {
                if (((PreferenceScreen) preference).getDialog() != null) {
                    ((PreferenceScreen) preference).getDialog().getWindow().getDecorView().setBackgroundDrawable(getActivity().getWindow().getDecorView().getBackground().getConstantState().newDrawable());
                    setUpNestedScreen((PreferenceScreen) preference, null);
                }
            }
        }

        return false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setUpNestedScreen(getPreferenceScreen(), view);
        toolbar.setTitle(R.string.general_preferences);
        super.onViewCreated(view, savedInstanceState);
    }

    public void setUpNestedScreen(PreferenceScreen preferenceScreen, View view) {
        LinearLayout root = null;
        Dialog dialog = preferenceScreen.getDialog();

        if (view != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                root = (LinearLayout) view.findViewById(android.R.id.list).getParent().getParent();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                root = (LinearLayout) view.findViewById(android.R.id.list).getParent();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                root = (LinearLayout) dialog.findViewById(android.R.id.list).getParent().getParent();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                root = (LinearLayout) dialog.findViewById(android.R.id.list).getParent();
            }
        }
        toolbar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.toolbar, root, false);
        toolbar.setTitle(preferenceScreen.getTitle());
        root.addView(toolbar, 0);

        View shadow = LayoutInflater.from(getActivity()).inflate(R.layout.toolbar_action_bar_shadow, root, false);
        root.addView(shadow, 1);
    }
}
