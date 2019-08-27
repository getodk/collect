package org.odk.collect.android.regression;

import android.Manifest;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.espressoutils.Settings;
import org.odk.collect.android.espressoutils.pages.FormEntryPage;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
import org.odk.collect.android.espressoutils.pages.SettingsPage;
import org.odk.collect.android.support.ActivityHelpers;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertNotSame;
import static org.odk.collect.android.support.matchers.DrawableMatcher.withImageDrawable;
import static org.odk.collect.android.support.matchers.RecyclerViewMatcher.withRecyclerView;

import static androidx.test.espresso.Espresso.pressBack;

//Issue NODK-244
@RunWith(AndroidJUnit4.class)
public class FillBlankFormTest extends BaseRegressionTest {
    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml"))
            .around(new CopyFormRule("1560_DateData.xml"))
            .around(new CopyFormRule("1560_IntegerData.xml"))
            .around(new CopyFormRule("1560_IntegerData_instanceID.xml"))
            .around(new CopyFormRule("predicate-warning.xml"))
            .around(new CopyFormRule("formulaire_adherent.xml", Collections.singletonList("espece.csv")))
            .around(new CopyFormRule("CSVerrorForm.xml", Collections.singletonList("TrapLists.csv")))
            .around(new CopyFormRule("different-search-appearances.xml", Collections.singletonList("fruits.csv")))
            .around(new CopyFormRule("random.xml"))
            .around(new CopyFormRule("randomTest_broken.xml"))
            .around(new CopyFormRule("g6Error.xml"))
            .around(new CopyFormRule("g6Error2.xml"))
            .around(new CopyFormRule("emptyGroupFieldList.xml"))
            .around(new CopyFormRule("emptyGroupFieldList2.xml"))
            .around(new CopyFormRule("metadata2.xml"))
            .around(new CopyFormRule("manyQ.xml"))
            .around(new CopyFormRule("nigeria-wards.xml"));

    @Test
    public void subtext_ShouldDisplayAdditionalInformation() {

        //TestCase2
        new MainMenuPage(main)
                .clickFillBlankForm()
                .checkIsFormSubtextDisplayed();

    }

    @Test
    public void exitDialog_ShouldDisplaySaveAndIgnoreOptions() {

        //TestCase6 , TestCase9
        new MainMenuPage(main)
                .startBlankForm("All widgets")
                .pressBack(FormEntryPage.class)
                .checkIsStringDisplayed(R.string.keep_changes)
                .checkIsStringDisplayed(R.string.do_not_save)
                .clickOnString(R.string.do_not_save)
                .checkIsIdDisplayed(R.id.enter_data)
                .checkIsIdDisplayed(R.id.get_forms);
    }

    @Test
    public void searchBar_ShouldSearchForm() {

        //TestCase12
        new MainMenuPage(main)
                .clickFillBlankForm()
                .clickMenuFilter()
                .searchInBar("Aaa")
                .pressBack(MainMenuPage.class)
                .pressBack(MainMenuPage.class);
    }

    @Test
    public void navigationButtons_ShouldBeVisibleWhenAreSetInTheMiddleOfForm() {

        //TestCase16
        Settings.clickUseSwipesAndButtons();
        new MainMenuPage(main)
                .startBlankForm("All widgets")
                .swipeToNextQuestion()
                .clickOptionsIcon()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickNavigation()
                .pressBack(SettingsPage.class)
                .pressBack(FormEntryPage.class)
                .checkAreNavigationButtonsDisplayed();
    }

    @Test
    public void formsWithDate_ShouldSaveFormsWithSuccess() {

        //TestCase17
        new MainMenuPage(main)
                .startBlankForm("1560_DateData")
                .checkIsTextDisplayed("Jan 01, 1900")
                .swipeToNextQuestion()
                .clickSaveAndExit()

                .startBlankForm("1560_IntegerData")
                .checkIsTextDisplayed("5")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("5")
                .clickSaveAndExit()

                .startBlankForm("1560_IntegerData_instanceID")
                .checkIsTextDisplayed("5")
                .swipeToNextQuestion()
                .clickSaveAndExit();
    }

    @Test
    public void answers_ShouldBeSuggestedInComplianceWithSelectedLetters() {

        //TestCase41
        new MainMenuPage(main).startBlankForm("formulaire_adherent")
                .clickOnString(R.string.add_another)
                .clickOnText("Plante")
                .putText("Abi")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("Abies")
                .swipeToPreviousQuestion()
                .putText("Abr")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("Abrotanum alpestre");
    }

    @Test
    public void sortByDialog_ShouldBeTranslatedAndDisplayProperIcons() {

        //TestCase37
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickOnLanguage()
                .clickOnSelectedLanguage("Deutsch");

        new MainMenuPage(main)
                .clickFillBlankForm()
                .clickOnSortByButton()
                .checkIsTextDisplayed("Sortieren nach");

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(0, R.id.title))
                .check(matches(withText("Name, A-Z")));
        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(0, R.id.icon))
                .check(matches(withImageDrawable(R.drawable.ic_sort_by_alpha)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(1, R.id.title))
                .check(matches(withText("Name, Z-A")));
        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(1, R.id.icon))
                .check(matches(withImageDrawable(R.drawable.ic_sort_by_alpha)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(2, R.id.title))
                .check(matches(withText("Datum, neuestes zuerst")));
        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(2, R.id.icon))
                .check(matches(withImageDrawable(R.drawable.ic_access_time)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(3, R.id.title))
                .check(matches(withText("Datum, ältestes zuerst")));
        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(3, R.id.icon))
                .check(matches(withImageDrawable(R.drawable.ic_access_time)));
        pressBack();
        pressBack();

        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickOnLanguage()
                .clickOnSelectedLanguage("English");
    }

    @Test
    public void searchExpression_ShouldDisplayWhenItContainsOtherAppearanceName() {

        //TestCase26
        new MainMenuPage(main).startBlankForm("CSV error Form")
                .clickOnText("Greg Pommen")
                .swipeToNextQuestion()
                .clickOnText("Mountain pine beetle")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("2018-COE-MPB-001 @ Wellington")
                .swipeToPreviousQuestion()
                .clickOnText("Invasive alien species")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("2018-COE-IAS-e-001 @ Coronation")
                .swipeToPreviousQuestion()
                .clickOnText("Longhorn beetles")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("2018-COE-LGH-M-001 @ Acheson")
                .clickOnText("2018-COE-LGH-L-004 @ Acheson")
                .swipeToNextQuestion()
                .clickOnText("No")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("Form successfully saved!");
    }

    @Test
    public void searchAppearance_ShouldDisplayWhenSearchAppearanceIsSpecified() {

        //TestCase25
        new MainMenuPage(main)
                .startBlankForm("different-search-appearances")
                .clickOnText("Mango")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("The fruit mango pulled from csv")
                .swipeToNextQuestion()
                .clickOnText("Wolf")
                .swipeToNextQuestion()
                .putText("w")
                .checkIsTextDisplayed("Wolf")
                .checkIsTextDisplayed("Warthog")
                .clickOnText("Wolf")
                .swipeToNextQuestion()
                .putText("r")
                .checkIsTextDisplayed("Warthog")
                .checkIsTextDisplayed("Raccoon")
                .checkIsTextDisplayed("Rabbit")
                .clickOnText("Rabbit")
                .swipeToNextQuestion()
                .putText("r")
                .checkIsTextDisplayed("Oranges")
                .checkIsTextDisplayed("Strawberries")
                .clickOnText("Oranges")
                .swipeToNextQuestion()
                .putText("n")
                .checkIsTextDisplayed("Mango")
                .checkIsTextDisplayed("Oranges")
                .clickOnText("Mango")
                .swipeToNextQuestion()
                .clickOnText("Mango")
                .clickOnText("Strawberries")
                .swipeToNextQuestion()
                .clickOnText("Raccoon")
                .clickOnText("Rabbit")
                .swipeToNextQuestion()
                .putText("w")
                .checkIsTextDisplayed("Wolf")
                .checkIsTextDisplayed("Warthog")
                .clickOnText("Wolf")
                .clickOnText("Warthog")
                .swipeToNextQuestion()
                .putText("r")
                .closeSoftKeyboard()
                .checkIsTextDisplayed("Warthog")
                .checkIsTextDisplayed("Raccoon")
                .checkIsTextDisplayed("Rabbit")
                .clickOnText("Raccoon")
                .clickOnText("Rabbit")
                .swipeToNextQuestion()
                .putText("m")
                .checkIsTextDisplayed("Mango")
                .clickOnText("Mango")
                .swipeToNextQuestion()
                .putText("n")
                .closeSoftKeyboard()
                .checkIsTextDisplayed("Mango")
                .checkIsTextDisplayed("Oranges")
                .clickOnText("Mango")
                .clickOnText("Oranges")
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("Form successfully saved!");
    }

    @Test
    public void values_ShouldBeRandom() {

        //TestCase22
        FormEntryPage formEntryPage = new FormEntryPage(main);

        List<String> firstQuestionAnswers = new ArrayList<>();
        List<String> secondQuestionAnswers = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            new MainMenuPage(main).startBlankForm("random");
            firstQuestionAnswers.add(getQuestionText());
            formEntryPage.swipeToNextQuestion();
            secondQuestionAnswers.add(getQuestionText());
            formEntryPage.swipeToNextQuestion();
            formEntryPage.clickSaveAndExit();
        }

        assertNotSame(firstQuestionAnswers.get(0), firstQuestionAnswers.get(1));
        assertNotSame(firstQuestionAnswers.get(0), firstQuestionAnswers.get(2));
        assertNotSame(firstQuestionAnswers.get(1), firstQuestionAnswers.get(2));

        assertNotSame(secondQuestionAnswers.get(0), secondQuestionAnswers.get(1));
        assertNotSame(secondQuestionAnswers.get(0), secondQuestionAnswers.get(2));
        assertNotSame(secondQuestionAnswers.get(1), secondQuestionAnswers.get(2));

        firstQuestionAnswers.clear();

        for (int i = 1; i <= 3; i++) {
            new MainMenuPage(main).startBlankForm("random test");
            formEntryPage.putText("3");
            formEntryPage.swipeToNextQuestion();
            firstQuestionAnswers.add(getQuestionText());
            formEntryPage.swipeToNextQuestion();
            formEntryPage.clickSaveAndExit();
        }

        assertNotSame(firstQuestionAnswers.get(0), firstQuestionAnswers.get(1));
        assertNotSame(firstQuestionAnswers.get(0), firstQuestionAnswers.get(2));
        assertNotSame(firstQuestionAnswers.get(1), firstQuestionAnswers.get(2));
    }

    @Test
    public void app_ShouldNotCrash() {

        //TestCase32
        new MainMenuPage(main).startBlankForm("g6Error")
                .checkIsStringDisplayed(R.string.error_occured)
                .clickOk()
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("Form successfully saved!");

        new MainMenuPage(main).startBlankForm("g6Error2")
                .putText("bla")
                .swipeToNextQuestion()
                .checkIsStringDisplayed(R.string.error_occured)
                .clickOk()
                .swipeToNextQuestion()
                .putText("ble")
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("Form successfully saved!");

        new MainMenuPage(main)
                .startBlankForm("emptyGroupFieldList")
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("Form successfully saved!");

        new MainMenuPage(main).startBlankForm("emptyGroupFieldList2")
                .putText("nana")
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("Form successfully saved!");
    }

    @Test
    public void user_ShouldBeAbleToFillTheForm() {

        //TestCase27
        new MainMenuPage(main).startBlankForm("metadata2")
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("Form successfully saved!");
    }

    @Test
    public void question_ShouldBeVisibleOnTheTopOfHierarchy() {

        //TestCase23
        new MainMenuPage(main).startBlankForm("manyQ")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickGoToIconInForm()
                .checkIsTextDisplayed("n1")
                .checkIfTextDoesNotExist("t1")
                .checkIfTextDoesNotExist("t2");
    }

    @Test
    public void bigForm_ShouldBeFilledSuccessfully() {

        //TestCase18
        new MainMenuPage(main).startBlankForm("Nigeria Wards")
                .clickOnString(R.string.select_one)
                .clickOnText("Adamawa")
                .swipeToNextQuestion()
                .clickOnString(R.string.select_one)
                .clickOnText("Ganye")
                .swipeToNextQuestion()
                .clickOnString(R.string.select_one)
                .clickOnText("Jaggu")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickSaveAndExit();
    }

    private String getQuestionText() {
        FormEntryActivity formEntryActivity = (FormEntryActivity) ActivityHelpers.getActivity();
        FrameLayout questionContainer = formEntryActivity.findViewById(R.id.select_container);
        TextView questionView = (TextView) questionContainer.getChildAt(0);
        return questionView.getText().toString();
    }
}
