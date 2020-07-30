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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.formmanagement.FormUpdateMode.MANUAL;
import static org.odk.collect.android.formmanagement.FormUpdateMode.MATCH_EXACTLY;
import static org.odk.collect.android.formmanagement.FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY;
import static org.odk.collect.android.injection.DaggerUtils.getComponent;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOMATIC_UPDATE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;

@RunWith(AndroidJUnit4.class)
public class FormManagementPreferencesTest {

    private SharedPreferences prefs;
    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        prefs = getComponent(context).preferencesProvider().getGeneralSharedPreferences();
    }

    @Test
    public void whenManualUpdatesEnabled_disablesPrefs() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MANUAL.getValue(context)).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(false));
        });
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_disablesPrefs() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, PREVIOUSLY_DOWNLOADED_ONLY.getValue(context)).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(true));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(true));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_disablesPrefs() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context)).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(true));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(false));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_andAutomaticDownloadDisabled_showsAutomaticDownloadAsChecked() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context)).apply();
        prefs.edit().putBoolean(KEY_AUTOMATIC_UPDATE, false).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(true));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_andAutomaticDownloadEnabled_showsAutomaticDownloadAsNotChecked() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MANUAL.getValue(context)).apply();
        prefs.edit().putBoolean(KEY_AUTOMATIC_UPDATE, true).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(false));
        });
    }
}