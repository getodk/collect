package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.activities.SmapMain;
import org.odk.collect.android.injection.DaggerUtils;

import static org.odk.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers;

public class ExperimentalPreferencesFragment extends BasePreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.experimental_preferences, rootKey);

        findPreference(GeneralKeys.KEY_MAGENTA_THEME).setOnPreferenceChangeListener((preference, newValue) -> {
            Intent intent = new Intent(getActivity().getBaseContext(), SmapMain.class);     // smap
            getActivity().startActivity(intent);
            //startActivityAndCloseAllOthers(requireActivity(), MainMenuActivity.class);    // smap commented out
            return true;
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }
}
