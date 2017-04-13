package org.odk.collect.android.preferences;

import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.util.FragmentTestUtil.startFragment;

/** Tests for Admin Preferences */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class AdminPreferencesActivityTest {

    private AdminPreferencesFragment adminPreferencesFragment;
    private SharedPreferences sharedPreferences;

    public AdminPreferencesActivityTest(String id, String adminKey) {
        this.id = id;
        this.adminKey = adminKey;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                //Main menu preferences
                {"edit_saved", AdminKeys.KEY_EDIT_SAVED},
                {"send_finalized", AdminKeys.KEY_SEND_FINALIZED},
                {"view_sent", AdminKeys.KEY_VIEW_SENT},
                {"get_blank", AdminKeys.KEY_GET_BLANK},
                {"delete_saved", AdminKeys.KEY_DELETE_SAVED},

                //Change setting preferences
                {"change_server", AdminKeys.KEY_CHANGE_SERVER},
                {"change_protocol_settings", AdminKeys.KEY_CHANGE_PROTOCOL_SETTINGS},
                {"change_username", AdminKeys.KEY_CHANGE_USERNAME},
                {"change_password", AdminKeys.KEY_CHANGE_PASSWORD},
                {"change_google_account", AdminKeys.KEY_CHANGE_GOOGLE_ACCOUNT},
                {"autosend_wifi", AdminKeys.KEY_AUTOSEND_WIFI},
                {"autosend_network", AdminKeys.KEY_AUTOSEND_NETWORK},
                {"navigation", AdminKeys.KEY_NAVIGATION},
                {"constraint_behavior", AdminKeys.KEY_CONSTRAINT_BEHAVIOR},
                {"change_font_size", AdminKeys.KEY_CHANGE_FONT_SIZE},
                {"instance_form_sync", AdminKeys.KEY_INSTANCE_FORM_SYNC},
                {"default_to_finalized", AdminKeys.KEY_DEFAULT_TO_FINALIZED},
                {"delete_after_send", AdminKeys.KEY_DELETE_AFTER_SEND},
                {"high_resolution", AdminKeys.KEY_HIGH_RESOLUTION},
                {"show_splash_screen", AdminKeys.KEY_SHOW_SPLASH_SCREEN},
                {"show_map_sdk", AdminKeys.KEY_SHOW_MAP_SDK},
                {"show_map_basemap", AdminKeys.KEY_SHOW_MAP_BASEMAP},
                {"analytics", AdminKeys.KEY_ANALYTICS},

                //Form entry preferences
                {"access_settings", AdminKeys.KEY_ACCESS_SETTINGS},
                {"change_language", AdminKeys.KEY_CHANGE_LANGUAGE},
                {"jump_to", AdminKeys.KEY_JUMP_TO},
                {"save_mid", AdminKeys.KEY_SAVE_MID},
                {"save_as", AdminKeys.KEY_SAVE_AS},
                {"mark_as_finalized", AdminKeys.KEY_MARK_AS_FINALIZED},

                {"timer_log", AdminKeys.KEY_TIMER_LOG_ENABLED}
        });
    }

    /**
     * Runs {@link Before} each test.
     */
    @Before
    public void setUp() throws Exception {
        adminPreferencesFragment = new AdminPreferencesFragment();
        startFragment(adminPreferencesFragment);

        sharedPreferences = adminPreferencesFragment.getActivity().
                getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0);
    }

    @Test
    public void shouldUpdateAdminSharedPreferences() throws NullPointerException {
        for (String adminKey : AdminKeys.ALL_KEYS) {
            Preference preference = adminPreferencesFragment.findPreference(adminKey);
            if (preference instanceof CheckBoxPreference) {
                System.out.println("Testing " + adminKey);
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;

                assertNotNull("Preference not found: " + adminKey, checkBoxPreference);
                checkBoxPreference.setChecked(true);
                boolean actual = sharedPreferences.getBoolean(adminKey, false);
                assertTrue("Error in preference " + adminKey, actual);

                checkBoxPreference.setChecked(false);
                actual = sharedPreferences.getBoolean(adminKey, true);
                assertFalse("Error in preference " + adminKey, actual);
            }
        }
    }
}
