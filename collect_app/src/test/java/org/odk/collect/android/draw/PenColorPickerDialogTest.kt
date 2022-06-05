package org.odk.collect.android.draw

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.settings.Settings

@RunWith(AndroidJUnit4::class)
class PenColorPickerDialogTest {

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(defaultThemeResId = R.style.Theme_MaterialComponents)

    @Test
    fun `dialog should be cancelable`() {
        val scenario = launcherRule.launch(PenColorPickerDialog::class.java)
        scenario.onFragment {
            assertThat(it.isCancelable, `is`(true))
        }
    }

    @Test
    fun `pen color in view model should be set after clicking ok`() {
        val viewModel = mock<PenColorPickerViewModel>().also {
            whenever(it.penColor).thenReturn(MutableNonNullLiveData(Color.BLACK))
        }

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesPenColorPickerViewModel(settingsProvider: SettingsProvider): PenColorPickerViewModel.Factory {
                return TestFactory(InMemSettingsProvider().getMetaSettings(), viewModel)
            }
        })

        launcherRule.launch(PenColorPickerDialog::class.java)

        onView(withText(R.string.ok)).inRoot(isDialog()).perform(click())

        verify(viewModel).setPenColor(Color.BLACK)
    }

    private class TestFactory(
        metaSettings: Settings,
        private val viewModel: ViewModel
    ) : PenColorPickerViewModel.Factory(metaSettings) {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return viewModel as T
        }
    }
}
