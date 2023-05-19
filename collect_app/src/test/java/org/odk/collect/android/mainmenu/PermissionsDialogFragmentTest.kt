package org.odk.collect.android.mainmenu

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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.android.fakes.FakePermissionsProvider
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class PermissionsDialogFragmentTest {

    private val permissionsProvider = FakePermissionsProvider()
    private val requestPermissionsViewModel = mock<RequestPermissionsViewModel>() {
        on { permissions } doReturn arrayOf("blah")
        on { shouldAskForPermissions() } doReturn true
    }

    private val fragmentFactory = FragmentFactoryBuilder()
        .forClass(PermissionsDialogFragment::class) {
            PermissionsDialogFragment(permissionsProvider, requestPermissionsViewModel)
        }
        .build()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(
        defaultThemeResId = R.style.Theme_MaterialComponents,
        defaultFactory = fragmentFactory
    )

    @Test
    fun clickingOK_asksForPermissions() {
        launcherRule.launch(PermissionsDialogFragment::class.java)

        onView(withText(R.string.ok)).inRoot(isDialog()).perform(click())
        assertThat(
            permissionsProvider.requestedPermissions,
            equalTo(listOf("blah"))
        )
    }

    @Test
    fun clickingOK_callsPermissionRequested() {
        launcherRule.launch(PermissionsDialogFragment::class.java)

        onView(withText(R.string.ok)).inRoot(isDialog()).perform(click())
        verify(requestPermissionsViewModel).permissionsRequested()
    }
}
