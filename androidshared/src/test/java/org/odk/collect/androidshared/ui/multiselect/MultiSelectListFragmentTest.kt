package org.odk.collect.androidshared.ui.multiselect

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.R
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.multiselect.support.TextAndCheckBoxView
import org.odk.collect.androidshared.ui.multiselect.support.TextAndCheckBoxViewHolder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import org.odk.collect.testshared.ViewActions.clickOnItemWith
import org.odk.collect.testshared.ViewMatchers.recyclerView

@RunWith(AndroidJUnit4::class)
class MultiSelectListFragmentTest {

    private val data = MutableLiveData<List<MultiSelectItem<String>>>(emptyList())
    private val multiSelectViewModel = MultiSelectViewModel(data)

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(MultiSelectListFragment::class) {
                MultiSelectListFragment(
                    "Action",
                    multiSelectViewModel,
                    { parent -> TextAndCheckBoxViewHolder(parent.context) }
                )
            }.build()
    )

    @Test
    fun `empty message shows when there are no forms`() {
        fragmentScenarioLauncherRule.launchInContainer(MultiSelectListFragment::class.java)
        onView(withId(R.id.empty)).check(matches(isDisplayed()))

        data.value = listOf(MultiSelectItem(1, "Blah"))
        onView(withId(R.id.empty)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `bottom buttons are hidden when there are no forms`() {
        fragmentScenarioLauncherRule.launchInContainer(MultiSelectListFragment::class.java)
        onView(withId(R.id.buttons)).check(matches(not(isDisplayed())))

        data.value = listOf(MultiSelectItem(1, "Blah"))
        onView(withId(R.id.buttons)).check(matches(isDisplayed()))
    }

    @Test
    fun `recreating maintains selection`() {
        val scenario =
            fragmentScenarioLauncherRule.launchInContainer(MultiSelectListFragment::class.java)
        data.value = listOf(MultiSelectItem(1, "Blah 1"), MultiSelectItem(2, "Blah 2"))

        onView(recyclerView()).perform(clickOnItemWith(withText("Blah 2")))

        scenario.recreate()
        onView(withRecyclerView(R.id.list).atPositionOnView(1, TextAndCheckBoxView.TEXT_VIEW_ID))
            .check(matches(withText("Blah 2")))
        onView(withRecyclerView(R.id.list).atPositionOnView(1, TextAndCheckBoxView.CHECK_BOX_ID))
            .check(matches(isChecked()))
    }
}
