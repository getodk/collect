package org.odk.collect.android.feature.formmanagement

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.matchers.DrawableMatcher.withImageDrawable
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView

@RunWith(AndroidJUnit4::class)
class ListOfFormsTest {
    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun sortByDialog_ShouldBeTranslatedAndDisplayProperIcons() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .clickOnLanguage()
            .clickOnSelectedLanguage("Deutsch")
            .clickFillBlankForm()
            .clickOnSortByButton()
            .assertText("Sortieren nach")

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(0, R.id.title)
        ).check(matches(withText("Name, A-Z")))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(0, R.id.icon)
        ).check(matches(withImageDrawable(R.drawable.ic_sort_by_alpha)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(1, R.id.title)
        ).check(matches(withText("Name, Z-A")))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(1, R.id.icon)
        ).check(matches(withImageDrawable(R.drawable.ic_sort_by_alpha)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(2, R.id.title)
        ).check(matches(withText("Datum, neuestes zuerst")))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(2, R.id.icon)
        ).check(matches(withImageDrawable(R.drawable.ic_access_time)))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(3, R.id.title)
        ).check(matches(withText("Datum, ältestes zuerst")))

        onView(
            withRecyclerView(R.id.recyclerView).atPositionOnView(3, R.id.icon)
        ).check(matches(withImageDrawable(R.drawable.ic_access_time)))

        Espresso.pressBack()
        Espresso.pressBack()

        MainMenuPage()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .clickOnLanguage()
            .clickOnSelectedLanguage("English")
    }
}
