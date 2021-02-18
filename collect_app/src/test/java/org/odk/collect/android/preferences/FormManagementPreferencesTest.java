package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
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

import static android.os.Looper.getMainLooper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.preferences.FormUpdateMode.MANUAL;
import static org.odk.collect.android.preferences.FormUpdateMode.MATCH_EXACTLY;
import static org.odk.collect.android.preferences.FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY;
import static org.odk.collect.android.injection.DaggerUtils.getComponent;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOMATIC_UPDATE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class FormManagementPreferencesTest {

    private SharedPreferences prefs;
    private Context context;
    private final AdminSharedPreferences adminSharedPreferences = AdminSharedPreferences.getInstance();

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        prefs = getComponent(context).preferencesProvider().getGeneralSharedPreferences();
    }

    @Test
    public void whenGoogleDriveUsedAsServer_showsUpdateModeAsManual_andDisablesPrefs() {
        prefs.edit().putString(KEY_PROTOCOL, Protocol.GOOGLE.getValue(context)).apply();
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context)).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_FORM_UPDATE_MODE).getSummary(), is(context.getString(R.string.manual)));
            assertThat(prefs.getString(KEY_FORM_UPDATE_MODE, ""), is(MATCH_EXACTLY.getValue(context)));

            assertThat(f.findPreference(KEY_FORM_UPDATE_MODE).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_AUTOMATIC_UPDATE).isEnabled(), is(false));
        });
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
            assertThat(prefs.getBoolean(KEY_AUTOMATIC_UPDATE, true), is(false));
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
            assertThat(prefs.getBoolean(KEY_AUTOMATIC_UPDATE, false), is(true));
        });
    }

    @Test
    public void whenGoogleDriveUsedAsServer_andAutomaticDownloadEnabled_showsAutomaticDownloadAsNotChecked() {
        prefs.edit().putString(KEY_PROTOCOL, Protocol.GOOGLE.getValue(context)).apply();
        prefs.edit().putBoolean(KEY_AUTOMATIC_UPDATE, true).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(prefs.getBoolean(KEY_AUTOMATIC_UPDATE, false), is(true));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_andAutomaticDownloadDisabled_settingToPreviouslyDownloaded_resetsAutomaticDownload() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context)).apply();
        prefs.edit().putBoolean(KEY_AUTOMATIC_UPDATE, false).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            ListPreference updateMode = f.findPreference(KEY_FORM_UPDATE_MODE);
            updateMode.setValue(PREVIOUSLY_DOWNLOADED_ONLY.getValue(context));
            shadowOf(getMainLooper()).idle();

            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_UPDATE);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(prefs.getBoolean(KEY_AUTOMATIC_UPDATE, true), is(false));
        });
    }

    @Test
    public void changingFormUpdateMode_shouldNotCauseAnyCrashIfRelatedPreferncesAreDisabledInAdminSettings() {
        adminSharedPreferences.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false);
        adminSharedPreferences.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false);

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
        adminSharedPreferences.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false);
        adminSharedPreferences.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false);
        adminSharedPreferences.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false);
        adminSharedPreferences.save(AdminKeys.KEY_AUTOSEND, false);
        adminSharedPreferences.save(AdminKeys.KEY_DELETE_AFTER_SEND, false);
        adminSharedPreferences.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false);
        adminSharedPreferences.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false);
        adminSharedPreferences.save(AdminKeys.KEY_HIGH_RESOLUTION, false);
        adminSharedPreferences.save(AdminKeys.KEY_IMAGE_SIZE, false);
        adminSharedPreferences.save(AdminKeys.KEY_GUIDANCE_HINT, false);
        adminSharedPreferences.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false);
        adminSharedPreferences.save(AdminKeys.KEY_EXTERNAL_APP_RECORDING, false);

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
        adminSharedPreferences.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false);
        adminSharedPreferences.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false);
        adminSharedPreferences.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false);
        adminSharedPreferences.save(AdminKeys.KEY_AUTOSEND, false);
        adminSharedPreferences.save(AdminKeys.KEY_DELETE_AFTER_SEND, false);
        adminSharedPreferences.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false);
        adminSharedPreferences.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false);
        adminSharedPreferences.save(AdminKeys.KEY_HIGH_RESOLUTION, false);
        adminSharedPreferences.save(AdminKeys.KEY_IMAGE_SIZE, false);
        adminSharedPreferences.save(AdminKeys.KEY_GUIDANCE_HINT, false);
        adminSharedPreferences.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false);

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
