package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.not
import org.odk.collect.android.R
import org.odk.collect.android.support.matchers.CustomMatchers.withIndex

class AccessControlPage : Page<AccessControlPage>() {

    override fun assertOnPage(): AccessControlPage {
        assertText(R.string.access_control_section_title)
        return this
    }

    fun openUserSettings(): AccessControlPage {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.user_settings))
        return this
    }

    fun clickFormEntrySettings(): AccessControlPage {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.form_entry_setting))
        return this
    }

    fun clickMovingBackwards(): AccessControlPage {
        clickOnString(R.string.moving_backwards_title)
        return this
    }

    fun assertGoToPromptEnabled(): AccessControlPage {
        onView(withText(getTranslatedString(R.string.view_hierarchy))).check(matches(isEnabled()))
        return this
    }

    fun assertGoToPromptDisabled(): AccessControlPage {
        onView(withText(getTranslatedString(R.string.view_hierarchy))).check(matches(not(isEnabled())))
        return this
    }

    fun assertGoToPromptChecked(): AccessControlPage {
        onView(withIndex(withClassName(endsWith("CheckBox")), 3)).check(matches(isChecked()))
        return this
    }

    fun assertGoToPromptUnchecked(): AccessControlPage {
        onView(withIndex(withClassName(endsWith("CheckBox")), 3)).check(matches(isNotChecked()))
        return this
    }

    fun assertSaveAsDraftInFormEntryEnabled(): AccessControlPage {
        onView(withIndex(withText(getTranslatedString(R.string.save_mid)), 0)).check(matches(isEnabled()))
        return this
    }

    fun assertSaveAsDraftInFormEntryDisabled(): AccessControlPage {
        onView(withIndex(withText(getTranslatedString(R.string.save_mid)), 0)).check(matches(not(isEnabled())))
        return this
    }

    fun assertSaveAsDraftInFormEntryChecked(): AccessControlPage {
        onView(withIndex(withClassName(endsWith("CheckBox")), 4)).check(matches(isChecked()))
        return this
    }

    fun assertSaveAsDraftInFormEntryUnchecked(): AccessControlPage {
        onView(withIndex(withClassName(endsWith("CheckBox")), 4)).check(matches(isNotChecked()))
        return this
    }

    fun assertSaveAsDraftInFormEndEnabled(): AccessControlPage {
        onView(withIndex(withText(getTranslatedString(R.string.save_as_draft)), 1)).check(matches(isEnabled()))
        return this
    }

    fun assertSaveAsDraftInFormEndDisabled(): AccessControlPage {
        onView(withIndex(withText(getTranslatedString(R.string.save_as_draft)), 1)).check(matches(not(isEnabled())))
        return this
    }

    fun assertSaveAsDraftInFormEndChecked(): AccessControlPage {
        onView(withIndex(withClassName(endsWith("CheckBox")), 5)).check(matches(isChecked()))
        return this
    }

    fun assertSaveAsDraftInFormEndUnchecked(): AccessControlPage {
        onView(withIndex(withClassName(endsWith("CheckBox")), 5)).check(matches(isNotChecked()))
        return this
    }

    fun assertFinalizeEnabled(): AccessControlPage {
        onView(withText(getTranslatedString(R.string.finalize))).check(matches(isEnabled()))
        return this
    }

    fun assertFinalizeDisabled(): AccessControlPage {
        onView(withText(getTranslatedString(R.string.finalize))).check(matches(not(isEnabled())))
        return this
    }

    fun assertFinalizeChecked(): AccessControlPage {
        onView(withIndex(withClassName(endsWith("CheckBox")), 6)).check(matches(isChecked()))
        return this
    }

    fun assertFinalizeUnchecked(): AccessControlPage {
        onView(withIndex(withClassName(endsWith("CheckBox")), 6)).check(matches(isNotChecked()))
        return this
    }

    fun clickOnSaveAsDraftInFormEnd(): AccessControlPage {
        onView(withIndex(withText(getTranslatedString(R.string.save_mid)), 1)).perform(click())
        return this
    }

    fun uncheckServerOption(): AccessControlPage {
        clickOnString(R.string.server_settings_title)
        return this
    }
}
