package org.odk.collect.android.support.pages

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.odk.collect.android.R
import org.odk.collect.testshared.WaitFor.waitFor
import java.util.concurrent.Callable

class FormHierarchyPage(private val formName: String) : Page<FormHierarchyPage>() {
    override fun assertOnPage(): FormHierarchyPage {
        // Make sure we've left the fill blank form screen
        waitFor(Callable {
            onView(withId(R.id.menu_goto)).check(doesNotExist())
            null
        } as Callable<*>)

        assertToolbarTitle(formName)
        assertText(org.odk.collect.strings.R.string.jump_to_beginning)
        assertText(org.odk.collect.strings.R.string.jump_to_end)
        return this
    }

    fun assertNotRemovableGroup(): FormHierarchyPage {
        onView(withId(R.id.menu_delete_child)).check(doesNotExist())
        return this
    }

    fun clickGoUpIcon(): FormHierarchyPage {
        onView(withId(R.id.menu_go_up)).perform(click())
        return this
    }

    fun clickGoToStart(): FormEntryPage {
        onView(withId(R.id.jumpBeginningButton)).perform(click())
        return FormEntryPage(formName).assertOnPage()
    }

    fun clickGoToEnd(): FormEndPage {
        return clickOnString(org.odk.collect.strings.R.string.jump_to_end)
            .assertOnPage<FormEndPage>(FormEndPage(formName))
    }

    fun clickGoToEnd(instanceName: String): FormEndPage {
        return clickOnString(org.odk.collect.strings.R.string.jump_to_end)
            .assertOnPage<FormEndPage>(FormEndPage(instanceName))
    }

    fun addGroup(): FormEntryPage {
        onView(withId(R.id.menu_add_repeat)).perform(click())
        return FormEntryPage(formName).assertOnPage()
    }

    fun deleteGroup(): FormHierarchyPage {
        onView(withId(R.id.menu_delete_child)).perform(click())
        return clickOnTextInDialog(
            org.odk.collect.strings.R.string.delete_repeat,
            this
        )
    }

    fun clickJumpEndButton(): FormEndPage {
        onView(withId(R.id.jumpEndButton)).perform(click())
        return FormEndPage(formName).assertOnPage()
    }

    fun assertPath(text: String): FormHierarchyPage {
        onView(withId(R.id.pathtext)).check(
            matches(withText(text))
        )
        return this
    }

    @JvmOverloads
    fun clickOnQuestion(questionLabel: String, isRequired: Boolean = false): FormEntryPage {
        var questionLabel = questionLabel
        if (isRequired) {
            questionLabel = "* $questionLabel"
        }

        onView(withId(R.id.list)).perform(
            scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(questionLabel))
            )
        )
        clickOnText(questionLabel)
        return FormEntryPage(formName)
    }

    fun clickOnGroup(groupLabel: String): FormHierarchyPage {
        onView(withId(R.id.list)).perform(
            scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(groupLabel))
            )
        )
        clickOnText(groupLabel)
        return this
    }
}
