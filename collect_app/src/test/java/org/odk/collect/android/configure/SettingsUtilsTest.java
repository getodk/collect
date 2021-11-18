package org.odk.collect.android.configure;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.shared.Settings;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class SettingsUtilsTest {

    @Test
    public void getFormUpdateMode_whenProtocolIsGoogleDrive_andModeNotManual_returnsManual() {
        Settings generalSettings = TestSettingsProvider.getUnprotectedSettings();
        Context context = getApplicationContext();

        generalSettings.save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS);
        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY.getValue(context));

        FormUpdateMode formUpdateMode = SettingsUtils.getFormUpdateMode(context, generalSettings);
        assertThat(formUpdateMode, is(FormUpdateMode.MANUAL));
    }
}
