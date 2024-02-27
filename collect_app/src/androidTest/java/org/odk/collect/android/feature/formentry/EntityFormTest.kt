package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class EntityFormTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun fillingEntityRegistrationForm_createsEntityInTheBrowser() {
        testDependencies.server.addForm("one-question-entity-registration.xml")

        rule.withMatchExactlyProject(testDependencies.server.url)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))
            .openEntityBrowser()
            .clickOnDataset("people")
            .assertEntity("full_name: Logan Roy")
    }

    @Test
    fun fillingEntityRegistrationForm_createsEntityForFollowUpForms() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm("one-question-entity-update.xml", listOf("people.csv"))

        rule.withMatchExactlyProject(testDependencies.server.url)
            .enableLocalEntitiesInForms()

            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertText("Roman Roy")
            .assertText("Logan Roy")
    }

    @Test
    fun fillingEntityRegistrationForm_createsEntityForFollowUpFormsWithCachedFormDefs() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm("one-question-entity-update.xml", listOf("people.csv"))

        rule.withMatchExactlyProject(testDependencies.server.url)
            .enableLocalEntitiesInForms()

            .startBlankForm("One Question Entity Update") // Open to create cached form def
            .pressBackAndDiscardForm()

            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertText("Roman Roy")
            .assertText("Logan Roy")
    }

    @Test
    fun fillingEntityUpdateForm_updatesEntityForFollowUpForms() {
        testDependencies.server.addForm("one-question-entity-update.xml", listOf("people.csv"))

        rule.withMatchExactlyProject(testDependencies.server.url)
            .enableLocalEntitiesInForms()

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
    fun fillingEntityFollowUpForm_whenOnlineDuplicateHasHigherVersion_showsOnlineVersion() {
        testDependencies.server.addForm("one-question-entity-update.xml", listOf("people.csv"))

        val mainMenuPage = rule.withMatchExactlyProject(testDependencies.server.url)
            .enableLocalEntitiesInForms()

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .clickOnText("Roman Roy")
            .swipeToNextQuestion("Name")
            .answerQuestion("Name", "Romulus Roy")
            .swipeToEndScreen()
            .clickFinalize()

        testDependencies.server.updateMediaFile(
            "one-question-entity-update.xml",
            "people.csv",
            "updated-people.csv"
        )

        mainMenuPage.clickFillBlankForm()
            .clickRefresh()
            .clickOnForm("One Question Entity Update")
            .assertText("Ro-Ro Roy")
            .assertTextDoesNotExist("Romulus Roy")
            .assertTextDoesNotExist("Roman Roy")
    }

    @Test
    fun fillingEntityFollowUpForm_whenOfflineDuplicateHasHigherVersion_showsOfflineVersion() {
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            mapOf("people.csv" to "updated-people.csv")
        )

        rule.withMatchExactlyProject(testDependencies.server.url)
            .enableLocalEntitiesInForms()

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .clickOnText("Ro-Ro Roy")
            .swipeToNextQuestion("Name")
            .answerQuestion("Name", "Romulus Roy")
            .swipeToEndScreen()
            .clickFinalize()

            .startBlankForm("One Question Entity Update")
            .assertText("Romulus Roy")
            .assertTextDoesNotExist("Ro-Ro Roy")
    }

    @Test
    fun fillingEntityUpdateForm_whenFormDoesNotUpdateLabel_updatesEntityForFollowUpForms() {
        testDependencies.server.addForm(
            "one-question-entity-update-no-label.xml",
            listOf("people.csv")
        )

        rule.withMatchExactlyProject(testDependencies.server.url)
            .enableLocalEntitiesInForms()

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .clickOnText("Roman Roy")
            .swipeToNextQuestion("Name")
            .answerQuestion("Name", "Romulus Roy")
            .swipeToEndScreen()
            .clickFinalize()

            .startBlankForm("One Question Entity Update")
            .assertText("Roman Roy")
            .assertTextDoesNotExist("Romulus Roy")
    }
}
