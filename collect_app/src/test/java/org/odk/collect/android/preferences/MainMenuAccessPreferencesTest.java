package org.odk.collect.android.preferences;

import android.content.Context;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.preference.CheckBoxPreference;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.AdminPreferencesFragment.MainMenuAccessPreferences;
import org.odk.collect.utilities.TestPreferencesProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.preferences.AdminKeys.KEY_GET_BLANK;

@RunWith(AndroidJUnit4.class)
public class MainMenuAccessPreferencesTest {

    private Context context;
    private final PreferencesDataSource generalPrefs = TestPreferencesProvider.getGeneralPreferences();
    private final PreferencesDataSource adminPrefs = TestPreferencesProvider.getAdminPreferences();

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        generalPrefs.clear();
        generalPrefs.loadDefaultPreferencesIfNotExist();
        adminPrefs.clear();
        adminPrefs.loadDefaultPreferencesIfNotExist();
    }

    @Test
    public void whenMatchExactlyEnabled_showsGetBlankFormAsUncheckedAndDisabled() {
        generalPrefs.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context));

        FragmentScenario<MainMenuAccessPreferences> scenario = FragmentScenario.launch(MainMenuAccessPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference getBlankForm = f.findPreference(KEY_GET_BLANK);
            assertThat(getBlankForm.isEnabled(), is(false));
            assertThat(getBlankForm.isChecked(), is(false));
            assertThat(adminPrefs.getBoolean(KEY_GET_BLANK), is(true));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_andGoogleUsedAsProtocol_getBlankFormIsEnabled() {
        generalPrefs.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context));
        generalPrefs.save(GeneralKeys.KEY_PROTOCOL, Protocol.GOOGLE.getValue(context));

        FragmentScenario<MainMenuAccessPreferences> scenario = FragmentScenario.launch(MainMenuAccessPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference getBlankForm = f.findPreference(KEY_GET_BLANK);
            assertThat(getBlankForm.isEnabled(), is(true));
        });
    }
}