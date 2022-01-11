package org.odk.collect.upgrade

import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.testshared.InMemSettings

class AppUpgraderTest {

    @Test
    fun `upgradeIfNeeded() runs all upgrades`() {
        val upgrade1 = mock<Upgrade>()
        val upgrade2 = mock<Upgrade>()

        val appUpgrader = AppUpgrader(
            InMemSettings(),
            mock { on { isUpgradedFirstLaunch() } doReturn true },
            listOf(upgrade1, upgrade2)
        )

        appUpgrader.upgradeIfNeeded()

        verify(upgrade1).run()
        verify(upgrade2).run()
    }

    @Test
    fun `upgradeIfNeeded() skips upgrades with a key the second time`() {
        val upgrade1 = mock<Upgrade> {
            on { key() } doReturn "blah"
        }

        val upgrade2 = mock<Upgrade> {
            on { key() } doReturn null
        }

        val appUpgrader = AppUpgrader(
            InMemSettings(),
            mock { on { isUpgradedFirstLaunch() } doReturn true },
            listOf(upgrade1, upgrade2)
        )

        appUpgrader.upgradeIfNeeded()
        appUpgrader.upgradeIfNeeded()

        verify(upgrade1, times(1)).run()
        verify(upgrade2, times(2)).run()
    }

    @Test
    fun `upgradeIfNeeded() just marks upgrades with a key as run if not upgrading`() {
        val upgrade1 = mock<Upgrade> {
            on { key() } doReturn "blah"
        }

        val upgrade2 = mock<Upgrade> {
            on { key() } doReturn null
        }

        val launchState = mock<LaunchState> { on { isUpgradedFirstLaunch() } doReturn false }
        val appUpgrader = AppUpgrader(
            InMemSettings(),
            launchState,
            listOf(upgrade1, upgrade2)
        )

        appUpgrader.upgradeIfNeeded()
        verify(upgrade1, times(0)).run()
        verify(upgrade2, times(0)).run()

        whenever(launchState.isUpgradedFirstLaunch()).thenReturn(true)
        appUpgrader.upgradeIfNeeded()
        verify(upgrade1, times(0)).run()
        verify(upgrade2, times(1)).run()
    }

    @Test
    fun `upgradeIfNeeded() calls appLaunched()`() {
        val launchState = mock<LaunchState> { on { isUpgradedFirstLaunch() } doReturn true }
        val appUpgrader = AppUpgrader(
            InMemSettings(),
            launchState,
            emptyList()
        )

        appUpgrader.upgradeIfNeeded()
        verify(launchState).appLaunched()
    }
}
