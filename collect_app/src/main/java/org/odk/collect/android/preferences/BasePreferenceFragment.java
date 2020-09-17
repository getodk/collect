package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import org.odk.collect.android.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.configure.SettingsChangeHandler;
import org.odk.collect.android.injection.DaggerUtils;

import javax.inject.Inject;

import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    SettingsChangeHandler settingsChangeHandler;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        super.onDisplayPreferenceDialog(preference);

        // If we don't do this there is extra padding on "Cancel" and "OK" on
        // the preference dialogs. This appears to have something to with the `updateLocale`
        // calls in `CollectAbstractActivity` and weirdly only happens for English.
        DialogPreference dialogPreference = (DialogPreference) preference;
        dialogPreference.setNegativeButtonText(R.string.cancel);
        dialogPreference.setPositiveButtonText(R.string.ok);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        if (activity instanceof CollectAbstractActivity) {
            ((CollectAbstractActivity) activity).initToolbar(getPreferenceScreen().getTitle());
        }
        removeDisabledPrefs();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        settingsChangeHandler.onSettingChanged(key, sharedPreferences.getAll().get(key));
    }

    private void removeDisabledPrefs() {
        if (!isInAdminMode()) {
            DisabledPreferencesRemover preferencesRemover = new DisabledPreferencesRemover(this);
            preferencesRemover.remove(AdminKeys.adminToGeneral);
            preferencesRemover.removeEmptyCategories();
        }
    }

    protected boolean isInAdminMode() {
        return getArguments() != null && getArguments().getBoolean(INTENT_KEY_ADMIN_MODE, false);
    }
}
