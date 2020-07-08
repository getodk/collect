package org.odk.collect.android.preferences;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.backgroundwork.BackgroundWorkManager;
import org.odk.collect.android.injection.DaggerUtils;

import javax.inject.Inject;

public class ExperimentalPreferencesFragment extends PreferenceFragmentCompat {

    @Inject
    BackgroundWorkManager backgroundWorkManager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.experimental_preferences, rootKey);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentActivity activity = getActivity();
        if (activity instanceof CollectAbstractActivity) {
            ((CollectAbstractActivity) activity).initToolbar(getPreferenceScreen().getTitle());
        }

        SwitchPreferenceCompat matchExactly = findPreference("match_exactly");
        matchExactly.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                backgroundWorkManager.scheduleMatchExactlySync();
            } else {
                backgroundWorkManager.cancelMatchExactlySync();
            }

            return true;
        });
    }

}
