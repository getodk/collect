package org.odk.collect.android.application.initialization.upgrade

import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.odk.collect.testshared.InMemSettings

class AppUpgraderTest {

    @Test
    fun `upgrade() runs all upgrades`() {
        val upgrade1 = mock<Upgrade>()
        val upgrade2 = mock<Upgrade>()

        val appUpgrader = AppUpgrader(InMemSettings(), listOf(upgrade1, upgrade2))
        appUpgrader.upgrade()

        verify(upgrade1).run()
        verify(upgrade2).run()
    }

    @Test
    fun `upgrade() skips upgrades with a key the second time`() {
        val upgrade1 = mock<Upgrade> {
            on { key() } doReturn "blah"
        }

        val upgrade2 = mock<Upgrade> {
            on { key() } doReturn null
        }

        val appUpgrader = AppUpgrader(InMemSettings(), listOf(upgrade1, upgrade2))
        appUpgrader.upgrade()
        appUpgrader.upgrade()

        verify(upgrade1, times(1)).run()
        verify(upgrade2, times(2)).run()
    }
}
