package org.odk.collect.android.projects

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.CodeCaptureManagerFactory
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class AutomaticProjectCreatorDialogTest {

    private val codeCaptureManagerFactory: CodeCaptureManagerFactory = mock {}

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesCodeCaptureManagerFactory(): CodeCaptureManagerFactory {
                return codeCaptureManagerFactory
            }
        })
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'Cancel' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AutomaticProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.cancel)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(AutomaticProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The ManualProjectCreatorDialog should be displayed after switching to the manual mode`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AutomaticProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withText(R.string.configure_manually)).perform(scrollTo(), click())
            assertThat(it.activity!!.supportFragmentManager.findFragmentByTag(ManualProjectCreatorDialog::class.java.name), `is`(notNullValue()))
        }
    }
}
