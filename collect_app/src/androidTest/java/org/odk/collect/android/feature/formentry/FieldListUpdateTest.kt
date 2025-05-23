package org.odk.collect.android.feature.formentry

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.FormEntryActivityTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.androidtest.RecordedIntentsRule
import java.io.File
import java.io.FileOutputStream
import java.util.Random

class FieldListUpdateTest {
    private var rule: FormEntryActivityTestRule = FormEntryActivityTestRule()

    @get:Rule
    var copyFormChain: RuleChain = chain()
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun relevanceChangeAtEnd_ShouldToggleLastWidgetVisibility() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .assertNoQuestion("Target1")
            .answerQuestion("Source1", "A")
            .assertQuestion("Target1")
            .assertTextBelow("Target1", "Source1")
            .longPressOnQuestion("Source1")
            .removeResponse()
            .assertNoQuestion("Target1")
    }

    @Test
    fun relevanceChangeAtBeginning_ShouldToggleFirstWidgetVisibility() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Single relevance at beginning")
            .clickOnQuestion("Source2")
            .assertNoQuestion("Target2")
            .answerQuestion("Source2", "A")
            .assertQuestion("Target2")
            .assertTextBelow("Source2", "Target2")
            .longPressOnQuestion("Source2")
            .removeResponse()
            .assertNoQuestion("Target2")
    }

    @Test
    fun relevanceChangeInMiddle_ShouldToggleMiddleWidgetVisibility() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Single relevance in middle")
            .clickOnQuestion("Source3")
            .assertNoQuestion("Target3")
            .answerQuestion("Source3", "A")
            .assertQuestion("Target3")
            .assertTextBelow("Filler3", "Source3")
            .assertTextBelow("Filler3", "Target3")
            .longPressOnQuestion("Source3")
            .removeResponse()
            .assertNoQuestion("Target3")
    }

    @Test
    fun changeInValueUsedInLabel_ShouldChangeLabelText() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Label change")
            .clickOnQuestion("Hello, , how are you today?")
            .assertQuestion("Hello, , how are you today?")
            .answerQuestion("What is your name?", "Adam")
            .assertQuestion("Hello, Adam, how are you today?")
            .longPressOnQuestion("What is your name?")
            .removeResponse()
            .assertQuestion("Hello, , how are you today?")
    }

    @Test
    fun changeInValueUsedInHint_ShouldChangeHintText() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Hint change")
            .clickOnQuestion("What is your name?")
            .assertText("Please don't use your calculator, !")
            .answerQuestion("What is your name?", "Adam")
            .assertText("Please don't use your calculator, Adam!")
            .longPressOnQuestion("What is your name?")
            .removeResponse()
            .assertText("Please don't use your calculator, !")
    }

    @Test
    fun changeInValueUsedInOtherField_ShouldChangeValue() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Value change")
            .clickOnQuestion("What is your name?")
            .assertAnswer("Name length", "0")
            .assertAnswer("First name letter", "")
            .answerQuestion("What is your name?", "Adam")
            .assertAnswer("Name length", "4")
            .assertAnswer("First name letter", "A")
            .longPressOnQuestion("What is your name?")
            .removeResponse()
            .assertAnswer("Name length", "0")
            .assertAnswer("First name letter", "")
    }

    @Test
    fun selectionChangeAtFirstCascadeLevel_ShouldUpdateNextLevels() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Cascading select")
            .clickOnQuestion("Level1") // No choices should be shown for levels 2 and 3 when no selection is made for level 1
            .assertTextsDoNotExist("A1", "B1", "C1", "A1A") // Selecting C for level 1 should only reveal options for C at level 2
            .clickOnText("C")
            .assertTextsDoNotExist("A1", "B1", "A1A")
            .assertText("C1") // Selecting A for level 1 should reveal options for A at level 2
            .clickOnText("A")
            .assertTextsDoNotExist("A1A", "B1", "C1")
            .assertText("A1") // Selecting A1 for level 2 should reveal options for A1 at level 3
            .clickOnText("A1")
            .assertText("A1A")
            .assertTextsDoNotExist("B1", "C1")
            .longPressOnQuestion("Level1")
            .removeResponse()
            .assertTextsDoNotExist("A1", "B1", "C1", "A1A")
    }

    @Test
    fun clearingParentSelect_ShouldUpdateAllDependentLevels() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Cascading select")
            .clickOnQuestion("Level1")
            .clickOnText("A")
            .clickOnText("A1")
            .clickOnText("A1B")
            .longPressOnQuestion("Level1")
            .removeResponse()
            .assertTextsDoNotExist("A1", "A1B")
    }

    @Test
    fun selectionChangeAtOneCascadeLevelWithMinimalAppearance_ShouldUpdateNextLevels() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Cascading select minimal")
            .clickOnQuestion("Level1")
            .assertTextsDoNotExist("A1", "B1", "C1", "A1A") // No choices should be shown for levels 2 and 3 when no selection is made for level 1
            .openSelectMinimalDialog(0)
            .selectItem("C") // Selecting C for level 1 should only reveal options for C at level 2
            .assertTextsDoNotExist("A1", "B1")
            .openSelectMinimalDialog(1)
            .selectItem("C1")
            .assertTextDoesNotExist("A1A")
            .clickOnText("C")
            .clickOnText("A") // Selecting A for level 1 should reveal options for A at level 2
            .openSelectMinimalDialog(1)
            .assertText("A1")
            .assertTextsDoNotExist("A1A", "B1", "C1")
            .selectItem("A1") // Selecting A1 for level 2 should reveal options for A1 at level 3
            .openSelectMinimalDialog(2)
            .assertText("A1A")
            .assertTextsDoNotExist("B1A", "B1", "C1")
    }

    @Test
    fun questionsAppearingBeforeCurrentTextQuestion_ShouldNotChangeFocus() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Push off screen")
            .clickOnQuestion("Source9")
            .assertNoQuestion("Target9-15")
            .answerQuestion("Source9", "A")
            .assertQuestion("Target9-15")
            .assertQuestionHasFocus("Source9")
    }

    @Test
    fun questionsAppearingBeforeCurrentBinaryQuestion_ShouldNotChangeFocus() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .apply {
                // FormFillingActivity expects an image at a fixed path so copy the app logo there
                val icon = BitmapFactory.decodeResource(
                    ApplicationProvider.getApplicationContext<Context>().resources,
                    R.drawable.notes
                )
                val tmpJpg = File(StoragePathProvider().getTmpImageFilePath())
                tmpJpg.createNewFile()
                val fos = FileOutputStream(tmpJpg)
                icon.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                Intents.intending(Matchers.not(IntentMatchers.isInternal())).respondWith(
                    Instrumentation.ActivityResult(
                        Activity.RESULT_OK, null
                    )
                )
            }
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Push off screen binary")
            .clickOnQuestion("Source10")
            .assertNoQuestion("Target10-15")
            .clickOnString(org.odk.collect.strings.R.string.capture_image)
            .assertQuestion("Target10-15")
            .assertText(org.odk.collect.strings.R.string.capture_image)
    }

    @Test
    fun changeInValueUsedInGuidanceHint_ShouldChangeGuidanceHintText() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickOptionsIcon()
            .clickProjectSettings()
            .clickFormManagement()
            .openShowGuidanceForQuestions()
            .clickOnString(org.odk.collect.strings.R.string.guidance_yes)
            .pressBack(ProjectSettingsPage())
            .pressBack(FormEntryPage("fieldlist-updates"))
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Guidance hint")
            .clickOnQuestion("Source11")
            .assertTextDoesNotExist("10")
            .answerQuestion("Source11", "5")
            .assertQuestion("10")
            .longPressOnQuestion("Source11")
            .removeResponse()
            .assertTextDoesNotExist("10")
    }

    @Test
    fun selectingADateForDateTime_ShouldChangeRelevanceOfRelatedField() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Date time")
            .clickOnQuestion("Source12")
            .assertNoQuestion("Target12")
            .clickOnString(org.odk.collect.strings.R.string.select_date)
            .clickOKOnDialog()
            .assertQuestion("Target12")
            .longPressOnQuestion("Source12")
            .removeResponse()
            .assertNoQuestion("Target12")
    }

    @Test
    fun selectingARating_ShouldChangeRelevanceOfRelatedField() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Rating")
            .clickOnQuestion("Source13")
            .assertNoQuestion("Target13")
            .setRating(3.0f)
            .assertQuestion("Target13")
            .longPressOnQuestion("Source13")
            .removeResponse()
            .assertNoQuestion("Target13")
    }

    @Test
    fun manuallySelectingAValueForMissingExternalApp_ShouldTriggerUpdate() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("External app")
            .clickOnQuestion("Source14")
            .clickOnText("Launch")
            .assertNoQuestion("Target14")
            .answerQuestion("Source14", Random().nextInt().toString())
            .assertQuestion("Target14")
            .longPressOnQuestion("Source14")
            .removeResponse()
            .assertNoQuestion("Target14")
    }

    @Test
    fun searchMinimalInFieldList() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Search in field-list")
            .clickOnQuestion("Source15")
            .assertSelectMinimalDialogAnswer(null)
            .assertNoQuestion("Target15")
            .openSelectMinimalDialog()
            .assertTexts("Mango", "Oranges", "Strawberries")
            .selectItem("Strawberries")
            .assertQuestion("Target15")
            .assertSelectMinimalDialogAnswer("Strawberries")
            .longPressOnQuestion("Source15")
            .removeResponse()
            .assertSelectMinimalDialogAnswer(null)
            .assertNoQuestion("Target15")
    }

    @Test
    fun listOfQuestionsShouldNotBeScrolledToTheLastEditedQuestionAfterClickingOnAQuestion() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Long list of questions")
            .clickOnQuestion("Question1")
            .answerQuestion(0, "X")
            .clickOnQuestionField("Question20")
            .assertQuestion("Question20")
    }

    @Test
    fun recordingAudio_ShouldChangeRelevanceOfRelatedField() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml")
            .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnGroup("Audio")
            .clickOnQuestion("Source16")
            .assertNoQuestion("Target16")
            .clickOnString(org.odk.collect.strings.R.string.capture_audio)
            .clickOnContentDescription(org.odk.collect.strings.R.string.stop_recording)
            .assertQuestion("Target16")
            .clickOnString(org.odk.collect.strings.R.string.delete_answer_file)
            .clickOnTextInDialog(
                org.odk.collect.strings.R.string.delete_answer_file,
                FormEntryPage("fieldlist-updates")
            )
            .assertNoQuestion("Target16")
    }

    @Test
    fun changeInValueUsedToDetermineIfAQuestionIsRequired_ShouldUpdateTheRelatedRequiredQuestion() {
        rule.setUpProjectAndCopyForm("dynamic_required_question.xml")
            .fillNewForm("dynamic_required_question.xml", "dynamic_required_question")
            .assertQuestion("Target", false)
            .answerQuestion("Source", "blah")
            .assertQuestion("Target", true)
            .swipeToNextQuestionWithConstraintViolation(org.odk.collect.strings.R.string.required_answer_error)
            .longPressOnQuestion("Source")
            .removeResponse()
            .assertQuestion("Target", false)
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.required_answer_error)
    }
}
