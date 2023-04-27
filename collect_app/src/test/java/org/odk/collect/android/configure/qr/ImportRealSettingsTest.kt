package org.odk.collect.android.configure.qr

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.settings.keys.ProtectedProjectKeys

@RunWith(AndroidJUnit4::class)
class ImportRealSettingsTest {
    @Before
    fun setup() {
        CollectHelpers.setupDemoProject()
    }

    @Test // https://github.com/getodk/collect/issues/5416
    fun `importing settings with removed finalization settings should migrate settings properly`() {
        val odkAppSettingsImporter = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>()).settingsImporter()
        val currentProjectProvider = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>()).currentProjectProvider()
        val settingsProvider = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>()).settingsProvider()

        odkAppSettingsImporter.fromJSON(
            "{\n" +
                "  \"general\": {\n" +
                "  },\n" +
                "  \"admin\": {\n" +
                "       \"mark_as_finalized\" : false" +
                "  }\n" +
                "}",
            currentProjectProvider.getCurrentProject()
        )

        assertThat(settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT), equalTo(false))
        assertThat(settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_FINALIZE), equalTo(true))

        odkAppSettingsImporter.fromJSON(
            "{\n" +
                "  \"general\": {\n" +
                "       \"default_completed\" : false" +
                "  },\n" +
                "  \"admin\": {\n" +
                "       \"mark_as_finalized\" : false" +
                "  }\n" +
                "}",
            currentProjectProvider.getCurrentProject()
        )

        assertThat(settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT), equalTo(true))
        assertThat(settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_FINALIZE), equalTo(false))
    }
}
