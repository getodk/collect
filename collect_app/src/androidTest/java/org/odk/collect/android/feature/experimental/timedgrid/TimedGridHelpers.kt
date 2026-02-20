package org.odk.collect.android.feature.experimental.timedgrid

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.odk.collect.android.R
import org.odk.collect.android.support.matchers.CustomMatchers.withIndex
import org.odk.collect.android.support.pages.FormEntryPage
import java.util.function.Consumer

object TimedGridHelpers {
    fun FormEntryPage.clickStartTestButton(): FormEntryPage {
        onView(withId(org.odk.collect.experimental.R.id.button_start)).perform(scrollTo(), click())
        return this
    }

    fun FormEntryPage.clickPauseTestButton(): FormEntryPage {
        onView(withId(org.odk.collect.experimental.R.id.button_timer)).perform(scrollTo(), click())
        return this
    }

    fun FormEntryPage.selectTestAnswers(items: List<String>): FormEntryPage {
        items.forEach(Consumer { item: String? ->
            onView(withIndex(ViewMatchers.withText(item), 0)).perform(click())
        })
        return this
    }

    fun FormEntryPage.clickUntilEarlyFinish(): FormEntryPage {
        var found = false
        while (!found) {
            try {
                // Try to click Early Finish directly by ID
                onView(withId(org.odk.collect.experimental.R.id.button_complete)).perform(scrollTo(), click())
                found = true
            } catch (e: Exception) {
                // If not clickable yet, go to next page
                onView(withId(org.odk.collect.experimental.R.id.button_next)).perform(scrollTo(), click())
            }
        }
        return this
    }

    fun FormEntryPage.clickForwardButtonWithError(): FormEntryPage {
        closeSoftKeyboard()
        onView(ViewMatchers.withText(getTranslatedString(org.odk.collect.strings.R.string.form_forward))).perform(
            click()
        )
        assertNavigationBlockedWarning()
        return this
    }

    fun FormEntryPage.clickGoToArrowWithError(): FormEntryPage {
        onView(withId(R.id.menu_goto)).perform(click())
        assertNavigationBlockedWarning()
        return this
    }

    fun FormEntryPage.clickProjectSettingsWithError(): FormEntryPage {
        onView(ViewMatchers.withText(getTranslatedString(org.odk.collect.strings.R.string.project_settings))).perform(
            click()
        )
        assertNavigationBlockedWarning()
        return this
    }

    fun FormEntryPage.assertEarlyFinishDialogAndConfirm(): FormEntryPage {
        assertText("Early Finish")
        assertText("Do you want to end the test now?")
        clickOnText("End Test")
        return this
    }

    fun FormEntryPage.assertLastAttemptedItemDialogAndConfirm(item: String): FormEntryPage {
        clickOnText(item)
        assertText("Last Attempted Item")
        assertText("Do you want to confirm the last attempted item?")
        clickOnText("Yes")
        return this
    }

    fun FormEntryPage.assertTestEndedEarlyDialogAndConfirm(endAfter: Int): FormEntryPage {
        assertText("Test Ended Early")
        assertText("You have reached the limit of $endAfter consecutive wrong answers.")
        clickOnText("OK")
        return this
    }

    fun FormEntryPage.assertConsecutiveMistakesDialogAndContinue(endAfter: Int): FormEntryPage {
        assertText("Consecutive Mistakes")
        assertText("You have made $endAfter consecutive mistakes. Do you want to end the test or continue?")
        clickOnText("Continue")
        return this
    }

    fun FormEntryPage.clickFinishTestButton(): FormEntryPage {
        onView(withId(org.odk.collect.experimental.R.id.button_finish)).perform(scrollTo(), click())
        return this
    }

    private fun FormEntryPage.assertNavigationBlockedWarning() {
        assertText("Assessment…")
        assertText("You must finish assessment before leaving this screen.")
        clickOnText("OK")
    }

    /**
     * Generate expected correct items string dynamically.
     */
    fun expectedCorrectItems(
        allItems: List<String>,
        tapped: Set<String>,
        lastAttemptedItem: String? = null
    ): String {
        val cutoffIndex = if (lastAttemptedItem != null) {
            allItems.indexOf(lastAttemptedItem).takeIf { it != -1 } ?: allItems.lastIndex
        } else {
            allItems.lastIndex
        }

        val subset = allItems.subList(0, cutoffIndex + 1).toMutableList()

        // Remove only the first occurrence of each tapped item
        tapped.forEach { tappedItem ->
            val idx = subset.indexOf(tappedItem)
            if (idx != -1) {
                subset.removeAt(idx)
            }
        }

        return subset.joinToString(", ")
    }

    /**
     * Returns the list of items not attempted/answered,
     * i.e. everything after the lastAttemptedItem in the full list.
     */
    fun notAttemptedItems(allItems: List<String>, lastAttemptedItem: String): List<String> {
        val index = allItems.indexOf(lastAttemptedItem)
        return if (index != -1 && index < allItems.size - 1) {
            allItems.subList(index + 1, allItems.size)
        } else {
            emptyList()
        }
    }

    fun extractTimeLeft(text: String): Int {
        // Example: "Time Left: 58" -> 58
        return text.substringAfter("Time Left: ").trim().toInt()
    }

    fun getTextFromView(resId: Int): String {
        var text = ""
        onView(withId(resId)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return Matchers.instanceOf(TextView::class.java)
            }

            override fun getDescription() = "Get text from a TextView"

            override fun perform(uiController: UiController?, view: View?) {
                val tv = view as TextView
                text = tv.text.toString()
            }
        })
        return text
    }

    fun assertVisibleRows(count: Int) {
        onView(withId(org.odk.collect.experimental.R.id.container_rows))
            .check(assertVisibleChildCount(count))
    }

    private fun assertVisibleChildCount(expected: Int): ViewAssertion {
        return ViewAssertion { view, noViewFoundException ->
            if (noViewFoundException != null) throw noViewFoundException
            if (view !is ViewGroup) throw AssertionError("View is not a ViewGroup")

            val visibleCount = (0 until view.childCount)
                .map { view.getChildAt(it) }
                .count { it.visibility == View.VISIBLE }

            assertThat(
                "Expected $expected visible children but was $visibleCount",
                visibleCount,
                equalTo(expected)
            )
        }
    }
}
