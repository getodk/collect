package org.odk.collect.android.preferences.screens;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule;
import org.odk.collect.metadata.InstallIDProvider;
import org.odk.collect.settings.InMemSettingsProvider;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.settings.keys.ProjectKeys;

@RunWith(AndroidJUnit4.class)
public class FormMetadataPreferencesFragmentTest {

    private final InstallIDProvider installIDProvider = mock(InstallIDProvider.class);
    private final SettingsProvider settingsProvider = new InMemSettingsProvider();

    @Rule
    public FragmentScenarioLauncherRule launcherRule = new FragmentScenarioLauncherRule();

    @Before
    public void setup() {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public InstallIDProvider providesInstallIDProvider(SettingsProvider settingsProvider) {
                return installIDProvider;
            }

            @Override
            public SettingsProvider providesSettingsProvider(Context context) {
                return settingsProvider;
            }
        });
    }

    @Test
    public void whenMetadataEmpty_preferenceSummariesAreNotSet() {
        when(installIDProvider.getInstallID()).thenReturn("");

        FragmentScenario<FormMetadataPreferencesFragment> scenario = launcherRule.launch(FormMetadataPreferencesFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference("metadata_username").getSummary(), equalTo("Not set"));
            assertThat(fragment.findPreference("metadata_phonenumber").getSummary(), equalTo("Not set"));
            assertThat(fragment.findPreference("metadata_email").getSummary(), equalTo("Not set"));
            assertThat(fragment.findPreference("deviceid").getSummary(), equalTo(fragment.getContext().getString(R.string.preference_not_available)));
        });
    }

    @Test
    public void whenMetadataNotEmpty_preferenceSummariesAreSet() {
        when(installIDProvider.getInstallID()).thenReturn("123456789");
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_USERNAME, "John");
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_PHONENUMBER, "789");
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_EMAIL, "john@gmail.com");

        FragmentScenario<FormMetadataPreferencesFragment> scenario = launcherRule.launch(FormMetadataPreferencesFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference("metadata_username").getSummary(), equalTo("John"));
            assertThat(fragment.findPreference("metadata_phonenumber").getSummary(), equalTo("789"));
            assertThat(fragment.findPreference("metadata_email").getSummary(), equalTo("john@gmail.com"));
            assertThat(fragment.findPreference("deviceid").getSummary(), equalTo("123456789"));
        });
    }
}
