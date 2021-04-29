package org.odk.collect.android.utilities

import android.content.pm.PackageInfo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.shared.Settings

class AppStateProviderTest {
    private lateinit var appStateProvider: AppStateProvider
    private lateinit var packageInfo: PackageInfo
    private lateinit var metaSettings: Settings

    @Before
    fun setup() {
        packageInfo = PackageInfo()
        metaSettings = mock(Settings::class.java)
        appStateProvider = AppStateProvider(packageInfo, metaSettings)
    }

    @Test
    fun `When app is newly installed isFreshInstall() should return true when opened for the first time`() {
        packageInfo.firstInstallTime = 100
        packageInfo.lastUpdateTime = 100
        `when`(metaSettings.contains(MetaKeys.FIRST_LAUNCH)).thenReturn(false)

        assertThat(appStateProvider.isFreshInstall(), `is`(true))
    }

    @Test
    fun `When app is newly installed isFreshInstall() should return false when opened not for the first time`() {
        packageInfo.firstInstallTime = 100
        packageInfo.lastUpdateTime = 100
        `when`(metaSettings.contains(MetaKeys.FIRST_LAUNCH)).thenReturn(true)

        assertThat(appStateProvider.isFreshInstall(), `is`(false))
    }

    @Test
    fun `When app is updated should return false`() {
        packageInfo.firstInstallTime = 100
        packageInfo.lastUpdateTime = 200

        assertThat(appStateProvider.isFreshInstall(), `is`(false))
    }
}
