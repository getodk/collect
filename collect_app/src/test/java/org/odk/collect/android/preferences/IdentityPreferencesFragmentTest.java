package org.odk.collect.android.preferences;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.utilities.TestPreferencesProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

@RunWith(AndroidJUnit4.class)
public class IdentityPreferencesFragmentTest {
    private final PreferencesDataSource adminPrefs = TestPreferencesProvider.getAdminPreferences();

    @Before
    public void setup() {
        adminPrefs.clear();
        adminPrefs.loadDefaultPreferencesIfNotExist();
    }

    @Test
    public void visiblePreferences_shouldBeVisibleIfOpenedFromGeneralPreferences() {
        FragmentScenario<IdentityPreferences> scenario = FragmentScenario.launch(IdentityPreferences.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS).isVisible(), equalTo(true));
        });
    }

    @Test
    public void visiblePreferences_shouldBeVisibleIfOpenedFromAdminPreferences() {
        Bundle args = new Bundle();
        args.putBoolean(INTENT_KEY_ADMIN_MODE, true);

        FragmentScenario<IdentityPreferences> scenario = FragmentScenario.launch(IdentityPreferences.class, args);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS).isVisible(), equalTo(true));
        });
    }

    @Test
    public void hiddenPreferences_shouldBeHiddenIfOpenedFromGeneralPreferences() {
        adminPrefs.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false);
        adminPrefs.save(AdminKeys.KEY_ANALYTICS, false);

        FragmentScenario<IdentityPreferences> scenario = FragmentScenario.launch(IdentityPreferences.class);
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

        FragmentScenario<IdentityPreferences> scenario = FragmentScenario.launch(IdentityPreferences.class, args);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS).isVisible(), equalTo(true));
        });
    }
}