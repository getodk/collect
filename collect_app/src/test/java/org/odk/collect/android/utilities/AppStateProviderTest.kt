package org.odk.collect.android.utilities

import android.content.pm.PackageInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
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
        whenever(metaSettings.contains(MetaKeys.FIRST_LAUNCH)).thenReturn(false)

        assertThat(appStateProvider.isFreshInstall(), equalTo(true))
    }

    @Test
    fun `When app is newly installed isFreshInstall() should return false when opened not for the first time`() {
        packageInfo.firstInstallTime = 100
        packageInfo.lastUpdateTime = 100
        whenever(metaSettings.contains(MetaKeys.FIRST_LAUNCH)).thenReturn(true)

        assertThat(appStateProvider.isFreshInstall(), equalTo(false))
    }

    @Test
    fun `When app is updated should return false`() {
        packageInfo.firstInstallTime = 100
        packageInfo.lastUpdateTime = 200

        assertThat(appStateProvider.isFreshInstall(), equalTo(false))
    }
}
