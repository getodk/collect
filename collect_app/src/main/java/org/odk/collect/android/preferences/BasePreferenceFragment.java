package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.preference.Preference;
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

    private void removeAllDisabledPrefs() {
        DisabledPreferencesRemover preferencesRemover = new DisabledPreferencesRemover((PreferencesActivity) getActivity(), this);
        preferencesRemover.remove(AdminKeys.adminToGeneral);
        preferencesRemover.removeEmptyCategories();
    }
}
