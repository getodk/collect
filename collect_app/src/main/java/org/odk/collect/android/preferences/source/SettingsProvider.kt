package org.odk.collect.android.preferences.source

import org.odk.collect.shared.Settings

interface SettingsProvider {

    fun getMetaSettings(): Settings

    fun getGeneralSettings(projectId: String?): Settings

    fun getGeneralSettings(): Settings = getGeneralSettings(null)

    fun getAdminSettings(projectId: String?): Settings

    fun getAdminSettings(): Settings = getAdminSettings(null)

    fun clearAll()
}
