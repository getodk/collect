package org.odk.collect.android.application.initialization

import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.only
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class AnalyticsInitializerTest {

    private val analytics = mock<Analytics>()
    private val settingsProvider = InMemSettingsProvider()
    private val versionInformation = mock<VersionInformation> {
        on { isBeta } doReturn false
    }

    @Test
    fun whenBetaVersion_enablesAnalytics() {
        whenever(versionInformation.isBeta).thenReturn(true)

        val analyticsInitializer = AnalyticsInitializer(
            analytics,
            versionInformation,
            settingsProvider
        )
        analyticsInitializer.initialize()
        verify(analytics, only()).setAnalyticsCollectionEnabled(true)
    }

    @Test
    fun whenBetaVersion_andAnalyticsDisabledInSettings_enablesAnalytics() {
        whenever(versionInformation.isBeta).thenReturn(true)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_ANALYTICS, false)

        val analyticsInitializer = AnalyticsInitializer(
            analytics,
            versionInformation,
            settingsProvider
        )
        analyticsInitializer.initialize()
        verify(analytics, only()).setAnalyticsCollectionEnabled(true)
    }

    @Test
    fun whenAnalyticsDisabledInSettings_disablesAnalytics() {
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_ANALYTICS, false)

        val analyticsInitializer =
            AnalyticsInitializer(analytics, versionInformation, settingsProvider)
        analyticsInitializer.initialize()
        verify(analytics, only()).setAnalyticsCollectionEnabled(false)
    }

    @Test
    fun whenAnalyticsEnabledInSettings_enablesAnalytics() {
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_ANALYTICS, true)

        val analyticsInitializer =
            AnalyticsInitializer(analytics, versionInformation, settingsProvider)
        analyticsInitializer.initialize()
        verify(analytics, only()).setAnalyticsCollectionEnabled(true)
    }
}
