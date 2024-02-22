package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.AddNewRepeatDialog
import org.odk.collect.android.support.pages.FormEndPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class FillBlankFormWithRepeatGroupTest {
    private var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = chain().around(rule)

    @Test
    fun whenNoRepeatGroupAdded_ShouldNotDoubleLastQuestion() {
        rule.startAtMainMenu()
            .copyForm("TestRepeat.xml")
            .startBlankForm("TestRepeat")
            .clickOptionsIcon()
            .clickGeneralSettings()
            .clickOnUserInterface()
            .clickNavigation()
            .clickUseSwipesAndButtons()
            .pressBack(ProjectSettingsPage())
            .pressBack(FormEntryPage("TestRepeat"))
            .swipeToNextQuestionWithRepeatGroup("Repeat # 1")
            .clickOnDoNotAdd(AddNewRepeatDialog("Repeat # 2"))
            .clickOnDoNotAdd(FormEntryPage("TestRepeat"))
            .clickForwardButtonToEndScreen()
            .clickFinalize()
    }

    @Test
    fun dynamicGroupLabel_should_beCalculatedProperly() {
        rule.startAtMainMenu()
            .copyForm("RepeatTitles_1648.xml")
            .startBlankForm("Repeat titles 1648")
            .inputText("test")
            .closeSoftKeyboard()
            .swipeToNextQuestion("Name")
            .inputText("FirstPerson")
            .closeSoftKeyboard()
            .swipeToNextQuestion("Age")
            .inputText("25")
            .closeSoftKeyboard()
            .swipeToNextQuestion("Photo")
            .assertText("gr1 > 1 > Person: 25")
            .clickGoToArrow()
            .assertText("gr1 > 1 > Person: 25")
            .clickOnQuestion("Photo")
            .swipeToNextQuestionWithRepeatGroup("gr1")
            .clickOnDoNotAdd(FormEntryPage("Repeat titles 1648"))
            .inputText("SecondPart")
            .closeSoftKeyboard()
            .swipeToNextQuestion("Date")
            .assertText("Part1 > 1 > Xxx: SecondPart")
            .clickGoToArrow()
            .assertText("Part1 > 1 > Xxx: SecondPart")
            .clickOnQuestion("Date")
            .swipeToNextQuestion("Multi Select")
            .swipeToNextQuestionWithRepeatGroup("Part1")
            .clickOnDoNotAdd(FormEndPage("Repeat titles 1648"))
            .clickFinalize()
    }

    @Test
    fun nestedGroupsWithFieldListAppearance_ShouldBeAbleToFillTheForm() {
        rule.startAtMainMenu()
            .copyForm("form1.xml")
            .startBlankForm("form1")
            .swipeToEndScreen()
            .clickFinalize()

        rule.startAtMainMenu()
            .copyForm("form2.xml")
            .startBlankForm("form2")
            .closeSoftKeyboard()
            .swipeToEndScreen()
            .clickFinalize()

        rule.startAtMainMenu()
            .copyForm("form3.xml")
            .startBlankForm("form3")
            .closeSoftKeyboard()
            .swipeToEndScreen()
            .clickFinalize()

        rule.startAtMainMenu()
            .copyForm("form4.xml")
            .startBlankForm("form4")
            .inputText("T1")
            .closeSoftKeyboard()
            .swipeToNextQuestion("T2")
            .inputText("T2")
            .closeSoftKeyboard()
            .swipeToNextQuestion("T3")
            .inputText("T3")
            .closeSoftKeyboard()
            .swipeToEndScreen()
            .clickFinalize()

        rule.startAtMainMenu()
            .copyForm("form5.xml")
            .startBlankForm("form5")
            .inputText("T1")
            .closeSoftKeyboard()
            .swipeToNextQuestion("T2")
            .inputText("T2")
            .closeSoftKeyboard()
            .swipeToNextQuestion("T3")
            .inputText("T3")
            .closeSoftKeyboard()
            .swipeToEndScreen()
            .clickFinalize()

        rule.startAtMainMenu()
            .copyForm("form6.xml")
            .startBlankForm("form6")
            .inputText("T1")
            .closeSoftKeyboard()
            .swipeToNextQuestion("T2")
            .inputText("T2")
            .closeSoftKeyboard()
            .swipeToNextQuestion("T3")
            .inputText("T3")
            .closeSoftKeyboard()
            .swipeToEndScreen()
            .clickFinalize()

        rule.startAtMainMenu()
            .copyForm("form7.xml")
            .startBlankForm("form7")
            .swipeToEndScreen()
            .clickFinalize()

        rule.startAtMainMenu()
            .copyForm("form8.xml")
            .startBlankForm("form8")
            .closeSoftKeyboard()
            .swipeToEndScreen()
            .clickFinalize()

        rule.startAtMainMenu()
            .copyForm("form9.xml")
            .startBlankForm("form9")
            .closeSoftKeyboard()
            .swipeToEndScreen()
            .clickFinalize()
    }

    @Test
    fun whenNoRepeatGroupAdded_ShouldBackwardButtonBeClickable() {
        rule.startAtMainMenu()
            .copyForm("RepeatGroupAndGroup.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .clickNavigation()
            .clickUseSwipesAndButtons()
            .pressBack(ProjectSettingsPage())
            .pressBack(rule.startAtMainMenu())
            .startBlankFormWithRepeatGroup("RepeatGroupAndGroup", "G1")
            .clickOnDoNotAdd(FormEntryPage("RepeatGroupAndGroup"))
            .closeSoftKeyboard()
            .swipeToPreviousQuestionWithRepeatGroup("G1")
            .clickOnDoNotAdd(FormEntryPage("RepeatGroupAndGroup"))
            .closeSoftKeyboard()
            .swipeToEndScreen()
            .clickFinalize()
    }

    @Test
    fun when_pageBehindRepeatGroupWithRegularGroupInsideIsVisible_should_swipeBackWork() {
        rule.startAtMainMenu()
            .copyForm("repeat_group_new.xml")
            .startBlankFormWithRepeatGroup("RepeatGroupNew", "People")
            .clickOnAdd(FormEntryPage("RepeatGroupNew"))
            .inputText("A")
            .closeSoftKeyboard()
            .swipeToNextQuestion("Age")
            .inputText("1")
            .closeSoftKeyboard()
            .swipeToNextQuestionWithRepeatGroup("People")
            .clickOnAdd(FormEntryPage("RepeatGroupNew"))
            .assertQuestion("Name")
            .inputText("B")
            .closeSoftKeyboard()
            .swipeToNextQuestion("Age")
            .inputText("2")
            .closeSoftKeyboard()
            .swipeToNextQuestionWithRepeatGroup("People")
            .clickOnAdd(FormEntryPage("RepeatGroupNew"))
            .assertQuestion("Name")
            .inputText("C")
            .closeSoftKeyboard()
            .swipeToNextQuestion("Age")
            .inputText("3")
            .closeSoftKeyboard()
            .swipeToNextQuestionWithRepeatGroup("People")
            .clickOnDoNotAdd(FormEndPage("RepeatGroupNew"))
            .swipeToPreviousQuestion("Age")
            .assertText("3")
            .swipeToNextQuestionWithRepeatGroup("People")
            .clickOnDoNotAdd(FormEndPage("RepeatGroupNew"))
            .clickFinalize()
    }

    @Test
    fun when_navigateOnHierarchyView_should_breadcrumbPathBeVisible() {
        rule.startAtMainMenu()
            .copyForm("repeat_group_new.xml")
            .startBlankFormWithRepeatGroup("RepeatGroupNew", "People")
            .clickOnAdd(FormEntryPage("RepeatGroupNew"))
            .inputText("A")
            .closeSoftKeyboard()
            .swipeToNextQuestion("Age")
            .inputText("1")
            .closeSoftKeyboard()
            .swipeToNextQuestionWithRepeatGroup("People")
            .clickOnAdd(FormEntryPage("RepeatGroupNew"))
            .inputText("B")
            .closeSoftKeyboard()
            .swipeToNextQuestion("Age")
            .inputText("2")
            .closeSoftKeyboard()
            .swipeToNextQuestionWithRepeatGroup("People")
            .clickOnAdd(FormEntryPage("RepeatGroupNew"))
            .inputText("C")
            .closeSoftKeyboard()
            .swipeToNextQuestion("Age")
            .inputText("3")
            .clickGoToArrow()
            .assertText("People > 3 > Person: C")
            .clickGoUpIcon()
            .assertText("3.\u200E Person: C")
            .clickJumpEndButton()
            .clickFinalize()
    }

    @Test
    fun openHierarchyPageFromLastView_ShouldNotDisplayError() {
        rule.startAtMainMenu()
            .copyForm("repeat_group_form.xml")
            .startBlankFormWithRepeatGroup("Repeat Group", "Grp1")
            .clickOnAdd(FormEntryPage("Repeat Group"))
            .swipeToNextQuestionWithRepeatGroup("Grp1")
            .clickOnDoNotAdd(FormEntryPage("Repeat Group"))
            .swipeToNextQuestionWithRepeatGroup("Grp2")
            .clickOnDoNotAdd(FormEndPage("Repeat Group"))
            .clickGoToArrow()
            .clickJumpEndButton()
            .clickFinalize()
    }
}
