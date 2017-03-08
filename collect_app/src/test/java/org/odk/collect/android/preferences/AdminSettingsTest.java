package org.odk.collect.android.preferences;

import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, packageName = "org.odk.collect")
public class AdminSettingsTest { // ToDo: Name should match class being tested

    @Test
    public void shouldUpdateAdminSharedPrefs() {
        AdminPreferencesFragment adminPrefFrag = new AdminPreferencesFragment();
        startFragment(adminPrefFrag);
        CheckBoxPreference adminGetBlank = (CheckBoxPreference)
                adminPrefFrag.findPreference("delete_after_send");
        adminGetBlank.setChecked(false);
        SharedPreferences prefs = adminPrefFrag.getActivity().getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);
        Boolean deleteAfterSend = prefs.getBoolean(AdminKeys.KEY_DELETE_AFTER_SEND, false);
        assertEquals(deleteAfterSend, false);
    }

    /*
    For testing that the admin preferences toggles are working, could we split them into
    two parts?

    1) One that makes sure that the buttons on the admin preferences screen
    update state properly...

    2) that makes sure that based on state, the user preferences are properly shown or not?
    */
}
