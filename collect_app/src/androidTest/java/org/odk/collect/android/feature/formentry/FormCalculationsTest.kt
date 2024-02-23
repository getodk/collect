package org.odk.collect.android.feature.formentry

import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertNotSame
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.activities.FormFillingActivity
import org.odk.collect.android.support.ActivityHelpers
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class FormCalculationsTest {
    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun randomFunction_ShouldWorkCorrectly() {
        rule.startAtMainMenu()
            .copyForm("random.xml")
            .copyForm("randomTest_broken.xml")

        val firstQuestionAnswers: MutableList<String> = ArrayList()
        val secondQuestionAnswers: MutableList<String> = ArrayList()
        for (i in 1..3) {
            val formEntryPage = MainMenuPage().startBlankForm("random")
            firstQuestionAnswers.add(getQuestionText())
            formEntryPage.swipeToNextQuestion("Your random once value:")
            secondQuestionAnswers.add(getQuestionText())
            formEntryPage.swipeToEndScreen().clickFinalize()
        }

        assertNotSame(firstQuestionAnswers[0], firstQuestionAnswers[1])
        assertNotSame(firstQuestionAnswers[0], firstQuestionAnswers[2])
        assertNotSame(firstQuestionAnswers[1], firstQuestionAnswers[2])
        assertNotSame(secondQuestionAnswers[0], secondQuestionAnswers[1])
        assertNotSame(secondQuestionAnswers[0], secondQuestionAnswers[2])
        assertNotSame(secondQuestionAnswers[1], secondQuestionAnswers[2])

        firstQuestionAnswers.clear()

        for (i in 1..3) {
            val formEntryPage = MainMenuPage().startBlankForm("random test")
            formEntryPage.inputText("3")
            formEntryPage.swipeToNextQuestion("Your random number was")
            firstQuestionAnswers.add(getQuestionText())
            formEntryPage.swipeToEndScreen().clickFinalize()
        }

        assertNotSame(firstQuestionAnswers[0], firstQuestionAnswers[1])
        assertNotSame(firstQuestionAnswers[0], firstQuestionAnswers[2])
        assertNotSame(firstQuestionAnswers[1], firstQuestionAnswers[2])
    }

    private fun getQuestionText(): String {
        val formFillingActivity = ActivityHelpers.getActivity() as FormFillingActivity
        val questionContainer = formFillingActivity.findViewById<FrameLayout>(R.id.text_container)
        val questionView = questionContainer.getChildAt(0) as TextView
        return questionView.text.toString()
    }
}
