package org.odk.collect.android.feature.experimental.timedgrid

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.assertConsecutiveMistakesDialogAndContinue
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.assertEarlyFinishDialogAndConfirm
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.assertLastAttemptedItemDialogAndConfirm
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.assertTestEndedEarlyDialogAndConfirm
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.clickFinishTestButton
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.clickForwardButtonWithError
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.clickGoToArrowWithError
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.clickPauseTestButton
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.clickProjectSettingsWithError
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.clickStartTestButton
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.clickUntilEarlyFinish
import org.odk.collect.android.feature.experimental.timedgrid.TimedGridHelpers.selectTestAnswers
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.timedgrid.TimerProvider
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class TimedGridTest {

    private val rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain()
        .around(rule)

    private val timer = FakeTimer()

    @Before
    fun setup() {
        TimerProvider.factory = { timer }
    }

    // --- Default Letters ---
    @Test
    fun testLetters_oneWrongAnswer_summaryCorrect() {
        val tapped = listOf("E")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters Test")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Type X (Letters alias) ---
    @Test
    fun testTypeX_oneWrongAnswer_summaryCorrect() {
        val tapped = listOf("a")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Type X Letters Test")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Words ---
    @Test
    fun testWords_threeWrongAnswers_summaryCorrect() {
        val tapped = listOf("tob", "lig", "pum")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allWords, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Words Test")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", allWords.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allWords.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Reading ---
    @Test
    fun testReading_threeWrongAnswers_summaryCorrect() {
        val tapped = listOf("Lorem", "ipsum", "dolor")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allReading, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Reading Test")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", allReading.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allReading.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "20")
    }

    // --- Numbers ---
    @Test
    fun testNumbers_threeWrongAnswers_summaryCorrect() {
        val tapped = listOf("1", "2", "3")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allNumbers, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Numbers Test")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", allNumbers.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allNumbers.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "false")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Arithmetic ---
    @Test
    fun testArithmetic_twoWrongAnswers_summaryCorrect() {
        val tapped = listOf("1 + 2 = 3", "5 + 5 = 10")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allArithmetic, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Arithmetic Test")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", allArithmetic.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allArithmetic.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "True")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters Finish=2 ---
    @Test
    fun testLettersFinish2_oneWrongAnswer_summaryCorrect() {
        val tapped = listOf("E")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters Finish 2")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .assertEarlyFinishDialogAndConfirm()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters Finish=1 ---
    @Test
    fun testLettersFinish1_twoWrongAnswers_lastAttemptedItem_summaryCorrect() {
        val tapped = listOf("a", "w")
        val lastAttemptedItem = "I"
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet(), lastAttemptedItem)
        val expectedNotAttempted = TimedGridHelpers.notAttemptedItems(allLetters, lastAttemptedItem)
        val totalAttempted = allLetters.size - expectedNotAttempted.size
        val correctItems = totalAttempted - tapped.size

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters Finish 1")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .assertEarlyFinishDialogAndConfirm()
            .assertLastAttemptedItemDialogAndConfirm(lastAttemptedItem)
            .clickFinishTestButton()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", totalAttempted.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", correctItems.toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", expectedNotAttempted.joinToString(", "))
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters No Pause ---
    @Test
    fun testLettersNoPause_oneWrongAnswer_summaryCorrect() {
        val tapped = listOf("a")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters No Pause")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .also {
                // Capture time before
                val timeBeforeText = TimedGridHelpers.getTextFromView(org.odk.collect.timedgrid.R.id.button_timer)
                val timeBefore = TimedGridHelpers.extractTimeLeft(timeBeforeText)

                it.clickOnId(org.odk.collect.timedgrid.R.id.button_timer)
                timer.wait(2)

                // Capture time after
                val timeAfterText = TimedGridHelpers.getTextFromView(org.odk.collect.timedgrid.R.id.button_timer)
                val timeAfter = TimedGridHelpers.extractTimeLeft(timeAfterText)

                // Assert that the time has decreased
                assert(timeAfter < timeBefore) {
                    "Timer did not continue after clicking Timer button. Before=$timeBefore After=$timeAfter"
                }
            }
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "53") // 60 - 5 - 2
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters Strict True + Duration=10 ---
    @Test
    fun testLettersStrictTrueDuration10_oneWrongAnswer_lastAttemptedItem_summaryCorrect() {
        val tapped = listOf("a")
        val lastAttemptedItem = "w"
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet(), lastAttemptedItem)
        val expectedNotAttempted = TimedGridHelpers.notAttemptedItems(allLetters, lastAttemptedItem)
        val totalAttempted = allLetters.size - expectedNotAttempted.size
        val correctItems = totalAttempted - tapped.size

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters Strict True Duration 10")
            .clickStartTestButton()
            .also { timer.wait(2) }
            .selectTestAnswers(tapped)
            .also { timer.wait(10) }
            .assertLastAttemptedItemDialogAndConfirm(lastAttemptedItem)
            .clickFinishTestButton()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "0")
            .assertAnswer("Total number of items attempted", totalAttempted.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", correctItems.toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", expectedNotAttempted.joinToString(", "))
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters Strict False + Duration=10 ---
    @Test
    fun testLettersStrictFalseDuration10_twoWrongAnswers_summaryCorrect() {
        val tapped = listOf("a", "w")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters Strict False Duration 10")
            .clickStartTestButton()
            .also { timer.wait(2) }
            .selectTestAnswers(tapped)
            .also { timer.wait(10) }
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "0")
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters Strict True + EndAfter=2 ---
    @Test
    fun testLettersStrictTrueEndAfter2_twoWrongAnswers_summaryCorrect() {
        val tapped = listOf("L", "i")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters Strict True End After 2")
            .clickStartTestButton()
            .selectTestAnswers(tapped)
            .assertTestEndedEarlyDialogAndConfirm(2)
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", ((timer.getMillisRemaining() / 1000.0).roundToInt()).toString())
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters Strict False + EndAfter=2 ---
    @Test
    fun testLettersStrictFalseEndAfter2_twoWrongAnswers_summaryCorrect() {
        val tapped = listOf("L", "i")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters Strict False End After 2")
            .clickStartTestButton()
            .selectTestAnswers(tapped)
            .assertConsecutiveMistakesDialogAndContinue(2)
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", ((timer.getMillisRemaining() / 1000.0).roundToInt()).toString())
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters PageRows=2 ---
    @Test
    fun testLettersPageRows2_twoWrongAnswers_summaryCorrect() {
        val tapped = listOf("L", "i")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters Page Rows 2")
            .clickStartTestButton()
            .also { TimedGridHelpers.assertVisibleRows(2) }
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters Grid + Summary ---
    @Test
    fun testLettersGridAndSummary_twoWrongAnswers_summaryCorrect() {
        val tapped = listOf("L", "i")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnGroup("Letters Grid And Summary:")
            .clickOnQuestion("Letters Grid And Summary")
            // Assert summary fields are visible immediately
            .assertTexts("Amount of time remaining in seconds", "Total number of items attempted", "Number of incorrect items",
                "Number of correct items", "Whether the firstline was all incorrect", "The list of correct items",
                "The list of items not attempted/answered", "The total number of punctuation marks")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters Grid + Summary (Relevant / Group 2) ---
    @Test
    fun testLettersGridAndSummary2_twoWrongAnswers_summaryCorrect() {
        val tapped = listOf("L", "i")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnGroup("Letters Grid And Summary + Relevant:")
            .clickOnQuestion("Letters Grid And Summary Relevant")
            // Assert summary fields are not visible yet
            .assertTextsDoNotExist("Amount of time remaining in seconds", "Total number of items attempted", "Number of incorrect items",
                "Number of correct items", "Whether the firstline was all incorrect", "The list of correct items",
                "The list of items not attempted/answered", "The total number of punctuation marks")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .assertAnswer("The list of correct items", expectedCorrect)
            .assertAnswer("The list of items not attempted/answered", "")
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Letters Grid No Group ---
    @Test
    fun testLettersNoGroup_twoWrongAnswers_summaryCorrect() {
        val tapped = listOf("L", "i")
        val expectedCorrect = TimedGridHelpers.expectedCorrectItems(allLetters, tapped.toSet())

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters No Group")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .clickUntilEarlyFinish()
            .clickForwardButton()
            .assertAnswer("Amount of time remaining in seconds", "55")
            .clickForwardButton()
            .assertAnswer("Total number of items attempted", allLetters.size.toString())
            .clickForwardButton()
            .assertAnswer("Number of incorrect items", tapped.size.toString())
            .clickForwardButton()
            .assertAnswer("Number of correct items", (allLetters.size - tapped.size).toString())
            .clickForwardButton()
            .assertAnswer("Whether the firstline was all incorrect", "False")
            .clickForwardButton()
            .assertAnswer("The number of sentence end marks (e.g. periods) passed, as indicated by the last attempted item when using the oral reading test type", "0")
            .clickForwardButton()
            .assertAnswer("The list of correct items", expectedCorrect)
            .clickForwardButton()
            .assertAnswer("The list of items not attempted/answered", "")
            .clickForwardButton()
            .assertAnswer("The total number of punctuation marks", "0")
    }

    // --- Check If Timer Works When Rotating The Device ---
    @Test
    fun checkTimerWhenRotating_timerContinuesRunning() {
        val tapped = listOf("a", "w")

        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters Test")
            .clickStartTestButton()
            .also { timer.wait(5) }
            .selectTestAnswers(tapped)
            .also {
                // Capture timer before rotation
                val timeBeforeText = TimedGridHelpers.getTextFromView(org.odk.collect.timedgrid.R.id.button_timer)
                val timeBefore = TimedGridHelpers.extractTimeLeft(timeBeforeText)

                // Rotate device
                it.rotateToLandscape(it)
                timer.wait(5)

                // Capture timer after rotation
                val timeAfterText = TimedGridHelpers.getTextFromView(org.odk.collect.timedgrid.R.id.button_timer)
                val timeAfter = TimedGridHelpers.extractTimeLeft(timeAfterText)

                // Assert timer is still running
                assert(timeAfter < timeBefore) {
                    "Timer did not continue after rotation. Before=$timeBefore After=$timeAfter"
                }
            }
    }

    @Test
    fun blockNavigationWhileTestRunning() {
        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Letters Test")
            .clickStartTestButton()
            .clickForwardButtonWithError()
            .clickGoToArrowWithError()
            .clickOptionsIcon()
            .clickProjectSettingsWithError()
            .clickPauseTestButton()
            .clickForwardButtonWithError()
            .clickGoToArrowWithError()
            .clickOptionsIcon()
            .clickProjectSettingsWithError()
    }

    @Test
    fun testTranslationsAreApplied() {
        rule.startAtMainMenu()
            .copyForm("timed-grid-form.xml")
            .startBlankForm("timed-grid-form")
            .clickGoToArrow()
            .clickOnQuestion("Words Test")
            // Assert original (English) labels
            .assertTexts(*allWords.take(3).toTypedArray())
            // Change language
            .clickOptionsIcon()
            .clickOnString(org.odk.collect.strings.R.string.change_language)
            .clickOnText("Polish (pl)")
            // Assert translated labels
            .assertTexts(*allWordsPl.take(3).toTypedArray())
    }

    companion object {
        private val allLetters = listOf(
            "L", "i", "h", "R", "S", "y", "E", "O", "n", "T",
            "i", "e", "T", "D", "A", "t", "a", "d", "e", "w",
            "h", "O", "e", "m", "U", "r", "L", "G", "R", "u",
            "G", "R", "B", "E", "i", "f", "m", "t", "s", "r",
            "S", "T", "C", "N", "p", "A", "F", "c", "G", "E",
            "y", "Q", "A", "M", "C", "O", "t", "n", "P", "s",
            "e", "A", "b", "s", "O", "F", "h", "u", "A", "t",
            "R", "z", "H", "e", "S", "i", "g", "m", "i", "L",
            "o", "I", "N", "O", "e", "L", "E", "r", "p", "X",
            "H", "A", "c", "D", "d", "t", "O", "j", "e", "n"
        )

        private val allWords = listOf(
            "tob", "lig", "pum", "inbok", "maton",
            "gatch", "tup", "noom", "sen", "timming",
            "beeth", "mun", "ellus", "fot", "widge",
            "han", "pite", "dazz", "unstade", "rike",
            "fipper", "chack", "gub", "weem", "foin",
            "ithan", "feth", "tade", "anth", "bom",
            "ruck", "rax", "jad", "foob", "bapent",
            "sull", "lotch", "snim", "queet", "reb",
            "lunkest", "vown", "coll", "kittle", "moy",
            "div", "trinless", "pran", "nauk", "otta"
        )

        private val allWordsPl = listOf(
            "tob (pl)", "lig (pl)", "pum (pl)", "inbok (pl)", "maton (pl)",
            "gatch (pl)", "tup (pl)", "noom (pl)", "sen (pl)", "timming (pl)",
            "beeth (pl)", "mun (pl)", "ellus (pl)", "fot (pl)", "widge (pl)",
            "han (pl)", "pite (pl)", "dazz (pl)", "unstade (pl)", "rike (pl)",
            "fipper (pl)", "chack (pl)", "gub (pl)", "weem (pl)", "foin (pl)",
            "ithan (pl)", "feth (pl)", "tade (pl)", "anth (pl)", "bom (pl)",
            "ruck (pl)", "rax (pl)", "jad (pl)", "foob (pl)", "bapent (pl)",
            "sull (pl)", "lotch (pl)", "snim (pl)", "queet (pl)", "reb (pl)",
            "lunkest (pl)", "vown (pl)", "coll (pl)", "kittle (pl)", "moy (pl)",
            "div (pl)", "trinless (pl)", "pran (pl)", "nauk (pl)", "otta (pl)"
        )

        private val allReading = listOf(
            "Lorem", "ipsum", "dolor", "sit", "amet", ",",
            "consectetur", "adipiscing", "elit", ".",
            "Phasellus", "vel", "tortor", "neque", ".",
            "Nulla", "vestibulum", "dictum", "nibh", ",",
            "eu", "vehicula", "felis", ".", "Suspendisse",
            "condimentum", "turpis", "ac", "viverra", "fermentum",
            ".", "Ut", "tincidunt", "metus", "a", "ante",
            "rhoncus", "suscipit", ".", "Ut", "sed", "lacus",
            "egestas", ",", "aliquam", "urna", "eu", "sollicitudin",
            "risus", ".", "Vestibulum", "imperdiet", "bibendum",
            "imperdiet", ".", "Quisque", "vitae", "felis", "tellus",
            ".", "Vivamus", "sit", "amet", "consectetur", "diam",
            ",", "eget", "auctor", "ligula", ".", "Phasellus",
            "vestibulum", ",", "ante", "id", "pharetra", "iaculis",
            ",", "mi", "nibh", "tristique", "urna", ",", "in",
            "lacinia", "arcu", "risus", "eu", "urna.", ".",
            "Aenean", "sollicitudin", "elementum", "erat", "vel",
            "feugiat", ".", "Nullam", "venenatis", "mattis", "metus",
            ",", "vel", "fringilla", "nunc", "pulvinar", "a", "."
        )

        private val allNumbers = (1..50).map { it.toString() }

        private val allArithmetic = listOf(
            "1 + 2 = 3", "5 + 5 = 10", "7 + 1 = 8", "4 + 3 = 7", "6 + 0 = 6",
            "5 - 2 = 3", "8 - 4 = 4", "10 - 1 = 9", "9 - 6 = 3", "7 - 7 = 0",
            "2 x 3 = 6", "4 x 2 = 8", "5 x 4 = 20", "3 x 3 = 9", "1 x 8 = 8",
            "10 ÷ 2 = 5", "9 ÷ 3 = 3", "8 ÷ 4 = 2", "6 ÷ 1 = 6", "12 ÷ 6 = 2"
        )
    }
}
