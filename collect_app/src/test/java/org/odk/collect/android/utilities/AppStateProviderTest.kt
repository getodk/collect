package org.odk.collect.android.utilities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.testshared.InMemSettings

class AppStateProviderTest {

    @Test
    fun `isUpgradedFirstLaunch() returns false for empty meta settings`() {
        val appStateProvider = AppStateProvider(1, InMemSettings())
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns false if last launched is equal to current version`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save(MetaKeys.LAST_LAUNCHED, 1)

        val appStateProvider = AppStateProvider(1, inMemSettings)
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns true if last launched is less than current version`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save(MetaKeys.LAST_LAUNCHED, 1)

        val appStateProvider = AppStateProvider(2, inMemSettings)
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(true))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns false if last launched is less than current version after appLaunched()`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save(MetaKeys.LAST_LAUNCHED, 1)

        val appStateProvider = AppStateProvider(2, inMemSettings)
        appStateProvider.appLaunched()
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }
}
