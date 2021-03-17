package org.odk.collect.android.preferences.screens;

import android.content.Context;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.preference.CheckBoxPreference;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.Settings;
import org.odk.collect.android.preferences.Protocol;
import org.odk.collect.android.preferences.screens.AdminPreferencesFragment.MainMenuAccessPreferences;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_GET_BLANK;

@RunWith(AndroidJUnit4.class)
public class MainMenuAccessPreferencesTest {

    private Context context;
    private final Settings generalSettings = TestSettingsProvider.getGeneralSettings();
    private final Settings adminSettings = TestSettingsProvider.getAdminSettings();

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        generalSettings.clear();
        generalSettings.setDefaultForAllSettingsWithoutValues();
        adminSettings.clear();
        adminSettings.setDefaultForAllSettingsWithoutValues();
    }

    @Test
    public void whenMatchExactlyEnabled_showsGetBlankFormAsUncheckedAndDisabled() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context));

        FragmentScenario<MainMenuAccessPreferences> scenario = FragmentScenario.launch(MainMenuAccessPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference getBlankForm = f.findPreference(KEY_GET_BLANK);
            assertThat(getBlankForm.isEnabled(), is(false));
            assertThat(getBlankForm.isChecked(), is(false));
            assertThat(adminSettings.getBoolean(KEY_GET_BLANK), is(true));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_andGoogleUsedAsProtocol_getBlankFormIsEnabled() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context));
        generalSettings.save(GeneralKeys.KEY_PROTOCOL, Protocol.GOOGLE.getValue(context));

        FragmentScenario<MainMenuAccessPreferences> scenario = FragmentScenario.launch(MainMenuAccessPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference getBlankForm = f.findPreference(KEY_GET_BLANK);
            assertThat(getBlankForm.isEnabled(), is(true));
        });
    }
}