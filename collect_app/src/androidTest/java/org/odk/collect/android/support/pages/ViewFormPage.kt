package org.odk.collect.android.support.pages

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.odk.collect.android.R

class ViewFormPage(private val formName: String) : Page<ViewFormPage>() {

    override fun assertOnPage(): ViewFormPage {
        assertToolbarTitle(formName)
        assertText(org.odk.collect.strings.R.string.exit)
        return this
    }

    fun clickOnGroup(groupLabel: String): ViewFormPage {
        onView(withId(R.id.list)).perform(scrollTo<RecyclerView.ViewHolder>(
            hasDescendant(withText(groupLabel)))
        )

        clickOnText(groupLabel)
        return this
    }

    fun editForm(formName: String): FormHierarchyPage {
        onView(withId(R.id.menu_edit)).perform(ViewActions.click())
        return FormHierarchyPage(formName).assertOnPage()
    }
}
