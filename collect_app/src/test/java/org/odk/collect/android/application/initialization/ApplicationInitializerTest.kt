package org.odk.collect.android.application.initialization

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.odk.collect.android.application.initialization.upgrade.AppUpgrader
import org.odk.collect.android.utilities.LaunchState

@RunWith(AndroidJUnit4::class)
class ApplicationInitializerTest {

    @Test
    fun `runs upgrade when upgraded version launched`() {
        val launchState = mock<LaunchState> {
            on { isUpgradedFirstLaunch() } doReturn true
        }

        val appUpgrader = mock<AppUpgrader>()

        val applicationInitializer = ApplicationInitializer(
            ApplicationProvider.getApplicationContext(),
            mock(),
            mock(),
            mock(),
            launchState,
            appUpgrader,
            mock(),
            mock(),
            mock()
        )

        applicationInitializer.initialize()
        verify(appUpgrader).upgrade()
    }

    @Test
    fun `does not run upgrade when not launching upgraded version`() {
        val launchState = mock<LaunchState> {
            on { isUpgradedFirstLaunch() } doReturn false
        }

        val appUpgrader = mock<AppUpgrader>()

        val applicationInitializer = ApplicationInitializer(
            ApplicationProvider.getApplicationContext(),
            mock(),
            mock(),
            mock(),
            launchState,
            appUpgrader,
            mock(),
            mock(),
            mock()
        )

        applicationInitializer.initialize()
        verify(appUpgrader, never()).upgrade()
    }

    @Test
    fun `registers app launch`() {
        val launchState = mock<LaunchState>()

        val applicationInitializer = ApplicationInitializer(
            ApplicationProvider.getApplicationContext(),
            mock(),
            mock(),
            mock(),
            launchState,
            mock(),
            mock(),
            mock(),
            mock()
        )

        applicationInitializer.initialize()
        verify(launchState).appLaunched()
    }
}
