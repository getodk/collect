package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.not
import org.odk.collect.android.R

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

    fun checkIfSaveFormOptionIsDisabled(): AccessControlPage {
        onView(withText(getTranslatedString(R.string.save_all_answers))).check(matches(not(isEnabled())))
        return this
    }

    fun uncheckServerOption(): AccessControlPage {
        clickOnString(R.string.server_settings_title)
        return this
    }
}
