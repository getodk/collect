package org.odk.collect.android.preferences;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

@RunWith(AndroidJUnit4.class)
public class IdentityPreferencesFragmentTest {
    private final AdminSharedPreferences adminSharedPreferences = AdminSharedPreferences.getInstance();

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
        adminSharedPreferences.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false);
        adminSharedPreferences.save(AdminKeys.KEY_ANALYTICS, false);

        FragmentScenario<IdentityPreferences> scenario = FragmentScenario.launch(IdentityPreferences.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS), nullValue());
        });
    }

    @Test
    public void hiddenPreferences_shouldBeVisibleIfOpenedFromAdminSettings() {
        adminSharedPreferences.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false);
        adminSharedPreferences.save(AdminKeys.KEY_ANALYTICS, false);

        Bundle args = new Bundle();
        args.putBoolean(INTENT_KEY_ADMIN_MODE, true);

        FragmentScenario<IdentityPreferences> scenario = FragmentScenario.launch(IdentityPreferences.class, args);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS).isVisible(), equalTo(true));
        });
    }
}