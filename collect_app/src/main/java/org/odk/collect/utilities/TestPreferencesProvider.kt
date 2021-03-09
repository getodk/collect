package org.odk.collect.utilities

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.PreferencesDataSource
import org.odk.collect.android.preferences.PreferencesDataSourceProvider
import org.odk.collect.android.preferences.SharedPreferencesDataSource

// Use just for testing
object TestPreferencesProvider {
    @JvmStatic
    fun getPreferencesRepository(): PreferencesDataSourceProvider {
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
        return SharedPreferencesDataSource(ApplicationProvider.getApplicationContext<Collect>().getSharedPreferences(name, Context.MODE_PRIVATE))
    }
}
