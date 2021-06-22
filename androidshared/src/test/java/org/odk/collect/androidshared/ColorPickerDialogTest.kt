package org.odk.collect.androidshared

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalToIgnoringCase
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.support.RobolectricApplication
import org.odk.collect.fragmentstest.DialogFragmentTest
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class ColorPickerDialogTest {
    private val colorPickerViewModel = ColorPickerViewModel()

    private val application: RobolectricApplication by lazy { ApplicationProvider.getApplicationContext() }

    @Before
    fun setup() {
        colorPickerViewModel.initColor = "#cccccc"
        colorPickerViewModel.icon = "P"

        application.setupDependencies(
            object : AndroidSharedDependencyModule() {
                override fun providesColorPickerViewModel(): ColorPickerViewModel.Factory {
                    return object : ColorPickerViewModel.Factory() {
                        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                            return colorPickerViewModel as T
                        }
                    }
                }
            }
        )
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = launchFragment()
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on the cancel button`() {
        val scenario = launchFragment()
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_NEGATIVE)).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `No more than six characters should be accepted as hex color`() {
        val scenario = launchFragment()
        scenario.onFragment {
            assertThat(it.binding.hexColor.length(), `is`(6))
        }
    }

    @Test
    fun `Current color should be set properly after opening the dialog`() {
        val scenario = launchFragment()
        scenario.onFragment {
            assertCurrentColor(it, "cccccc")
        }
    }

    @Test
    fun `Current icon should be set properly after opening the dialog`() {
        val scenario = launchFragment()
        scenario.onFragment {
            assertThat(it.binding.currentColor.text.toString(), equalToIgnoringCase("P"))
        }
    }

    @Test
    fun `Selected color should be remembered after dialog recreation`() {
        val scenario = launchFragment()
        scenario.onFragment {
            it.binding.color5.performClick()
            assertCurrentColor(it, "2296F3")

            scenario.recreate()

            assertCurrentColor(it, "2296F3")
        }
    }

    @Test
    fun `Selecting any color should update the current color`() {
        val scenario = launchFragment()
        scenario.onFragment {
            it.binding.color1.performClick()
            assertCurrentColor(it, "EA4633")

            it.binding.color2.performClick()
            assertCurrentColor(it, "E9527E")

            it.binding.color3.performClick()
            assertCurrentColor(it, "9F50B0")

            it.binding.color4.performClick()
            assertCurrentColor(it, "3F51B5")

            it.binding.color5.performClick()
            assertCurrentColor(it, "2296F3")

            it.binding.color6.performClick()
            assertCurrentColor(it, "53BDD4")

            it.binding.color7.performClick()
            assertCurrentColor(it, "489789")

            it.binding.color8.performClick()
            assertCurrentColor(it, "5DAF50")

            it.binding.color9.performClick()
            assertCurrentColor(it, "8BC34A")

            it.binding.color10.performClick()
            assertCurrentColor(it, "CDDC39")

            it.binding.color11.performClick()
            assertCurrentColor(it, "FFEB3B")

            it.binding.color12.performClick()
            assertCurrentColor(it, "F9C028")

            it.binding.color13.performClick()
            assertCurrentColor(it, "F2972C")

            it.binding.color14.performClick()
            assertCurrentColor(it, "795548")

            it.binding.color15.performClick()
            assertCurrentColor(it, "9E9E9E")
        }
    }

    private fun launchFragment(): FragmentScenario<ColorPickerDialog> {
        return DialogFragmentTest.launchDialogFragment(ColorPickerDialog::class.java)
    }

    private fun assertCurrentColor(fragment: ColorPickerDialog, color: String) {
        assertThat(fragment.binding.hexColor.text.toString(), equalToIgnoringCase(color))

        val currentColor = (fragment.binding.currentColor.background as GradientDrawable).color!!.defaultColor

        assertThat(currentColor, `is`(Color.parseColor("#$color")))
    }
}
