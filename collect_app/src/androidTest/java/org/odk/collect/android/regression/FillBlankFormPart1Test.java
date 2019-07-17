package org.odk.collect.android.regression;

import android.Manifest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.espressoutils.Settings;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import java.util.Collections;

//Issue NODK-244
@RunWith(AndroidJUnit4.class)
public class FillBlankFormPart1Test extends BaseRegressionTest {
    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("formulaire_adherent.xml", "regression", Collections.singletonList("espece.csv")))
            .around(new CopyFormRule("CSVerrorForm.xml", "regression", Collections.singletonList("TrapLists.csv")))
            .around(new CopyFormRule("different-search-appearances.xml", "regression", Collections.singletonList("fruits.csv")))
            .around(new CopyFormRule("random.xml", "regression"))
            .around(new CopyFormRule("randomTest_broken.xml", "regression"));

    @Test
    public void answers_ShouldBeSuggestedInComplianceWithSelectedLetters() {
        MainMenu.startBlankForm("formulaire_adherent");
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.clickOnText("Plante");
        FormEntry.putText("Abi");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("Abies");
        FormEntry.swipeToPrevoiusQuestion();
        FormEntry.putText("Abr");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("Abrotanum alpestre");
    }

    @Test
    public void sortByDialog_ShouldBeTranslated() {
        MainMenu.clickOnMenu();
        MainMenu.clickGeneralSettings();
        Settings.clickOnUserInterface();
        Settings.clickOnLanguage();
        Settings.clickOnSelectedLanguage("Deutsch");
        MainMenu.clickFillBlankForm();
        MainMenu.clickOnSortByButton();
        FormEntry.checkIsTextDisplayed("Sortieren nach");
        FormEntry.checkIsTextDisplayed("Datum, neuestes zuerst");
        FormEntry.checkIsTextDisplayed("Datum, ältestes zuerst");
    }

    @Test
    public void searchExpression_ShouldDisplayWhenItContainsOtherAppearanceName() {
        MainMenu.startBlankForm("CSV error Form");
        FormEntry.clickOnText("Greg Pommen");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Mountain pine beetle");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("2018-COE-MPB-001 @ Wellington");
        FormEntry.swipeToPrevoiusQuestion();
        FormEntry.clickOnText("Invasive alien species");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("2018-COE-IAS-e-001 @ Coronation");
        FormEntry.swipeToPrevoiusQuestion();
        FormEntry.clickOnText("Longhorn beetles");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("2018-COE-LGH-M-001 @ Acheson");
        FormEntry.clickOnText("2018-COE-LGH-L-004 @ Acheson");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("No");
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);
    }

    @Test
    public void searchAppearance_ShouldDisplayWhenSearchAppearanceIsSpecified() {
        MainMenu.startBlankForm("different-search-appearances");
        FormEntry.clickOnText("Mango");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("The fruit mango pulled from csv");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Wolf");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("w");
        FormEntry.checkIsTextDisplayed("Wolf");
        FormEntry.checkIsTextDisplayed("Warthog");
        FormEntry.clickOnText("Wolf");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("r");
        FormEntry.checkIsTextDisplayed("Warthog");
        FormEntry.checkIsTextDisplayed("Raccoon");
        FormEntry.checkIsTextDisplayed("Rabbit");
        FormEntry.clickOnText("Rabbit");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("r");
        FormEntry.checkIsTextDisplayed("Oranges");
        FormEntry.checkIsTextDisplayed("Strawberris");
        FormEntry.clickOnText("Oranges");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("n");
        FormEntry.checkIsTextDisplayed("Mango");
        FormEntry.checkIsTextDisplayed("Oranges");
        FormEntry.clickOnText("Mango");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Mango");
        FormEntry.clickOnText("Strawberris");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Raccoon");
        FormEntry.clickOnText("Rabbit");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("w");
        FormEntry.checkIsTextDisplayed("Wolf");
        FormEntry.checkIsTextDisplayed("Warthog");
        FormEntry.clickOnText("Wolf");
        FormEntry.clickOnText("Warthog");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("r");
        FormEntry.checkIsTextDisplayed("Warthog");
        FormEntry.checkIsTextDisplayed("Raccoon");
        FormEntry.checkIsTextDisplayed("Rabbit");
        FormEntry.clickOnText("Raccoon");
        FormEntry.clickOnText("Rabbit");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("m");
        FormEntry.checkIsTextDisplayed("Mango");
        FormEntry.clickOnText("Mango");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("n");
        FormEntry.checkIsTextDisplayed("Mango");
        FormEntry.checkIsTextDisplayed("Oranges");
        FormEntry.clickOnText("Mango");
        FormEntry.clickOnText("Oranges");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);
    }

    @Test
    public void values_ShouldBeRandom() {
        //TestCase1
        for (int i = 1; i <= 3; i++) {
            MainMenu.startBlankForm("random");
            FormEntry.swipeToNextQuestion();
            FormEntry.swipeToNextQuestion();
            FormEntry.clickSaveAndExit();
        }
        //TestCase2
        for (int i = 1; i <= 3; i++) {
            MainMenu.startBlankForm("random test");
            FormEntry.putText("3");
            FormEntry.swipeToNextQuestion();
            FormEntry.swipeToNextQuestion();
            FormEntry.clickSaveAndExit();
        }
    }

}
