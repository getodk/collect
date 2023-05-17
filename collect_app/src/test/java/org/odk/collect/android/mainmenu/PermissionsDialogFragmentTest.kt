package org.odk.collect.android.mainmenu

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.fakes.FakePermissionsProvider
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.permissions.PermissionsChecker

@RunWith(AndroidJUnit4::class)
class PermissionsDialogFragmentTest {

    private val permissionsProvider = FakePermissionsProvider()
    private val permissionChecker = mock<PermissionsChecker>() {
        on { shouldAskForPermission(any()) } doReturn true
    }

    private val fragmentFactory = FragmentFactoryBuilder()
        .forClass(PermissionsDialogFragment::class) { PermissionsDialogFragment(permissionChecker, permissionsProvider) }
        .build()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(
        defaultThemeResId = R.style.Theme_MaterialComponents,
        defaultFactory = fragmentFactory
    )

    @Test
    @Ignore("Need getState() for FragmentScenario")
    fun whenShouldNotAskForNotificationPermission_dismisses() {
        whenever(permissionChecker.shouldAskForPermission(any(), Manifest.permission.POST_NOTIFICATIONS))
            .doReturn(false)

        val scenario = launcherRule.launch(PermissionsDialogFragment::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
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
