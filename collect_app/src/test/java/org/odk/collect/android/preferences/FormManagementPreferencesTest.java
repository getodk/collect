package org.odk.collect.android.preferences;

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
import org.odk.collect.utilities.TestPreferencesProvider;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.preferences.FormUpdateMode.MANUAL;
import static org.odk.collect.android.preferences.FormUpdateMode.MATCH_EXACTLY;
import static org.odk.collect.android.preferences.FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOMATIC_UPDATE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class FormManagementPreferencesTest {

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
    public void whenGoogleDriveUsedAsServer_showsUpdateModeAsManual_andDisablesPrefs() {
        generalPrefs.save(KEY_PROTOCOL, Protocol.GOOGLE.getValue(context));
        generalPrefs.save(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context));

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_FORM_UPDATE_MODE).getSummary(), is(context.getString(R.string.manual)));
            assertThat(generalPrefs.getString(KEY_FORM_UPDATE_MODE), is(MATCH_EXACTLY.getValue(context)));

            assertThat(f.findPreference(KEY_FORM_UPDATE_MODE).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(false));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_disablesPrefs() {
        generalPrefs.save(KEY_FORM_UPDATE_MODE, MANUAL.getValue(context));

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(false));
        });
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_disablesPrefs() {
        generalPrefs.save(KEY_FORM_UPDATE_MODE, PREVIOUSLY_DOWNLOADED_ONLY.getValue(context));

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(true));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(true));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_disablesPrefs() {
        generalPrefs.save(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context));

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(true));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(false));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_andAutomaticDownloadDisabled_showsAutomaticDownloadAsChecked() {
        generalPrefs.save(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context));
        generalPrefs.save(KEY_AUTOMATIC_UPDATE, false);

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(true));
            assertThat(generalPrefs.getBoolean(KEY_AUTOMATIC_UPDATE), is(false));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_andAutomaticDownloadEnabled_showsAutomaticDownloadAsNotChecked() {
        generalPrefs.save(KEY_FORM_UPDATE_MODE, MANUAL.getValue(context));
        generalPrefs.save(KEY_AUTOMATIC_UPDATE, true);

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(generalPrefs.getBoolean(KEY_AUTOMATIC_UPDATE), is(true));
        });
    }

    @Test
    public void whenGoogleDriveUsedAsServer_andAutomaticDownloadEnabled_showsAutomaticDownloadAsNotChecked() {
        generalPrefs.save(KEY_PROTOCOL, Protocol.GOOGLE.getValue(context));
        generalPrefs.save(KEY_AUTOMATIC_UPDATE, true);

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(generalPrefs.getBoolean(KEY_AUTOMATIC_UPDATE), is(true));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_andAutomaticDownloadDisabled_settingToPreviouslyDownloaded_resetsAutomaticDownload() {
        generalPrefs.save(KEY_FORM_UPDATE_MODE, MANUAL.getValue(context));
        generalPrefs.save(KEY_AUTOMATIC_UPDATE, false);

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            ListPreference updateMode = f.findPreference(KEY_FORM_UPDATE_MODE);
            updateMode.setValue(PREVIOUSLY_DOWNLOADED_ONLY.getValue(context));
            shadowOf(getMainLooper()).idle();

            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(generalPrefs.getBoolean(KEY_AUTOMATIC_UPDATE), is(false));
        });
    }

    @Test
    public void changingFormUpdateMode_shouldNotCauseAnyCrashIfRelatedPreferncesAreDisabledInAdminSettings() {
        adminPrefs.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false);
        adminPrefs.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false);

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
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
        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
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

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class, args);
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
        adminPrefs.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false);
        adminPrefs.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false);
        adminPrefs.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false);
        adminPrefs.save(AdminKeys.KEY_AUTOSEND, false);
        adminPrefs.save(AdminKeys.KEY_DELETE_AFTER_SEND, false);
        adminPrefs.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false);
        adminPrefs.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false);
        adminPrefs.save(AdminKeys.KEY_HIGH_RESOLUTION, false);
        adminPrefs.save(AdminKeys.KEY_IMAGE_SIZE, false);
        adminPrefs.save(AdminKeys.KEY_GUIDANCE_HINT, false);
        adminPrefs.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false);
        adminPrefs.save(AdminKeys.KEY_EXTERNAL_APP_RECORDING, false);

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
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
        adminPrefs.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false);
        adminPrefs.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false);
        adminPrefs.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false);
        adminPrefs.save(AdminKeys.KEY_AUTOSEND, false);
        adminPrefs.save(AdminKeys.KEY_DELETE_AFTER_SEND, false);
        adminPrefs.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false);
        adminPrefs.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false);
        adminPrefs.save(AdminKeys.KEY_HIGH_RESOLUTION, false);
        adminPrefs.save(AdminKeys.KEY_IMAGE_SIZE, false);
        adminPrefs.save(AdminKeys.KEY_GUIDANCE_HINT, false);
        adminPrefs.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false);

        Bundle args = new Bundle();
        args.putBoolean(INTENT_KEY_ADMIN_MODE, true);

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class, args);
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
