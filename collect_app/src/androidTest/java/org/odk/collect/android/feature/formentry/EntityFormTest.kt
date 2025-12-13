package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.StubOpenRosaServer.EntityListItem
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.AddNewRepeatDialog
import org.odk.collect.android.support.pages.FormEndPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.strings.R

@RunWith(AndroidJUnit4::class)
class EntityFormTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun fillingEntityRegistrationFormWithNestedRepeats_createsEntitiesForFollowUpForms() {
        testDependencies.server.addForm("nested-repeats-with-entity-registration.xml")
        testDependencies.server.addForm(
            "nested-repeats-with-entity-update.xml",
            listOf(EntityListItem("people.csv"), EntityListItem("cars.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("Nested Repeats With Entity Registration")
            .answerQuestion("Name", "Logan Roy")
            .swipeToNextQuestion("Car model")
            .answerQuestion("Car model", "Kia Stonic")
            .swipeToNextQuestionWithRepeatGroup("Cars")
            .clickOnDoNotAdd(AddNewRepeatDialog("People"))
            .clickOnAdd(FormEntryPage("Nested Repeats With Entity Registration"))
            .answerQuestion("Name", "Kendall Roy")
            .swipeToNextQuestionWithRepeatGroup("Cars")
            .clickOnAdd(FormEntryPage("Nested Repeats With Entity Registration"))
            .answerQuestion("Car model", "Honda Accord")
            .swipeToNextQuestionWithRepeatGroup("Cars")
            .clickOnDoNotAdd(AddNewRepeatDialog("People"))
            .clickOnDoNotAdd(FormEndPage("Nested Repeats With Entity Registration"))
            .clickFinalize()

            .startBlankForm("Nested Repeats With Entity Update")
            .assertQuestion("Select person")
            .assertTexts("Roman Roy", "Shiv Roy", "Logan Roy", "Kendall Roy")
            .swipeToNextQuestion("Name")
            .swipeToNextQuestion("Select car")
            .assertTexts("Toyota Corolla", "Honda Civic", "Kia Stonic", "Honda Accord")
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
            .assertTexts("Roman Roy", "Shiv Roy", "Logan Roy")
    }

    @Test
    fun fillingEntityUpdateFormWithNestedRepeats_updatesEntitiesForFollowUpForms() {
        testDependencies.server.addForm(
            "nested-repeats-with-entity-update.xml",
            listOf(EntityListItem("people.csv"), EntityListItem("cars.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("Nested Repeats With Entity Update")
            .assertQuestion("Select person")
            .clickOnText("Roman Roy")
            .swipeToNextQuestion("Name")
            .answerQuestion("Name", "Romulus Roy")
            .swipeToNextQuestion("Select car")
            .clickOnText("Toyota Corolla")
            .swipeToNextQuestion("Car model")
            .answerQuestion("Car model", "Toyota Yaris")
            .swipeToNextQuestionWithRepeatGroup("Cars")
            .clickOnDoNotAdd(AddNewRepeatDialog("People"))
            .clickOnAdd(FormEntryPage("Nested Repeats With Entity Update"))
            .clickOnText("Shiv Roy")
            .swipeToNextQuestion("Name")
            .answerQuestion("Name", "Siobhan Roy")
            .swipeToNextQuestionWithRepeatGroup("Cars")
            .clickOnAdd(FormEntryPage("Nested Repeats With Entity Update"))
            .clickOnText("Honda Civic")
            .swipeToNextQuestion("Car model")
            .answerQuestion("Car model", "Honda Jazz")
            .swipeToNextQuestionWithRepeatGroup("Cars")
            .clickOnDoNotAdd(AddNewRepeatDialog("People"))
            .clickOnDoNotAdd(FormEndPage("Nested Repeats With Entity Update"))
            .clickFinalize()

            .startBlankForm("Nested Repeats With Entity Update")
            .assertQuestion("Select person")
            .assertTexts("Romulus Roy", "Siobhan Roy")
            .assertTextsDoNotExist("Roman Roy", "Shiv Roy")
            .swipeToNextQuestion("Name")
            .swipeToNextQuestion("Select car")
            .assertTexts("Toyota Yaris", "Honda Jazz")
            .assertTextsDoNotExist("Toyota Corolla", "Honda Civic")
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
            .assertTexts("Roman Roy", "Shiv Roy")
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
            .assertTexts("Roman Roy", "Shiv Roy")
    }

    @Test
    fun entityListSecondaryInstancesAreConsistentBetweenFollowUpForms() {
        testDependencies.server.apply {
            addForm(
                "one-question-entity-update.xml",
                listOf(EntityListItem("people.csv", "people.csv", 1))
            )

            addForm(
                "one-question-entity-follow-up.xml",
                listOf(EntityListItem("people.csv", "updated-people.csv", 2))
            )
        }

        rule.withProject(testDependencies.server)
            .clickGetBlankForm()
            .clickClearAll()
            .clickForm("One Question Entity Update")
            .clickGetSelected()
            .clickOK(MainMenuPage())
            .startBlankForm("One Question Entity Update")
            .assertText("Roman Roy")
            .pressBackAndDiscardForm()

            .clickGetBlankForm()
            .clickGetSelected() // Collect automatically only selects the un-downloaded forms
            .clickOK(MainMenuPage())
            .startBlankForm("One Question Entity Update")
            .assertText("Ro-Ro Roy")
            .assertTextDoesNotExist("Roman Roy")
    }

    @Test
    fun entityListFormsAllShowAsUpdatedTogether() {
        testDependencies.server.apply {
            addForm(
                "one-question-entity-update.xml",
                listOf(EntityListItem("people.csv", "people.csv", 1))
            )

            addForm(
                "one-question-entity-follow-up.xml",
                listOf(EntityListItem("people.csv", "people.csv", 1))
            )
        }

        val mainMenuPage = rule.withProject(testDependencies.server.url, matchExactly = true)

        testDependencies.server.apply {
            removeForm("One Question Entity Update")
            removeForm("One Question Entity Follow Up")

            addForm(
                "one-question-entity-update.xml",
                listOf(EntityListItem("people.csv", "people.csv", 2))
            )

            addForm(
                "one-question-entity-follow-up.xml",
                listOf(EntityListItem("people.csv", "people.csv", 2))
            )
        }

        mainMenuPage.clickFillBlankForm()
            .assertTextBesides(equalTo("One Question Entity Update"), containsString("Added on"))
            .assertTextBesides(equalTo("One Question Entity Follow Up"), containsString("Added on"))
            .clickRefresh()
            .assertTextBesides(equalTo("One Question Entity Update"), containsString("Updated on"))
            .assertTextBesides(
                equalTo("One Question Entity Follow Up"),
                containsString("Updated on")
            )
    }

    @Test
    fun fillingEntityRegistrationForm_whenFormUsesOldSpecVersion_doesNotCreateEntityForFollowUpForms() {
        testDependencies.server.addForm("one-question-entity-registration-v2023.1.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertTexts("Roman Roy", "Shiv Roy")
            .assertTextDoesNotExist("Logan Roy")
    }

    @Test
    fun manualEntityFormDownload_withUnsupportedSpecVersion_completesSuccessfully_butThrowsAnErrorAfterOpeningIt() {
        testDependencies.server.addForm("one-question-entity-registration-v2020.1.xml")

        rule.withProject(testDependencies.server)
            .clickGetBlankForm()
            .clickClearAll()
            .clickForm("One Question Entity Registration")
            .clickGetSelected()
            .clickOK(MainMenuPage())
            .startBlankFormWithError("One Question Entity Registration", true)
            .assertTextInDialog(R.string.unrecognized_entity_version, "2020.1.0")
            .clickOKOnDialog(MainMenuPage())
    }

    @Test
    fun automaticEntityFormDownload_withUnsupportedSpecVersion_completesSuccessfully_butThrowsAnErrorAfterOpeningIt() {
        testDependencies.server.addForm("one-question-entity-registration-v2020.1.xml")

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankFormWithError("One Question Entity Registration", true)
            .assertTextInDialog(R.string.unrecognized_entity_version, "2020.1.0")
            .clickOKOnDialog(MainMenuPage())
    }

    @Test
    fun syncEntityFormFromDisc_withUnsupportedSpecVersion_completesSuccessfully_butThrowsAnErrorAfterOpeningIt() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("one-question-entity-registration-v2020.1.xml")
            .startBlankFormWithError("One Question Entity Registration", true)
            .assertTextInDialog(R.string.unrecognized_entity_version, "2020.1.0")
            .clickOKOnDialog(MainMenuPage())
    }

    @Test
    fun closingEntityForm_releasesTheLockAndLetsOtherEntityFormsToBeStarted() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("one-question-entity-registration.xml")
            .startBlankForm("One Question Entity Registration")
            .pressBackAndDiscardForm()
            .startBlankForm("One Question Entity Registration")
    }

    @Test
    fun closingBrokenEntityForm_releasesTheLockAndLetsOtherEntityFormsToBeStarted() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("one-question-entity-registration-broken.xml")
            .copyForm("one-question-entity-registration.xml")
            .startBlankFormWithError("One Question Entity Registration Broken", true)
            .clickOKOnDialog(MainMenuPage())
            .startBlankForm("One Question Entity Registration")
    }

    @Test
    fun aLocallyCreatedEntity_thatIsDeletedOnTheServer_isNotAvailableToFollowUpForms() {
        testDependencies.server.includeIntegrityUrl()
        testDependencies.server.addForm("one-question-entity-registration-id.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .also {
                testDependencies.server.deleteEntity("Logan Roy")
            }

            .clickFillBlankForm()
            .clickRefresh()

            .clickOnForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertTexts("Roman Roy", "Shiv Roy")
            .assertTextDoesNotExist("Logan Roy")
    }

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
            .assertTexts("Roman Roy", "Shiv Roy", "Logan Roy")
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

    @Test
    fun whenListIsApprovalEntityList_localEntitiesCannotBeCreated() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv", true))
        )

        rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))

            .startBlankForm("One Question Entity Update")
            .assertQuestion("Select person")
            .assertTextDoesNotExist("Logan Roy")
    }

    @Test
    fun whenListIsApprovalEntityList_localEntitiesCanBeUpdated() {
        testDependencies.server.addForm("one-question-entity-registration.xml")
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv", true))
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
}
