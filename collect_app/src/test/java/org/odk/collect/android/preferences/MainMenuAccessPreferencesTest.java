package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;

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
    private SharedPreferences generalPrefs;
    private SharedPreferences adminPrefs;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        generalPrefs = getComponent(context).preferencesProvider().getGeneralSharedPreferences();
        adminPrefs = getComponent(context).preferencesProvider().getAdminSharedPreferences();
    }

    @Test
    public void whenMatchExactlyEnabled_showsGetBlankFormAsUncheckedAndDisabled() {
        generalPrefs.edit()
                .putString(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))
                .apply();

        FragmentScenario<MainMenuAccessPreferences> scenario = FragmentScenario.launch(MainMenuAccessPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference getBlankForm = f.findPreference(KEY_GET_BLANK);
            assertThat(getBlankForm.isEnabled(), is(false));
            assertThat(getBlankForm.isChecked(), is(false));
            assertThat(adminPrefs.getBoolean(KEY_GET_BLANK, false), is(true));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_andGoogleUsedAsProtocol_getBlankFormIsEnabled() {
        generalPrefs.edit()
                .putString(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))
                .putString(GeneralKeys.KEY_PROTOCOL, Protocol.GOOGLE.getValue(context))
                .apply();

        FragmentScenario<MainMenuAccessPreferences> scenario = FragmentScenario.launch(MainMenuAccessPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference getBlankForm = f.findPreference(KEY_GET_BLANK);
            assertThat(getBlankForm.isEnabled(), is(true));
        });
    }
}