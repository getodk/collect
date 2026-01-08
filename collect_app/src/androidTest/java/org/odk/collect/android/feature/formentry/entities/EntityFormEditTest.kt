package org.odk.collect.android.feature.formentry.entities

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.StubOpenRosaServer.EntityListItem
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class EntityFormEditTest {
    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun editingEntityRegistrationForm_doesNotCreateEntityForFollowUpForms() {
        testDependencies.server.addForm("one-question-entity-registration-editable.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration Editable")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))
            .clickSendFinalizedForm(1)
            .clickOnForm("One Question Entity Registration Editable")
            .editForm("One Question Entity Registration Editable")
            .clickOnQuestion("Name")
            .answerQuestion("Name", "Kendall Roy")
            .swipeToEndScreen("One Question Entity Registration Editable (Edit 1)")
            .clickFinalize()

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertTexts("Roman Roy", "Logan Roy")
            .assertTextDoesNotExist("Kendall Roy")
    }

    @Test
    fun editingEntityUpdateForm_doesNotUpdateEntityForFollowUpForms() {
        testDependencies.server.addForm(
            "one-question-entity-update-editable.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Update Editable")
            .assertQuestion("Select person")
            .clickOnText("Roman Roy")
            .swipeToNextQuestion("Name")
            .swipeToEndScreen()
            .clickFinalize()
            .clickSendFinalizedForm(1)
            .clickOnForm("One Question Entity Update Editable")
            .editForm("One Question Entity Update Editable")
            .clickOnQuestion("Name")
            .answerQuestion("Name", "Romulus Roy")
            .swipeToEndScreen("One Question Entity Update Editable (Edit 1)")
            .clickFinalize()

            .startBlankForm("One Question Entity Update Editable")
            .assertText("Roman Roy")
            .assertTextDoesNotExist("Romulus Roy")
    }
}
