package org.odk.collect.android.mainmenu

import android.Manifest
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.fakes.FakePermissionsProvider
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.permissions.PermissionsChecker
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class PermissionsDialogFragmentTest {

    private val permissionsProvider = FakePermissionsProvider()
    private val permissionChecker = mock<PermissionsChecker>() {
        on { shouldAskForPermission(any(), any()) } doReturn true
    }

    private val fragmentFactory = FragmentFactoryBuilder()
        .forClass(PermissionsDialogFragment::class) {
            PermissionsDialogFragment(
                permissionChecker,
                permissionsProvider
            )
        }
        .build()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(
        defaultThemeResId = R.style.Theme_MaterialComponents,
        defaultFactory = fragmentFactory
    )

    @Test
    fun whenShouldNotAskForNotificationPermission_dismisses() {
        whenever(
            permissionChecker.shouldAskForPermission(
                any(),
                eq(Manifest.permission.POST_NOTIFICATIONS)
            )
        ).doReturn(false)

        val scenario = launcherRule.launch(
            initialState = Lifecycle.State.INITIALIZED,
            fragmentClass = PermissionsDialogFragment::class.java
        )

        scenario.onFragment {
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    @Config(sdk = [32])
    fun whenNoNeedToAskForNotificationPermission_dismisses() {
        val scenario = launcherRule.launch(
            initialState = Lifecycle.State.INITIALIZED,
            fragmentClass = PermissionsDialogFragment::class.java
        )

        scenario.onFragment {
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun clickingOK_asksForNotificationPermissions() {
        launcherRule.launch(PermissionsDialogFragment::class.java)

        onView(withText(R.string.ok)).inRoot(isDialog()).perform(click())
        assertThat(
            permissionsProvider.requestedPermissions,
            equalTo(listOf(Manifest.permission.POST_NOTIFICATIONS))
        )
    }
}
