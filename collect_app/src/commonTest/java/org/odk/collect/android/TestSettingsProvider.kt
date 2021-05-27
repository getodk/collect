package org.odk.collect.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.preferences.source.SharedPreferencesSettings
import org.odk.collect.shared.Settings

// Use just for testing
object TestSettingsProvider {
    @JvmStatic
    fun getSettingsProvider(): SettingsProvider {
        return DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>()).settingsProvider()
    }

    @JvmStatic
    @JvmOverloads
    fun getGeneralSettings(uuid: String? = null): Settings {
        return getSettingsProvider().getGeneralSettings(uuid)
    }

    @JvmStatic
    fun getAdminSettings(): Settings {
        return getSettingsProvider().getAdminSettings()
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
