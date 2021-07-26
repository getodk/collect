package org.odk.collect.android.application.initialization

import org.odk.collect.analytics.Analytics
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.version.VersionInformation

class AnalyticsInitializer(
    private val analytics: Analytics,
    private val versionInformation: VersionInformation,
    private val settingsProvider: SettingsProvider
) {

    fun initialize() {
        if (versionInformation.isBeta) {
            analytics.setAnalyticsCollectionEnabled(true)
        } else {
            val analyticsEnabled = settingsProvider.getGeneralSettings().getBoolean(ProjectKeys.KEY_ANALYTICS)
            analytics.setAnalyticsCollectionEnabled(analyticsEnabled)
        }

        Analytics.setInstance(analytics)
    }
}
