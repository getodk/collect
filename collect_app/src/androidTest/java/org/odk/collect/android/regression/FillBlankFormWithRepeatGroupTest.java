package org.odk.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.pages.AddNewRepeatDialog;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.support.pages.FormEndPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.ProjectSettingsPage;

//Issue NODK-247
@RunWith(AndroidJUnit4.class)
public class FillBlankFormWithRepeatGroupTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void whenNoRepeatGroupAdded_ShouldNotDoubleLastQuestion() {

        //TestCase1
        rule.startAtMainMenu()
                .copyForm("TestRepeat.xml")
                .startBlankForm("TestRepeat")
                .clickOptionsIcon()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickNavigation()
                .clickUseSwipesAndButtons()
                .pressBack(new ProjectSettingsPage())
                .pressBack(new FormEntryPage("TestRepeat"))
                .swipeToNextQuestionWithRepeatGroup("Repeat # 1")
                .clickOnDoNotAdd(new AddNewRepeatDialog("Repeat # 2"))
                .clickOnDoNotAdd(new FormEntryPage("TestRepeat"))
                .clickForwardButtonToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void dynamicGroupLabel_should_beCalculatedProperly() {

        //TestCase3
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
                .clickOnDoNotAdd(new FormEntryPage("Repeat titles 1648"))
                .inputText("SecondPart")
                .closeSoftKeyboard()
                .swipeToNextQuestion("Date")
                .assertText("Part1 > 1 > Xxx: SecondPart")
                .clickGoToArrow()
                .assertText("Part1 > 1 > Xxx: SecondPart")
                .clickOnQuestion("Date")
                .swipeToNextQuestion("Multi Select")
                .swipeToNextQuestionWithRepeatGroup("Part1")
                .clickOnDoNotAdd(new FormEndPage("Repeat titles 1648"))
                .clickSaveAndExit();
    }

    @Test
    public void nestedGroupsWithFieldListAppearance_ShouldBeAbleToFillTheForm() {

        //TestCase5
        rule.startAtMainMenu()
                .copyForm("form1.xml")
                .startBlankForm("form1")
                .swipeToEndScreen()
                .clickSaveAndExit();

        rule.startAtMainMenu()
                .copyForm("form2.xml")
                .startBlankForm("form2")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();

        rule.startAtMainMenu()
                .copyForm("form3.xml")
                .startBlankForm("form3")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();

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
                .clickSaveAndExit();

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
                .clickSaveAndExit();

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
                .clickSaveAndExit();

        rule.startAtMainMenu()
                .copyForm("form7.xml")
                .startBlankForm("form7")
                .swipeToEndScreen()
                .clickSaveAndExit();

        rule.startAtMainMenu()
                .copyForm("form8.xml")
                .startBlankForm("form8")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();

        rule.startAtMainMenu()
                .copyForm("form9.xml")
                .startBlankForm("form9")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void whenNoRepeatGroupAdded_ShouldBackwardButtonBeClickable() {

        //TestCase6
        rule.startAtMainMenu()
                .copyForm("RepeatGroupAndGroup.xml")
                .openProjectSettingsDialog()
                .clickSettings()
                .clickOnUserInterface()
                .clickNavigation()
                .clickUseSwipesAndButtons()
                .pressBack(new ProjectSettingsPage())
                .pressBack(rule.startAtMainMenu())
                .startBlankFormWithRepeatGroup("RepeatGroupAndGroup", "G1")
                .clickOnDoNotAdd(new FormEntryPage("RepeatGroupAndGroup"))
                .closeSoftKeyboard()
                .swipeToPreviousQuestionWithRepeatGroup("G1")
                .clickOnDoNotAdd(new FormEntryPage("RepeatGroupAndGroup"))
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void when_pageBehindRepeatGroupWithRegularGroupInsideIsVisible_should_swipeBackWork() {

        //TestCase7
        rule.startAtMainMenu()
                .copyForm("repeat_group_new.xml")
                .startBlankFormWithRepeatGroup("RepeatGroupNew", "People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .inputText("A")
                .closeSoftKeyboard()
                .swipeToNextQuestion("Age")
                .inputText("1")
                .closeSoftKeyboard()
                .swipeToNextQuestionWithRepeatGroup("People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .assertQuestion("Name")
                .inputText("B")
                .closeSoftKeyboard()
                .swipeToNextQuestion("Age")
                .inputText("2")
                .closeSoftKeyboard()
                .swipeToNextQuestionWithRepeatGroup("People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .assertQuestion("Name")
                .inputText("C")
                .closeSoftKeyboard()
                .swipeToNextQuestion("Age")
                .inputText("3")
                .closeSoftKeyboard()
                .swipeToNextQuestionWithRepeatGroup("People")
                .clickOnDoNotAdd(new FormEndPage("RepeatGroupNew"))
                .swipeToPreviousQuestion("Age")
                .assertText("3")
                .swipeToNextQuestionWithRepeatGroup("People")
                .clickOnDoNotAdd(new FormEndPage("RepeatGroupNew"))
                .clickSaveAndExit();
    }

    @Test
    public void when_navigateOnHierarchyView_should_breadcrumbPathBeVisible() {

        //TestCase8
        rule.startAtMainMenu()
                .copyForm("repeat_group_new.xml")
                .startBlankFormWithRepeatGroup("RepeatGroupNew", "People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .inputText("A")
                .closeSoftKeyboard()
                .swipeToNextQuestion("Age")
                .inputText("1")
                .closeSoftKeyboard()
                .swipeToNextQuestionWithRepeatGroup("People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .inputText("B")
                .closeSoftKeyboard()
                .swipeToNextQuestion("Age")
                .inputText("2")
                .closeSoftKeyboard()
                .swipeToNextQuestionWithRepeatGroup("People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .inputText("C")
                .closeSoftKeyboard()
                .swipeToNextQuestion("Age")
                .inputText("3")
                .clickGoToArrow()
                .assertText("People > 3 > Person: C")
                .clickGoUpIcon()
                .assertText("3.\u200E Person: C")
                .clickJumpEndButton()
                .clickSaveAndExit();
    }

    @Test
    public void firstQuestionWithLongLabel_ShouldDisplayBothAnswersInHierarchyPage() {

        //TestCase11
        rule.startAtMainMenu()
                .copyForm("basic.xml")
                .startBlankForm("basic")
                .inputText("1")
                .closeSoftKeyboard()
                .swipeToNextQuestion("what is your age")
                .inputText("2")
                .closeSoftKeyboard()
                .clickGoToArrow()
                .assertText("2")
                .clickJumpEndButton()
                .clickSaveAndExit();
    }

    @Test
    public void openHierarchyPageFromLastView_ShouldNotDisplayError() {

        //TestCase12
        rule.startAtMainMenu()
                .copyForm("repeat_group_form.xml")
                .startBlankFormWithRepeatGroup("Repeat Group", "Grp1")
                .clickOnAdd(new FormEntryPage("Repeat Group"))
                .swipeToNextQuestionWithRepeatGroup("Grp1")
                .clickOnDoNotAdd(new FormEntryPage("Repeat Group"))
                .swipeToNextQuestionWithRepeatGroup("Grp2")
                .clickOnDoNotAdd(new FormEndPage("Repeat Group"))
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }
}
