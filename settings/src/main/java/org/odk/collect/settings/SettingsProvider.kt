package org.odk.collect.settings

import org.odk.collect.shared.settings.Settings

interface SettingsProvider {

    fun getMetaSettings(): Settings

    fun getUnprotectedSettings(projectId: String?): Settings

    fun getUnprotectedSettings(): Settings = getUnprotectedSettings(null)

    fun getProtectedSettings(projectId: String?): Settings

    fun getProtectedSettings(): Settings = getProtectedSettings(null)

    fun clearAll()
}
