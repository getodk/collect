package org.odk.collect.android.instancemanagement

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import org.odk.collect.testshared.ViewActions.clickOnItemWith
import org.odk.collect.testshared.ViewMatchers.recyclerView

@RunWith(AndroidJUnit4::class)
class DeleteSavedFormFragmentTest {

    private val formsToDisplay = MutableLiveData<List<Instance>>(emptyList())
    private val savedFormListViewModel = mock<SavedFormListViewModel> {
        on { formsToDisplay } doReturn formsToDisplay
    }

    private val viewModelFactory = viewModelFactory {
        addInitializer(SavedFormListViewModel::class) { savedFormListViewModel }
    }

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(DeleteSavedFormFragment::class) {
                DeleteSavedFormFragment(viewModelFactory)
            }.build()
    )

    @Test
    fun `recreating maintains selection`() {
        val fragmentScenario = fragmentScenarioLauncherRule.launchInContainer(DeleteSavedFormFragment::class.java)
        formsToDisplay.value = listOf(
            InstanceFixtures.instance(dbId = 1, displayName = "Form 1"),
            InstanceFixtures.instance(dbId = 2, displayName = "Form 2")
        )

        onView(recyclerView()).perform(clickOnItemWith(withText("Form 2")))

        fragmentScenario.recreate()
        onView(withRecyclerView(R.id.list).atPositionOnView(1, R.id.form_title)).check(matches(withText("Form 2")))
        onView(withRecyclerView(R.id.list).atPositionOnView(1, R.id.checkbox)).check(matches(isChecked()))
    }
}
