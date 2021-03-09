package org.odk.collect.android.preferences.screens;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesDataSource;
import org.odk.collect.utilities.TestPreferencesProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.preferences.screens.GeneralPreferencesActivity.INTENT_KEY_ADMIN_MODE;

@RunWith(AndroidJUnit4.class)
public class IdentityPreferencesFragmentFragmentTest {
    private final PreferencesDataSource adminPrefs = TestPreferencesProvider.getAdminPreferences();

    @Before
    public void setup() {
        adminPrefs.clear();
        adminPrefs.loadDefaultPreferencesIfNotExist();
    }

    @Test
    public void visiblePreferences_shouldBeVisibleIfOpenedFromGeneralPreferences() {
        FragmentScenario<IdentityPreferencesFragment> scenario = FragmentScenario.launch(IdentityPreferencesFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS).isVisible(), equalTo(true));
        });
    }

    @Test
    public void visiblePreferences_shouldBeVisibleIfOpenedFromAdminPreferences() {
        Bundle args = new Bundle();
        args.putBoolean(INTENT_KEY_ADMIN_MODE, true);

        FragmentScenario<IdentityPreferencesFragment> scenario = FragmentScenario.launch(IdentityPreferencesFragment.class, args);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS).isVisible(), equalTo(true));
        });
    }

    @Test
    public void hiddenPreferences_shouldBeHiddenIfOpenedFromGeneralPreferences() {
        adminPrefs.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false);
        adminPrefs.save(AdminKeys.KEY_ANALYTICS, false);

        FragmentScenario<IdentityPreferencesFragment> scenario = FragmentScenario.launch(IdentityPreferencesFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS), nullValue());
        });
    }

    @Test
    public void hiddenPreferences_shouldBeVisibleIfOpenedFromAdminSettings() {
        adminPrefs.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false);
        adminPrefs.save(AdminKeys.KEY_ANALYTICS, false);

        Bundle args = new Bundle();
        args.putBoolean(INTENT_KEY_ADMIN_MODE, true);

        FragmentScenario<IdentityPreferencesFragment> scenario = FragmentScenario.launch(IdentityPreferencesFragment.class, args);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS).isVisible(), equalTo(true));
        });
    }
}