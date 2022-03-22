package org.odk.collect.android.widgets.items

import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule

@RunWith(AndroidJUnit4::class)
class SelectOneFromMapDialogFragmentTest {

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Test
    fun `pressing back dismisses dialog`() {
        val scenario = launcherRule.launchDialogFragment(SelectOneFromMapDialogFragment::class.java)
        scenario.onFragment {
            Espresso.pressBack()
            assertThat(it.isVisible, equalTo(false))
        }
    }
}
