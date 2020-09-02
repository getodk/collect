package org.odk.collect.android.configure;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.Protocol;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(AndroidJUnit4.class)
public class SettingsUtilsTest {

    @Test
    public void getFormUpdateMode_whenProtocolIsGoogleDrive_andModeNotManual_returnsManual() {
        Context context = getApplicationContext();
        SharedPreferences prefs = initPrefs(
                GeneralKeys.KEY_PROTOCOL, Protocol.GOOGLE.getValue(context),
                GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY.getValue(context)
        );

        FormUpdateMode formUpdateMode = SettingsUtils.getFormUpdateMode(context, prefs);
        assertThat(formUpdateMode, is(FormUpdateMode.MANUAL));
    }
}