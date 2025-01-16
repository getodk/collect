package org.odk.collect.android.feature.formentry

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.pages.EndOfFormPage
import org.odk.collect.android.support.pages.FormEndPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class AddRepeatTest {
    private val rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = chain().around(rule)

    @Test
    fun whenInRepeat_swipingNext_andClickingAdd_addsAnotherRepeat() {
        rule.startAtMainMenu()
            .copyForm("one-question-repeat.xml")
            .startBlankForm("One Question Repeat")
            .assertText("Person > 1")
            .swipeToNextQuestionWithRepeatGroup("Person")
            .clickOnAdd(FormEntryPage("One Question Repeat"))
            .assertText("Person > 2")
    }

    @Test
    fun whenInRepeat_swipingNext_andClickingDoNotAdd_leavesRepeatGroup() {
        rule.startAtMainMenu()
            .copyForm("one-question-repeat.xml")
            .startBlankForm("One Question Repeat")
            .assertText("Person > 1")
            .swipeToNextQuestionWithRepeatGroup("Person")
            .clickOnDoNotAdd(EndOfFormPage("One Question Repeat"))
    }

    @Test
    fun whenInRepeat_thatIsAFieldList_swipingNext_andClickingAdd_addsAnotherRepeat() {
        rule.startAtMainMenu()
            .copyForm("field-list-repeat.xml")
            .startBlankForm("Field-List Repeat")
            .assertText("Person > 1")
            .assertText("What is their age?")
            .assertText("What is their name?")
            .swipeToNextQuestionWithRepeatGroup("Person")
            .clickOnAdd(FormEntryPage("Field-List Repeat"))
            .assertText("Person > 2")
            .assertText("What is their age?")
            .assertText("What is their name?")
    }

    @Test
    fun whenInRepeat_clickingPlus_andClickingAdd_addsRepeatToEndOfSeries() {
        rule.startAtMainMenu()
            .copyForm("one-question-repeat.xml")
            .startBlankForm("One Question Repeat")
            .assertText("Person > 1")
            .swipeToNextQuestionWithRepeatGroup("Person")
            .clickOnAdd(FormEntryPage("One Question Repeat"))
            .swipeToPreviousQuestion("What is their age?")
            .assertText("Person > 1")
            .clickPlus("Person")
            .clickOnAdd(FormEntryPage("One Question Repeat"))
            .assertText("Person > 3")
    }

    @Test
    fun whenInARepeat_clickingPlus_andClickingDoNotAdd_returns() {
        rule.startAtMainMenu()
            .copyForm("one-question-repeat.xml")
            .startBlankForm("One Question Repeat")
            .assertText("Person > 1")
            .swipeToNextQuestionWithRepeatGroup("Person")
            .clickOnAdd(FormEntryPage("One Question Repeat"))
            .swipeToPreviousQuestion("What is their age?")
            .assertText("Person > 1")
            .clickPlus("Person")
            .clickOnDoNotAdd(FormEntryPage("One Question Repeat"))
            .assertText("Person > 1")
    }

    @Test
    fun whenInRepeatWithFixedCount_noPlusButtonAppears() {
        rule.startAtMainMenu()
            .copyForm("fixed-count-repeat.xml")
            .startBlankForm("Fixed Count Repeat")

        Espresso.onView(ViewMatchers.withId(R.id.menu_add_repeat))
            .check(ViewAssertions.doesNotExist())
    }

    @Test
    fun whenInHierarchyForRepeatGroup_clickingPlus_addsRepeatAtEndOfSeries() {
        rule.startAtMainMenu()
            .copyForm("one-question-repeat.xml")
            .startBlankForm("One Question Repeat")
            .assertText("Person > 1")
            .swipeToNextQuestionWithRepeatGroup("Person")
            .clickOnAdd(FormEntryPage("One Question Repeat"))
            .clickGoToArrow()
            .clickGoUpIcon()
            .addGroup()
            .assertText("Person > 3")
    }

    @Test
    fun whenInRepeatWithoutLabel_swipingNext_displaysTheAddRepeatDialog() {
        rule.startAtMainMenu()
            .copyForm("repeat_without_label.xml")
            .startBlankForm("Repeat without label") // group with no label
            .swipeToNextQuestionWithRepeatGroup("")
            .clickOnDoNotAdd(FormEntryPage("Repeat without label")) // group with blank label
            .swipeToNextQuestionWithRepeatGroup("")
            .clickOnDoNotAdd(FormEntryPage("Repeat without label"))
    }

    @Test
    fun whenViewFormInHierarchyForRepeatGroup_noAddButtonAppears() {
        rule.startAtMainMenu()
            .copyForm("one-question-repeat.xml")
            .startBlankForm("One Question Repeat")
            .swipeToNextQuestionWithRepeatGroup("Person")
            .clickOnDoNotAdd(FormEndPage("One Question Repeat"))
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickOnForm("One Question Repeat")
            .clickOnGroup("Person")
            .assertNoId(R.id.menu_add_repeat)
    }
}
