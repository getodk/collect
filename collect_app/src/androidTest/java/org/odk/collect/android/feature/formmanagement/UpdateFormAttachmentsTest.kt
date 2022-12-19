package org.odk.collect.android.feature.formmanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

// https://github.com/getodk/collect/issues/5384
@RunWith(AndroidJUnit4::class)
class UpdateFormAttachmentsTest {

    private val rule = CollectTestRule(false)
    private val testDependencies = TestDependencies()

    @get:Rule
    var chain: RuleChain = chain(testDependencies).around(rule)

    @Test
    fun updateOn_instead_addedOn_subtextShouldBeDisplayedAfterDownloadingNewAttachments() {
        testDependencies.server.addForm(
            "One Question",
            "one_question",
            "1",
            "one-question.xml"
        )

        val mainMenuPage = rule.withProject(testDependencies.server.url)
            .clickGetBlankForm()
            .assertTextNotDisplayed(R.string.newer_version_of_a_form_info)
            .clickGetSelected()
            .assertMessage("All downloads succeeded!")
            .clickOKOnDialog(MainMenuPage())
            .clickFillBlankForm()
            .assertTextThatContainsExists("Added on")
            .pressBack(MainMenuPage())
            .clickDeleteSavedForm()
            .clickBlankForms()
            .assertTextThatContainsExists("Added on")
            .pressBack(MainMenuPage())

        testDependencies.server.removeForm("One Question")

        testDependencies.server.addForm(
            "One Question",
            "one_question",
            "1",
            "one-question.xml",
            listOf("fruits.csv")
        )

        mainMenuPage.clickGetBlankForm()
            .assertText(R.string.newer_version_of_a_form_info)
            .clickGetSelected()
            .assertMessage("All downloads succeeded!")
            .clickOKOnDialog(MainMenuPage())
            .clickFillBlankForm()
            .assertTextThatContainsExists("Updated on")
            .pressBack(MainMenuPage())
            .clickDeleteSavedForm()
            .clickBlankForms()
            .assertTextThatContainsExists("Updated on")
    }

    @Test
    fun addedOn_subtextShouldBeDisplayedAfterDownloadingNewFormVersionEvenIfThatFormHasNewAttachments() {
        testDependencies.server.addForm(
            "One Question",
            "one_question",
            "1",
            "one-question.xml"
        )

        val mainMenuPage = rule.withProject(testDependencies.server.url)
            .clickGetBlankForm()
            .assertTextNotDisplayed(R.string.newer_version_of_a_form_info)
            .clickGetSelected()
            .assertMessage("All downloads succeeded!")
            .clickOKOnDialog(MainMenuPage())
            .clickFillBlankForm()
            .assertTextThatContainsExists("Added on")
            .pressBack(MainMenuPage())
            .clickDeleteSavedForm()
            .clickBlankForms()
            .assertTextThatContainsExists("Added on")
            .pressBack(MainMenuPage())

        testDependencies.server.removeForm("One Question")

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml",
            listOf("fruits.csv")
        )

        mainMenuPage.clickGetBlankForm()
            .assertText(R.string.newer_version_of_a_form_info)
            .clickGetSelected()
            .assertMessage("All downloads succeeded!")
            .clickOKOnDialog(MainMenuPage())
            .clickFillBlankForm()
            .assertTextThatContainsExists("Added on")
            .pressBack(MainMenuPage())
            .clickDeleteSavedForm()
            .clickBlankForms()
            .assertTextThatContainsExists("Added on")
            .assertTextThatContainsExists("Added on", 1)
    }
}
