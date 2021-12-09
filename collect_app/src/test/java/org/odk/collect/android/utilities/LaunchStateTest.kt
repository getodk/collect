package org.odk.collect.android.utilities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.testshared.InMemSettings

class LaunchStateTest {

    @Test
    fun `isUpgradedFirstLaunch() returns false for empty meta settings`() {
        val appStateProvider = LaunchState(InMemSettings(), 1)
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns false if last launched is equal to current version`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save(MetaKeys.LAST_LAUNCHED, 1)

        val appStateProvider = LaunchState(inMemSettings, 1)
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns true if last launched is less than current version`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save(MetaKeys.LAST_LAUNCHED, 1)

        val appStateProvider = LaunchState(inMemSettings, 2)
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(true))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns false if last launched is less than current version after appLaunched()`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save(MetaKeys.LAST_LAUNCHED, 1)

        val appStateProvider = LaunchState(inMemSettings, 2)
        appStateProvider.appLaunched()
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns true for empty meta settings when legacy install detected`() {
        val appStateProvider = LaunchState(
            InMemSettings(),
            1,
            mock { on { installDetected() } doReturn true }
        )

        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(true))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns false for empty meta settings when legacy install not detected`() {
        val appStateProvider = LaunchState(
            InMemSettings(),
            1,
            mock { on { installDetected() } doReturn false }
        )

        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }
}
