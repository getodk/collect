package org.odk.collect.android.support.pages

import androidx.appcompat.widget.AppCompatImageButton
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.odk.collect.android.R

class ErrorPage : Page<ErrorPage>() {

    override fun assertOnPage(): ErrorPage {
        assertText(R.string.errors)
        return this
    }

    fun assertError(errorMessage: String): ErrorPage {
        assertText(errorMessage)
        return this
    }

    fun navigateBack(): MainMenuPage {
        onView(allOf(instanceOf(AppCompatImageButton::class.java), withParent(withId(R.id.toolbar)))).perform(click())
        return MainMenuPage()
    }
}
