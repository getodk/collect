package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.odk.collect.strings.R

class AddNewRepeatDialog(private val repeatName: String?) : Page<AddNewRepeatDialog>() {
    override fun assertOnPage(): AddNewRepeatDialog {
        val dialogMessage = if (repeatName.isNullOrBlank()) {
            getTranslatedString(R.string.add_another_question)
        } else {
            getTranslatedString(R.string.add_repeat_question, repeatName)
        }
        onView(withText(dialogMessage))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        return this
    }

    fun <D : Page<D>> clickOnAdd(destination: D): D {
        return clickOnTextInDialog(R.string.add_repeat, destination)
    }

    fun <D : Page<D>> clickOnDoNotAdd(destination: D): D {
        return clickOnTextInDialog(R.string.cancel, destination)
    }
}
