package org.odk.collect.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.source.SharedPreferencesSettings
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

    @JvmStatic
    fun getProtectedSettings(): Settings {
        return getSettingsProvider().getProtectedSettings()
    }

    @JvmStatic
    fun getMetaSettings(): Settings {
        return getSettingsProvider().getMetaSettings()
    }

    @JvmStatic
    fun getTestSettings(name: String?): Settings {
        return SharedPreferencesSettings(ApplicationProvider.getApplicationContext<Collect>().getSharedPreferences(name, Context.MODE_PRIVATE))
    }
}
