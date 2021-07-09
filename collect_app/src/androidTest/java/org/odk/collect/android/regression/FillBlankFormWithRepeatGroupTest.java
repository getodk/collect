package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.ProjectSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;

//Issue NODK-247
@RunWith(AndroidJUnit4.class)
public class FillBlankFormWithRepeatGroupTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule())
            .around(new CopyFormRule("TestRepeat.xml"))
            .around(new CopyFormRule("form1.xml"))
            .around(new CopyFormRule("form2.xml"))
            .around(new CopyFormRule("form3.xml"))
            .around(new CopyFormRule("form4.xml"))
            .around(new CopyFormRule("form5.xml"))
            .around(new CopyFormRule("form6.xml"))
            .around(new CopyFormRule("form7.xml"))
            .around(new CopyFormRule("form8.xml"))
            .around(new CopyFormRule("form9.xml"))
            .around(new CopyFormRule("RepeatGroupAndGroup.xml"))
            .around(new CopyFormRule("basic.xml"))
            .around(new CopyFormRule("repeat_group_form.xml"))
            .around(new CopyFormRule("repeat_group_new.xml"))
            .around(new CopyFormRule("RepeatTitles_1648.xml"))
            .around(rule);

    @Test
    public void whenNoRepeatGroupAdded_ShouldNotDoubleLastQuestion() {

        //TestCase1
        new MainMenuPage()
                .startBlankForm("TestRepeat")
                .clickOptionsIcon()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickNavigation()
                .clickUseSwipesAndButtons()
                .pressBack(new ProjectSettingsPage())
                .pressBack(new FormEntryPage("TestRepeat"))
                .clickForwardButton()
                .clickOnDoNotAddGroup()
                .clickOnDoNotAddGroup()
                .clickForwardButtonToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void dynamicGroupLabel_should_beCalculatedProperly() {

        //TestCase3
        new MainMenuPage()
                .startBlankForm("Repeat titles 1648")
                .inputText("test")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("FirstPerson")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("25")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .assertText("gr1 > 1 > Person: 25")
                .clickGoToArrow()
                .assertText("gr1 > 1 > Person: 25")
                .clickOnQuestion("Photo")
                .swipeToNextQuestion()
                .clickOnDoNotAddGroup()
                .inputText("SecondPart")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .assertText("Part1 > 1 > Xxx: SecondPart")
                .clickGoToArrow()
                .assertText("Part1 > 1 > Xxx: SecondPart")
                .clickOnQuestion("Date")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickOnDoNotAddGroupEndingForm()
                .clickSaveAndExit();
    }

    @Test
    public void nestedGroupsWithFieldListAppearance_ShouldBeAbleToFillTheForm() {

        //TestCase5
        new MainMenuPage()
                .startBlankForm("form1")
                .swipeToNextQuestion()
                .swipeToEndScreen()
                .clickSaveAndExit();

        new MainMenuPage()
                .startBlankForm("form2")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();

        new MainMenuPage()
                .startBlankForm("form3")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();

        new MainMenuPage()
                .startBlankForm("form4")
                .inputText("T1")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T2")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T3")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();

        new MainMenuPage()
                .startBlankForm("form5")
                .inputText("T1")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T2")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T3")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();

        new MainMenuPage()
                .startBlankForm("form6")
                .inputText("T1")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T2")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T3")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();

        new MainMenuPage()
                .startBlankForm("form7")
                .swipeToEndScreen()
                .clickSaveAndExit();

        new MainMenuPage()
                .startBlankForm("form8")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();

        new MainMenuPage()
                .startBlankForm("form9")
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void whenNoRepeatGroupAdded_ShouldBackwardButtonBeClickable() {

        //TestCase6
        new MainMenuPage()
                .openProjectSettings()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickNavigation()
                .clickUseSwipesAndButtons()
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .startBlankFormWithRepeatGroup("RepeatGroupAndGroup", "G1")
                .clickOnDoNotAdd(new FormEntryPage("RepeatGroupAndGroup"))
                .closeSoftKeyboard()
                .clickBackwardButton()
                .clickOnDoNotAddGroup()
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void when_pageBehindRepeatGroupWithRegularGroupInsideIsVisible_should_swipeBackWork() {

        //TestCase7
        new MainMenuPage()
                .startBlankFormWithRepeatGroup("RepeatGroupNew", "People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .inputText("A")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("1")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .inputText("B")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("2")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .inputText("C")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("3")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickOnDoNotAddGroup()
                .swipeToPreviousQuestion()
                .assertText("3")
                .swipeToNextQuestion()
                .clickOnDoNotAddGroupEndingForm()
                .clickSaveAndExit();
    }

    @Test
    public void when_navigateOnHierarchyView_should_breadcrumbPathBeVisible() {

        //TestCase8
        new MainMenuPage()
                .startBlankFormWithRepeatGroup("RepeatGroupNew", "People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .inputText("A")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("1")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .inputText("B")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("2")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .inputText("C")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
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
        new MainMenuPage()
                .startBlankForm("basic")
                .inputText("1")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
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
        new MainMenuPage()
                .startBlankFormWithRepeatGroup("Repeat Group", "Grp1")
                .clickOnAdd(new FormEntryPage("Repeat Group"))
                .swipeToNextQuestion()
                .clickOnDoNotAddGroup()
                .swipeToNextQuestion()
                .clickOnDoNotAddGroup()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }
}
