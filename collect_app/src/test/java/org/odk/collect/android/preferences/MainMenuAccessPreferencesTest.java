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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.injection.DaggerUtils.getComponent;
import static org.odk.collect.android.preferences.AdminKeys.KEY_GET_BLANK;

@RunWith(AndroidJUnit4.class)
public class MainMenuAccessPreferencesTest {

    private Context context;
    private PreferencesDataSource generalPrefs;
    private PreferencesDataSource adminPrefs;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        generalPrefs = getComponent(context).preferencesRepository().getGeneralPreferences();
        adminPrefs = getComponent(context).preferencesRepository().getAdminPreferences();
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