package org.odk.collect.android.preferences;

import android.preference.CheckBoxPreference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, packageName = "org.odk.collect")
public class AdminSettingsTest {

    /** Tests the “Delete after send” checkboxes */
    @Test
    public void disableGetBlank() {
        AdminPreferencesFragment adminPrefFrag = new AdminPreferencesFragment();
        startFragment(adminPrefFrag);
        CheckBoxPreference adminGetBlank = (CheckBoxPreference) adminPrefFrag.findPreference("delete_after_send");
        adminGetBlank.setChecked(true);

        PreferencesFragment prefFrag = new PreferencesFragment();
        startFragment(prefFrag);
        CheckBoxPreference getBlank = (CheckBoxPreference) adminPrefFrag.findPreference("delete_after_send");
        getBlank.setChecked(true);
    }
}
