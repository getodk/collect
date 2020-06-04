package org.odk.collect.android.preferences;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;

public class CustomServerPathsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.custom_server_paths_preferences, rootKey);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentActivity activity = getActivity();
        if (activity instanceof CollectAbstractActivity) {
            ((CollectAbstractActivity) activity).initToolbar(getPreferenceScreen().getTitle());
        }
    }
}
