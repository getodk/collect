package org.odk.collect.android.preferences.dialogs

import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment.AdminPasswordDialogCallback
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.TestActivityScenario
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class AdminPasswordDialogFragmentTest {
    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesAdminPasswordProvider(settingsProvider: SettingsProvider): AdminPasswordProvider {
                return StubAdminPasswordProvider()
            }
        })
    }

    @Test
    fun enteringPassword_andClickingOK_callsOnCorrectAdminPassword() {
        val activityScenario = TestActivityScenario.launch(
            SpyAdminPasswordDialogCallbackActivity::class.java
        )
        activityScenario.onActivity { activity: SpyAdminPasswordDialogCallbackActivity ->
            val fragment = AdminPasswordDialogFragment()
            fragment.show(activity.supportFragmentManager, "tag")
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            fragment.binding.editText.setText("password")
            (fragment.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertThat(activity.onCorrectAdminPasswordCalled, equalTo(true))
            assertThat(
                activity.onIncorrectAdminPasswordCalled,
                equalTo(false)
            )
        }
    }

    @Test
    fun enteringIncorrectPassword_andClickingOK_callsOnInCorrectAdminPassword() {
        val activityScenario = TestActivityScenario.launch(
            SpyAdminPasswordDialogCallbackActivity::class.java
        )
        activityScenario.onActivity { activity: SpyAdminPasswordDialogCallbackActivity ->
            val fragment = AdminPasswordDialogFragment()
            fragment.show(activity.supportFragmentManager, "tag")
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            fragment.binding.editText.setText("not the password")
            (fragment.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertThat(activity.onCorrectAdminPasswordCalled, equalTo(false))
            assertThat(
                activity.onIncorrectAdminPasswordCalled,
                equalTo(true)
            )
        }
    }

    @Test
    fun afterRecreating_enteringPassword_andClickingOK_callsOnCorrectAdminPasswordWithAction() {
        val activityScenario = TestActivityScenario.launch(
            SpyAdminPasswordDialogCallbackActivity::class.java
        )
        activityScenario.onActivity { activity: SpyAdminPasswordDialogCallbackActivity ->
            val fragment = AdminPasswordDialogFragment()
            fragment.show(activity.supportFragmentManager, "tag")
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
        activityScenario.recreate()
        activityScenario.onActivity { activity: SpyAdminPasswordDialogCallbackActivity ->
            val fragment =
                activity.supportFragmentManager.findFragmentByTag("tag") as AdminPasswordDialogFragment?
            fragment!!.binding.editText.setText("password")
            (fragment.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertThat(activity.onCorrectAdminPasswordCalled, equalTo(true))
            assertThat(
                activity.onIncorrectAdminPasswordCalled,
                equalTo(false)
            )
        }
    }

    private class StubAdminPasswordProvider : AdminPasswordProvider(null) {
        override fun isAdminPasswordSet(): Boolean {
            return true
        }

        override fun getAdminPassword(): String {
            return "password"
        }
    }

    private class SpyAdminPasswordDialogCallbackActivity : FragmentActivity(), AdminPasswordDialogCallback {
        var onCorrectAdminPasswordCalled = false
        var onIncorrectAdminPasswordCalled = false

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setTheme(R.style.Theme_AppCompat) // Needed for androidx.appcompat.app.AlertDialog
        }

        override fun onCorrectAdminPassword() {
            onCorrectAdminPasswordCalled = true
        }

        override fun onIncorrectAdminPassword() {
            onIncorrectAdminPasswordCalled = true
        }
    }
}
