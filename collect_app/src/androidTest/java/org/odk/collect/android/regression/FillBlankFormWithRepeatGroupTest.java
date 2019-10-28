package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.espressoutils.pages.FormEntryPage;
import org.odk.collect.android.espressoutils.pages.GeneralSettingsPage;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

//Issue NODK-247
@RunWith(AndroidJUnit4.class)
public class FillBlankFormWithRepeatGroupTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
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
            .around(new CopyFormRule("form9.xml"));

    @Test
    public void repeatGroupsNotAdded_ShouldNotDoubleLastMessage() {

        //TestCase1
        new MainMenuPage(main)
                .startBlankForm("TestRepeat")
                .clickOptionsIcon()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickNavigation()
                .clickUseSwipesAndButtons()
                .pressBack(new GeneralSettingsPage(main))
                .pressBack(new FormEntryPage("TestRepeat", main))
                .clickForwardButton()
                .clickOnDoNotAddGroup()
                .clickOnDoNotAddGroup()
                .clickForwardButton()
                .clickSaveAndExit();
    }

    @Test
    public void nestedGroupsWithFieldListAppearance_ShouldBeAbleToFillTheForm() {

        //TestCase5
        new MainMenuPage(main)
                .startBlankForm("form1")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickSaveAndExit();

        new MainMenuPage(main)
                .startBlankForm("form2")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickSaveAndExit();

        new MainMenuPage(main)
                .startBlankForm("form3")
                .swipeToNextQuestion()
                .clickSaveAndExit();

        new MainMenuPage(main)
                .startBlankForm("form4")
                .inputText("T1")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T2")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T3")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickSaveAndExit();

        new MainMenuPage(main)
                .startBlankForm("form5")
                .inputText("T1")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T2")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T3")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickSaveAndExit();

        new MainMenuPage(main)
                .startBlankForm("form6")
                .inputText("T1")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T2")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("T3")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickSaveAndExit();

        new MainMenuPage(main)
                .startBlankForm("form7")
                .swipeToNextQuestion()
                .clickSaveAndExit();

        new MainMenuPage(main)
                .startBlankForm("form8")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickSaveAndExit();

        new MainMenuPage(main)
                .startBlankForm("form9")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .clickSaveAndExit();
    }
}