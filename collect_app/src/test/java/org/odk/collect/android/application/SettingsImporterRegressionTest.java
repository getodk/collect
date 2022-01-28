package org.odk.collect.android.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.injection.DaggerUtils.getComponent;
import static org.odk.collect.android.preferences.keys.ProtectedProjectKeys.KEY_ADMIN_PW;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.projects.Project;

@RunWith(AndroidJUnit4.class)
public class SettingsImporterRegressionTest {

    private SettingsImporter settingsImporter;
    private Project.Saved currentProject;
    private final SettingsProvider settingsProvider = TestSettingsProvider.getSettingsProvider();

    @Before
    public void setup() {
        CollectHelpers.createProject(Project.Companion.getDEMO_PROJECT());
        getComponent(ApplicationProvider.<Collect>getApplicationContext()).currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID);
        settingsImporter = getComponent(ApplicationProvider.<Collect>getApplicationContext()).settingsImporter();
        currentProject = getComponent(ApplicationProvider.<Collect>getApplicationContext()).currentProjectProvider().getCurrentProject();
    }

    @Test
    public void adminPW() {
        settingsImporter.fromJSON("{\"general\":{\"periodic_form_updates_check\":\"every_fifteen_minutes\"},\"admin\":{\"admin_pw\":\"blah\"}}", currentProject);
        assertThat(settingsProvider.getProtectedSettings().getString(KEY_ADMIN_PW), is("blah"));
    }
}
