package org.odk.collect.android.mainmenu

import android.Manifest
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
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
import org.odk.collect.android.R
import org.odk.collect.android.fakes.FakePermissionsProvider
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.MetaKeys
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class PermissionsDialogFragmentTest {

    private val permissionsProvider = FakePermissionsProvider()
    private val settingsProvider = InMemSettingsProvider()

    private val fragmentFactory = FragmentFactoryBuilder()
        .forClass(PermissionsDialogFragment::class) {
            PermissionsDialogFragment(settingsProvider, permissionsProvider)
        }
        .build()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(
        defaultThemeResId = R.style.Theme_MaterialComponents,
        defaultFactory = fragmentFactory
    )

    @Test
    @Config(sdk = [32])
    fun whenNoNeedToAskForNotificationPermission_dismisses() {
        val scenario = launcherRule.launch(
            initialState = Lifecycle.State.INITIALIZED,
            fragmentClass = PermissionsDialogFragment::class.java
        )

        scenario.onFragment {
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertThat(it.dialog?.isShowing, equalTo(false))
        }
    }

    @Test
    fun whenPermissionsHaveAlreadyBeenAskedFor_dismisses() {
        settingsProvider.getMetaSettings().save(MetaKeys.PERMISSIONS_REQUESTED, true)

        val scenario = launcherRule.launch(
            initialState = Lifecycle.State.INITIALIZED,
            fragmentClass = PermissionsDialogFragment::class.java
        )

        scenario.onFragment {
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertThat(it.dialog?.isShowing, equalTo(false))
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

    @Test
    fun clickingOK_marksPermissionsRequestedInSettings() {
        launcherRule.launch(PermissionsDialogFragment::class.java)

        onView(withText(R.string.ok)).inRoot(isDialog()).perform(click())
        assertThat(
            settingsProvider.getMetaSettings().getBoolean(MetaKeys.PERMISSIONS_REQUESTED),
            equalTo(true)
        )
    }

    @Test
    fun dismissing_doesNotMarkPermissionsRequestedInSettings() {
        launcherRule.launch(PermissionsDialogFragment::class.java)

        Espresso.pressBack()
        assertThat(
            settingsProvider.getMetaSettings().getBoolean(MetaKeys.PERMISSIONS_REQUESTED),
            equalTo(false)
        )
    }
}
