package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
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

        // start smap disable preferences overridden by the server

        // Send Locations
        Preference location = getPreferenceScreen().findPreference(GeneralKeys.KEY_SMAP_USER_LOCATION);
        if(location != null) {
            boolean override_location = (Boolean) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SMAP_OVERRIDE_LOCATION);
            if (override_location) {
                location.setEnabled(false);
            } else {
                location.setEnabled(true);
            }
        }

        // Auto Sync
        Preference autosend = getPreferenceScreen().findPreference(GeneralKeys.KEY_AUTOSEND);
        if(autosend != null) {
            boolean override_sync = (Boolean) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SMAP_OVERRIDE_SYNC);
            if (override_sync) {
                autosend.setEnabled(false);
            } else {
                autosend.setEnabled(true);
            }
        }

        // smap Delete after send
        Preference del = getPreferenceScreen().findPreference(GeneralKeys.KEY_DELETE_AFTER_SEND);
        if(del != null) {
            boolean override_delete = (Boolean) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SMAP_OVERRIDE_DELETE);
            if (override_delete) {
                del.setEnabled(false);
            } else {
                del.setEnabled(true);
            }
        }

        // smap High res video
        Preference hrv = getPreferenceScreen().findPreference(GeneralKeys.KEY_HIGH_RESOLUTION);
        if(hrv != null) {
            boolean override_hrv = (Boolean) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SMAP_OVERRIDE_HIGH_RES_VIDEO);
            if (override_hrv) {
                hrv.setEnabled(false);
            } else {
                hrv.setEnabled(true);
            }
        }

        // smap guidance
        Preference guide = getPreferenceScreen().findPreference(GeneralKeys.KEY_GUIDANCE_HINT);
        if(guide != null) {
            boolean override_guide = (Boolean) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SMAP_OVERRIDE_GUIDANCE);
            if (override_guide) {
                guide.setEnabled(false);
            } else {
                guide.setEnabled(true);
            }
        }

        // smap image size
        Preference image_size = getPreferenceScreen().findPreference(GeneralKeys.KEY_IMAGE_SIZE);
        if(image_size != null) {
            boolean override_image_size = (Boolean) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SMAP_OVERRIDE_IMAGE_SIZE);
            if (override_image_size) {
                image_size.setEnabled(false);
            } else {
                image_size.setEnabled(true);
            }
        }

        // smap navigation
        Preference nav = getPreferenceScreen().findPreference(GeneralKeys.KEY_NAVIGATION);
        if(nav != null) {
            boolean override_nav = (Boolean) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_SMAP_OVERRIDE_NAVIGATION);
            if (override_nav) {
                nav.setEnabled(false);
            } else {
                nav.setEnabled(true);
            }
        }

        // smap backward navigation
        Preference back_nav = getPreferenceScreen().findPreference(AdminKeys.KEY_MOVING_BACKWARDS);
        if(back_nav != null) {
            boolean override_back_nav = (Boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_SMAP_OVERRIDE_MOVING_BACKWARDS);
            if (override_back_nav) {
                back_nav.setEnabled(false);
            } else {
                back_nav.setEnabled(true);
            }
        }
    }

    protected boolean isInAdminMode() {
        return getArguments() != null && getArguments().getBoolean(INTENT_KEY_ADMIN_MODE, false);
    }
}
