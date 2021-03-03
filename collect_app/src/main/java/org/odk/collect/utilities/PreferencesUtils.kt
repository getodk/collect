package org.odk.collect.utilities

import androidx.test.core.app.ApplicationProvider
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.PreferencesDataSource
import org.odk.collect.android.preferences.PreferencesRepository

object PreferencesUtils {
    @JvmStatic
    fun getPreferencesRepository(): PreferencesRepository {
        return DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>()).preferencesRepository()
    }

    @JvmStatic
    fun getGeneralPreferences(): PreferencesDataSource {
        return getPreferencesRepository().getGeneralPreferences()
    }

    @JvmStatic
    fun getAdminPreferences(): PreferencesDataSource {
        return getPreferencesRepository().getAdminPreferences()
    }

    @JvmStatic
    fun getMetaPreferences(): PreferencesDataSource {
        return getPreferencesRepository().getMetaPreferences()
    }

    @JvmStatic
    fun getTestPreferences(name: String?): PreferencesDataSource {
        return getPreferencesRepository().getTestPreferences(name!!)
    }
}
