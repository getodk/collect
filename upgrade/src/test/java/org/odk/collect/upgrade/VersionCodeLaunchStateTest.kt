package org.odk.collect.upgrade

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.testshared.InMemSettings

class VersionCodeLaunchStateTest {

    @Test
    fun `isUpgradedFirstLaunch() returns false for empty settings`() {
        val appStateProvider = VersionCodeLaunchState("key", InMemSettings(), 1, mock())
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns false if last launched is equal to current version`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save("key", 1)

        val appStateProvider = VersionCodeLaunchState("key", inMemSettings, 1, mock())
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns true if last launched is less than current version`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save("key", 1)

        val appStateProvider = VersionCodeLaunchState("key", inMemSettings, 2, mock())
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(true))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns false if last launched is less than current version after appLaunched()`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save("key", 1)

        val appStateProvider = VersionCodeLaunchState("key", inMemSettings, 2, mock())
        appStateProvider.appLaunched()
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns true for empty settings when legacy install detected`() {
        val appStateProvider = VersionCodeLaunchState(
            "key",
            InMemSettings(),
            1,
            mock { on { installDetected() } doReturn true }
        )

        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(true))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns false for empty settings when legacy install not detected`() {
        val appStateProvider = VersionCodeLaunchState(
            "key",
            InMemSettings(),
            1,
            mock { on { installDetected() } doReturn false }
        )

        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }
}
