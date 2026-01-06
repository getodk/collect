package org.odk.collect.android.feature.formentry

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
class EntityFormCreateUpdateTest {
    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun fillingEntityRegistrationForm_createsEntityForFollowUpForms() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertTexts("Roman Roy", "Logan Roy")
    }

    @Test
    fun fillingEntityRegistrationForm_createsEntityForFollowUpFormsWithCachedFormDefs() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Update") // Open to create cached form def
            .pressBackAndDiscardForm()

            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertTexts("Roman Roy", "Logan Roy")
    }

    @Test
    fun fillingEntityUpdateForm_updatesEntityForFollowUpForms() {
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .clickOnText("Roman Roy")
            .swipeToNextQuestion("Name")
            .answerQuestion("Name", "Romulus Roy")
            .swipeToEndScreen()
            .clickFinalize()

            .startBlankForm("One Question Entity Update")
            .assertText("Romulus Roy")
            .assertTextDoesNotExist("Roman Roy")
    }

    @Test
    fun fillingEntityCreateForm_withUpdate_doesNotCreateEntityForFollowUpForms() {
        testDependencies.server.addForm("one-question-entity-create-and-update.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertText("Roman Roy")
            .assertTextDoesNotExist("Logan Roy")
    }

    @Test
    fun fillingEntityUpdateForm_withCreate_doesNotUpdateEntityForFollowUpForms() {
        testDependencies.server.addForm(
            "one-question-entity-update-and-create.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .clickOnText("Roman Roy")
            .swipeToNextQuestion("Name")
            .answerQuestion("Name", "Romulus Roy")
            .swipeToEndScreen()
            .clickFinalize()

            .startBlankForm("One Question Entity Update")
            .assertTextDoesNotExist("Romulus Roy")
            .assertText("Roman Roy")
    }
}
