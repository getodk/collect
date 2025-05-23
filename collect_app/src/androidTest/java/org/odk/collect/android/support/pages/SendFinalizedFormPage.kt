package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.allOf
import org.odk.collect.android.support.matchers.CustomMatchers.withIndex
import org.odk.collect.strings.R

class SendFinalizedFormPage : Page<SendFinalizedFormPage>() {
    override fun assertOnPage(): SendFinalizedFormPage {
        onView(
            allOf(
                withText(getTranslatedString(R.string.send_data)),
                isDescendantOfA(withId(org.odk.collect.androidshared.R.id.toolbar))
            )
        ).check(matches(isDisplayed()))
        return this
    }

    fun clickOnForm(formLabel: String): ViewFormPage {
        clickOnText(formLabel)
        return ViewFormPage(formLabel).assertOnPage()
    }

    fun clickOnForm(formName: String, instanceName: String): ViewFormPage {
        clickOnText(instanceName)
        return ViewFormPage(formName).assertOnPage()
    }

    fun clickSendSelected(): OkDialog {
        clickOnText(getTranslatedString(R.string.send_selected_data))
        return OkDialog()
    }

    fun clickSendSelectedWithAuthenticationError(): ServerAuthDialog {
        clickOnText(getTranslatedString(R.string.send_selected_data))
        return ServerAuthDialog().assertOnPage()
    }

    fun clickSelectAll(): SendFinalizedFormPage {
        clickOnString(R.string.select_all)
        return this
    }

    @Deprecated("uses the deprecated {@link org.odk.collect.android.support.matchers.CustomMatchers#withIndex(Matcher, int)})} helper.")
    fun selectForm(index: Int): SendFinalizedFormPage {
        onView(withIndex(ViewMatchers.withId(androidx.appcompat.R.id.checkbox), index)).perform(click())
        return this
    }

    fun sortByDateOldestFirst(): SendFinalizedFormPage {
        onView(withId(org.odk.collect.android.R.id.menu_sort)).perform(click())
        clickOnString(R.string.sort_by_date_asc)
        return this
    }

    fun sortByDateNewestFirst(): SendFinalizedFormPage {
        onView(withId(org.odk.collect.android.R.id.menu_sort)).perform(click())
        clickOnString(R.string.sort_by_date_desc)
        return this
    }
}
