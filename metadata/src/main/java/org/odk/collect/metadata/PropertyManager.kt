package org.odk.collect.metadata

import org.javarosa.core.services.IPropertyManager
import org.javarosa.core.services.properties.IPropertyRules
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import timber.log.Timber

/**
 * Returns device properties and metadata to JavaRosa
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
class PropertyManager(
    private val permissionsProvider: PermissionsProvider,
    private val deviceDetailsProvider: DeviceDetailsProvider,
    private val settingsProvider: SettingsProvider
) : IPropertyManager {
    private val properties = mutableMapOf<String, String>()
    var isPhoneStateRequired = false
        private set

    fun reload(): PropertyManager {
        isPhoneStateRequired = false

        try {
            putProperty(PROPMGR_DEVICE_ID, "", deviceDetailsProvider.deviceId)
            putProperty(PROPMGR_PHONE_NUMBER, SCHEME_TEL, deviceDetailsProvider.line1Number)
        } catch (e: SecurityException) {
            Timber.i(e)
        }

        // User-defined properties. Will replace any above with the same PROPMGR_ name
        val generalSettings = settingsProvider.getUnprotectedSettings()

        putProperty(PROPMGR_USERNAME, SCHEME_USERNAME, generalSettings.getString(ProjectKeys.KEY_METADATA_USERNAME))
        putProperty(PROPMGR_PHONE_NUMBER, SCHEME_TEL, generalSettings.getString(ProjectKeys.KEY_METADATA_PHONENUMBER))
        putProperty(PROPMGR_EMAIL, SCHEME_MAILTO, generalSettings.getString(ProjectKeys.KEY_METADATA_EMAIL))

        // Use the server username by default if the metadata username is not defined
        if (getSingularProperty(PROPMGR_USERNAME).isBlank()) {
            putProperty(PROPMGR_USERNAME, SCHEME_USERNAME, settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_USERNAME))
        }
        return this
    }

    private fun putProperty(propName: String, scheme: String, value: String?) {
        if (value != null && value.isNotBlank()) {
            properties[propName] = value
            properties["uri:$propName"] = "$scheme:$value"
        }
    }

    override fun getSingularProperty(propertyName: String): String {
        if (propertyName.equals(PROPMGR_PHONE_NUMBER, ignoreCase = true) && !permissionsProvider.isReadPhoneStatePermissionGranted) {
            isPhoneStateRequired = true
        }

        return properties[propertyName.lowercase()] ?: ""
    }

    override fun getProperty(propertyName: String): List<String> = emptyList()
    override fun setProperty(propertyName: String, propertyValue: String) = Unit
    override fun setProperty(propertyName: String, propertyValue: List<String>) = Unit
    override fun addRules(rules: IPropertyRules) = Unit
    override fun getRules(): List<IPropertyRules> = emptyList()

    companion object {
        const val PROPMGR_DEVICE_ID = "deviceid"
        const val PROPMGR_PHONE_NUMBER = "phonenumber"
        const val PROPMGR_USERNAME = "username"
        const val PROPMGR_EMAIL = "email"
        const val SCHEME_USERNAME = "username"

        private const val SCHEME_TEL = "tel"
        private const val SCHEME_MAILTO = "mailto"
    }
}
