package org.odk.collect.android.preferences.source

import org.odk.collect.shared.Settings

interface SettingsProvider {

    fun getMetaSettings(): Settings

    fun getUnprotectedSettings(projectId: String?): Settings

    fun getUnprotectedSettings(): Settings = getUnprotectedSettings(null)

    fun getAdminSettings(projectId: String?): Settings

    fun getAdminSettings(): Settings = getAdminSettings(null)

    fun clearAll()
}
