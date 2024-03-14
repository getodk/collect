package org.odk.collect.androidshared.ui.multiselect

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.strings.R.string

@RunWith(AndroidJUnit4::class)
class MultiSelectControlsFragmentTest {

    private val data = MutableLiveData(listOf(MultiSelectItem(1, null), MultiSelectItem(2, null)))
    private val multiSelectViewModel = MultiSelectViewModel(data)

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(MultiSelectControlsFragment::class) {
                MultiSelectControlsFragment("Action", multiSelectViewModel)
            }.build()
    )

    @Test
    fun `clicking select all selects all items`() {
        fragmentScenarioLauncherRule.launchInContainer(MultiSelectControlsFragment::class.java)

        onView(withText(string.select_all)).perform(click())
        assertThat(multiSelectViewModel.getSelected().getOrAwaitValue(), equalTo(setOf<Long>(1, 2)))
    }

    @Test
    fun `clicking clear all unselects all items`() {
        fragmentScenarioLauncherRule.launchInContainer(MultiSelectControlsFragment::class.java)

        onView(withText(string.select_all)).perform(click())
        onView(withText(string.select_all)).check(doesNotExist())

        onView(withText(string.clear_all)).perform(click())
        assertThat(multiSelectViewModel.getSelected().getOrAwaitValue(), equalTo(emptySet()))
    }

    @Test
    fun `action is disabled when nothing is selected`() {
        fragmentScenarioLauncherRule.launchInContainer(MultiSelectControlsFragment::class.java)
        onView(withText("Action")).check(matches(not(isEnabled())))

        multiSelectViewModel.select(1)
        onView(withText("Action")).check(matches(isEnabled()))
    }
}
