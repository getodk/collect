package org.odk.collect.android

import androidx.test.core.app.ApplicationProvider
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.settings.Settings

// Use just for testing
object TestSettingsProvider {
    @JvmStatic
    fun getSettingsProvider(): SettingsProvider {
        return DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>()).settingsProvider()
    }

    @JvmStatic
    @JvmOverloads
    fun getUnprotectedSettings(uuid: String? = null): Settings {
        return getSettingsProvider().getUnprotectedSettings(uuid)
    }
}
