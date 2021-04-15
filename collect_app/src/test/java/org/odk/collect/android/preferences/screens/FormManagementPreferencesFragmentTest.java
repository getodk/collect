package org.odk.collect.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.preferences.keys.AdminKeys;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.shared.Settings;
import org.odk.collect.android.preferences.Protocol;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.preferences.FormUpdateMode.MANUAL;
import static org.odk.collect.android.preferences.FormUpdateMode.MATCH_EXACTLY;
import static org.odk.collect.android.preferences.FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_AUTOMATIC_UPDATE;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.screens.GeneralPreferencesActivity.INTENT_KEY_ADMIN_MODE;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class FormManagementPreferencesFragmentTest {

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
    public void whenGoogleDriveUsedAsServer_showsUpdateModeAsManual_andDisablesPrefs() {
        generalSettings.save(KEY_PROTOCOL, Protocol.GOOGLE.getValue(context));
        generalSettings.save(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context));

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_FORM_UPDATE_MODE).getSummary(), is(context.getString(R.string.manual)));
            assertThat(generalSettings.getString(KEY_FORM_UPDATE_MODE), is(MATCH_EXACTLY.getValue(context)));

            assertThat(f.findPreference(KEY_FORM_UPDATE_MODE).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(false));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_disablesPrefs() {
        generalSettings.save(KEY_FORM_UPDATE_MODE, MANUAL.getValue(context));

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(false));
        });
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_disablesPrefs() {
        generalSettings.save(KEY_FORM_UPDATE_MODE, PREVIOUSLY_DOWNLOADED_ONLY.getValue(context));

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(true));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(true));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_disablesPrefs() {
        generalSettings.save(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context));

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(true));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(false));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_andAutomaticDownloadDisabled_showsAutomaticDownloadAsChecked() {
        generalSettings.save(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context));
        generalSettings.save(KEY_AUTOMATIC_UPDATE, false);

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(true));
            assertThat(generalSettings.getBoolean(KEY_AUTOMATIC_UPDATE), is(false));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_andAutomaticDownloadEnabled_showsAutomaticDownloadAsNotChecked() {
        generalSettings.save(KEY_FORM_UPDATE_MODE, MANUAL.getValue(context));
        generalSettings.save(KEY_AUTOMATIC_UPDATE, true);

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(generalSettings.getBoolean(KEY_AUTOMATIC_UPDATE), is(true));
        });
    }

    @Test
    public void whenGoogleDriveUsedAsServer_andAutomaticDownloadEnabled_showsAutomaticDownloadAsNotChecked() {
        generalSettings.save(KEY_PROTOCOL, Protocol.GOOGLE.getValue(context));
        generalSettings.save(KEY_AUTOMATIC_UPDATE, true);

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(generalSettings.getBoolean(KEY_AUTOMATIC_UPDATE), is(true));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_andAutomaticDownloadDisabled_settingToPreviouslyDownloaded_resetsAutomaticDownload() {
        generalSettings.save(KEY_FORM_UPDATE_MODE, MANUAL.getValue(context));
        generalSettings.save(KEY_AUTOMATIC_UPDATE, false);

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(f -> {
            ListPreference updateMode = f.findPreference(KEY_FORM_UPDATE_MODE);
            updateMode.setValue(PREVIOUSLY_DOWNLOADED_ONLY.getValue(context));
            shadowOf(getMainLooper()).idle();

            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(generalSettings.getBoolean(KEY_AUTOMATIC_UPDATE), is(false));
        });
    }

    @Test
    public void changingFormUpdateMode_shouldNotCauseAnyCrashIfRelatedPreferncesAreDisabledInAdminSettings() {
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false);
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false);

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK), nullValue());
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE), nullValue());

            ListPreference updateMode = f.findPreference(KEY_FORM_UPDATE_MODE);
            updateMode.setValue(PREVIOUSLY_DOWNLOADED_ONLY.getValue(context));

            updateMode.setValue(MATCH_EXACTLY.getValue(context));

            updateMode.setValue(MANUAL.getValue(context));
        });
    }

    @Test
    public void visiblePreferences_shouldBeVisibleIfOpenedFromGeneralPreferences() {
        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOMATIC_UPDATE).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOSEND).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_DELETE_AFTER_SEND).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_COMPLETED_DEFAULT).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIGH_RESOLUTION).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_IMAGE_SIZE).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_GUIDANCE_HINT).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_INSTANCE_SYNC).isVisible(), equalTo(true));
        });
    }

    @Test
    public void visiblePreferences_shouldBeVisibleIfOpenedFromAdminPreferences() {
        Bundle args = new Bundle();
        args.putBoolean(INTENT_KEY_ADMIN_MODE, true);

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class, args);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOMATIC_UPDATE).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOSEND).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_DELETE_AFTER_SEND).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_COMPLETED_DEFAULT).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIGH_RESOLUTION).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_IMAGE_SIZE).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_GUIDANCE_HINT).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_INSTANCE_SYNC).isVisible(), equalTo(true));
        });
    }

    @Test
    public void hiddenPreferences_shouldBeHiddenIfOpenedFromGeneralPreferences() {
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false);
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false);
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false);
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false);
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, false);
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false);
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false);
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false);
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false);
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false);
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false);
        adminSettings.save(AdminKeys.KEY_EXTERNAL_APP_RECORDING, false);

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOMATIC_UPDATE), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOSEND), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_DELETE_AFTER_SEND), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_COMPLETED_DEFAULT), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIGH_RESOLUTION), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_IMAGE_SIZE), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_GUIDANCE_HINT), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_INSTANCE_SYNC), nullValue());
            assertThat(fragment.findPreference(GeneralKeys.KEY_EXTERNAL_APP_RECORDING), nullValue());
        });
    }

    @Test
    public void hiddenPreferences_shouldBeVisibleIfOpenedFromAdminSettings() {
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false);
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false);
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false);
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false);
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, false);
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false);
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false);
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false);
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false);
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false);
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false);

        Bundle args = new Bundle();
        args.putBoolean(INTENT_KEY_ADMIN_MODE, true);

        FragmentScenario<FormManagementPreferencesFragment> scenario = FragmentScenario.launch(FormManagementPreferencesFragment.class, args);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOMATIC_UPDATE).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOSEND).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_DELETE_AFTER_SEND).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_COMPLETED_DEFAULT).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIGH_RESOLUTION).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_IMAGE_SIZE).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_GUIDANCE_HINT).isVisible(), equalTo(true));
            assertThat(fragment.findPreference(GeneralKeys.KEY_INSTANCE_SYNC).isVisible(), equalTo(true));
        });
    }
}
