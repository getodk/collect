package org.odk.collect.android.projects

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.ProjectsRepository

class SettingsConnectionMatcher(
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider
) {

    fun getProjectWithMatchingConnection(settingsJson: String): String? {
        try {
            val jsonObject = JSONObject(settingsJson)
            val jsonSettings = jsonObject.getJSONObject(AppConfigurationKeys.GENERAL)
            val jsonProtocol = try { jsonSettings.get(GeneralKeys.KEY_PROTOCOL) } catch (e: JSONException) { GeneralKeys.PROTOCOL_SERVER }

            val jsonUrl = try { jsonSettings.get(GeneralKeys.KEY_SERVER_URL) } catch (e: JSONException) { "" }
            val jsonUsername = try { jsonSettings.get(GeneralKeys.KEY_USERNAME) } catch (e: JSONException) { "" }

            val jsonGoogleAccount = try { jsonSettings.get(GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT) } catch (e: JSONException) { "" }

            projectsRepository.getAll().forEach {
                val projectSettings = settingsProvider.getGeneralSettings(it.uuid)
                val projectProtocol = projectSettings.getString(GeneralKeys.KEY_PROTOCOL)
                val projectUrl = projectSettings.getString(GeneralKeys.KEY_SERVER_URL)
                val projectUsername = projectSettings.getString(GeneralKeys.KEY_USERNAME)
                val projectGoogleAccount = projectSettings.getString(GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT)

                if (jsonProtocol.equals(projectProtocol) && jsonProtocol.equals(GeneralKeys.PROTOCOL_SERVER)) {
                    if (jsonUrl.equals(projectUrl) && jsonUsername.equals(projectUsername)) {
                        return it.uuid
                    }
                } else {
                    if (jsonGoogleAccount.equals(projectGoogleAccount)) {
                        return it.uuid
                    }
                }
            }
        } catch (e: JSONException) {
            return null
        }
        return null
    }
}
