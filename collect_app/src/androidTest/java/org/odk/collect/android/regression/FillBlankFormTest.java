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
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.espressoutils.Settings;
import org.odk.collect.android.support.ActivityHelpers;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
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
        new MainMenu(main.getActivity()).clickFillBlankForm();
        new MainMenu(main.getActivity()).checkIsFormSubtextDisplayed();

    }

    @Test
    public void exitDialog_ShouldDisplaySaveAndIgnoreOptions() {

        //TestCase6 , TestCase9
        new MainMenu(main.getActivity()).startBlankForm("All widgets");
        pressBack();
        FormEntry.checkIsStringDisplayed(R.string.keep_changes);
        FormEntry.checkIsStringDisplayed(R.string.do_not_save);
        FormEntry.clickOnString(R.string.do_not_save);
        FormEntry.checkIsIdDisplayed(R.id.enter_data);
        FormEntry.checkIsIdDisplayed(R.id.get_forms);

    }

    @Test
    public void searchBar_ShouldSearchForm() {

        //TestCase12
        new MainMenu(main.getActivity()).clickFillBlankForm();
        new MainMenu(main.getActivity()).clickMenuFilter();
        new MainMenu(main.getActivity()).searchInBar("Aaa");
        pressBack();
        pressBack();

    }

    @Test
    public void navigationButtons_ShouldBeVisibleWhenAreSetInTheMiddleOfForm() {

        //TestCase16
        new MainMenu(main.getActivity()).startBlankForm("All widgets");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOptionsIcon();
        FormEntry.clickGeneralSettings();
        Settings.clickUserInterface();
        Settings.clickNavigation();
        Settings.clickUseSwipesAndButtons();
        pressBack();
        pressBack();
        FormEntry.checkAreNavigationButtonsDisplayed();

    }

    @Test
    public void formsWithDate_ShouldSaveFormsWithSuccess() {

        //TestCase17
        new MainMenu(main.getActivity()).startBlankForm("1560_DateData");
        FormEntry.checkIsTextDisplayed("Jan 01, 1900");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();

        new MainMenu(main.getActivity()).startBlankForm("1560_IntegerData");
        FormEntry.checkIsTextDisplayed("5");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("5");
        FormEntry.clickSaveAndExit();

        new MainMenu(main.getActivity()).startBlankForm("1560_IntegerData_instanceID");
        FormEntry.checkIsTextDisplayed("5");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();

    }

    @Test
    public void answers_ShouldBeSuggestedInComplianceWithSelectedLetters() {

        //TestCase41
        new MainMenu(main.getActivity()).startBlankForm("formulaire_adherent");
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
    public void sortByDialog_ShouldBeTranslatedAndDisplayProperIcons() {

        //TestCase37
        new MainMenu(main.getActivity())
                .clickOnMenu()
                .clickGeneralSettings();

        Settings.clickOnUserInterface();
        Settings.clickOnLanguage();
        Settings.clickOnSelectedLanguage("Deutsch");
        new MainMenu(main.getActivity()).clickFillBlankForm();
        new MainMenu(main.getActivity()).clickOnSortByButton();
        FormEntry.checkIsTextDisplayed("Sortieren nach");

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
        
        new MainMenu(main.getActivity())
                .clickOnMenu()
                .clickGeneralSettings();

        Settings.clickOnUserInterface();
        Settings.clickOnLanguage();
        Settings.clickOnSelectedLanguage("English");
    }

    @Test
    public void searchExpression_ShouldDisplayWhenItContainsOtherAppearanceName() {

        //TestCase26
        new MainMenu(main.getActivity()).startBlankForm("CSV error Form");
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

        //TestCase25
        new MainMenu(main.getActivity()).startBlankForm("different-search-appearances");
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
        FormEntry.checkIsTextDisplayed("Strawberries");
        FormEntry.clickOnText("Oranges");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("n");
        FormEntry.checkIsTextDisplayed("Mango");
        FormEntry.checkIsTextDisplayed("Oranges");
        FormEntry.clickOnText("Mango");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Mango");
        FormEntry.clickOnText("Strawberries");
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
        closeSoftKeyboard();
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
        closeSoftKeyboard();
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

        //TestCase22
        List<String> firstQuestionAnswers = new ArrayList<>();
        List<String> secondQuestionAnswers = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            new MainMenu(main.getActivity()).startBlankForm("random");
            firstQuestionAnswers.add(getQuestionText());
            FormEntry.swipeToNextQuestion();
            secondQuestionAnswers.add(getQuestionText());
            FormEntry.swipeToNextQuestion();
            FormEntry.clickSaveAndExit();
        }

        assertNotSame(firstQuestionAnswers.get(0), firstQuestionAnswers.get(1));
        assertNotSame(firstQuestionAnswers.get(0), firstQuestionAnswers.get(2));
        assertNotSame(firstQuestionAnswers.get(1), firstQuestionAnswers.get(2));

        assertNotSame(secondQuestionAnswers.get(0), secondQuestionAnswers.get(1));
        assertNotSame(secondQuestionAnswers.get(0), secondQuestionAnswers.get(2));
        assertNotSame(secondQuestionAnswers.get(1), secondQuestionAnswers.get(2));

        firstQuestionAnswers.clear();

        for (int i = 1; i <= 3; i++) {
            new MainMenu(main.getActivity()).startBlankForm("random test");
            FormEntry.putText("3");
            FormEntry.swipeToNextQuestion();
            firstQuestionAnswers.add(getQuestionText());
            FormEntry.swipeToNextQuestion();
            FormEntry.clickSaveAndExit();
        }

        assertNotSame(firstQuestionAnswers.get(0), firstQuestionAnswers.get(1));
        assertNotSame(firstQuestionAnswers.get(0), firstQuestionAnswers.get(2));
        assertNotSame(firstQuestionAnswers.get(1), firstQuestionAnswers.get(2));
    }

    @Test
    public void app_ShouldNotCrash() {

        //TestCase32
        new MainMenu(main.getActivity()).startBlankForm("g6Error");
        FormEntry.checkIsStringDisplayed(R.string.error_occured);
        FormEntry.clickOk();
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);

        new MainMenu(main.getActivity()).startBlankForm("g6Error2");
        FormEntry.putText("bla");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsStringDisplayed(R.string.error_occured);
        FormEntry.clickOk();
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("ble");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);

        new MainMenu(main.getActivity()).startBlankForm("emptyGroupFieldList");
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);

        new MainMenu(main.getActivity()).startBlankForm("emptyGroupFieldList2");
        FormEntry.putText("nana");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);
    }

    @Test
    public void user_ShouldBeAbleToFillTheForm() {

        //TestCase27
        new MainMenu(main.getActivity()).startBlankForm("metadata2");
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);
    }

    private String getQuestionText() {
        FormEntryActivity formEntryActivity = (FormEntryActivity) ActivityHelpers.getActivity();
        FrameLayout questionContainer = formEntryActivity.findViewById(R.id.select_container);
        TextView questionView = (TextView) questionContainer.getChildAt(0);
        return questionView.getText().toString();
    }

    @Test
    public void question_ShouldBeVisibleOnTheTopOfHierarchy() {

        //TestCase23
        new MainMenu(main.getActivity()).startBlankForm("manyQ");
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.clickGoToIconInForm();
        FormEntry.checkIsTextDisplayed("n1");
        FormEntry.checkIfTextDoesNotExist("t1");
        FormEntry.checkIfTextDoesNotExist("t2");
    }

    @Test
    public void bigForm_ShouldBeFilledSuccessfully() {

        //TestCase18
        new MainMenu(main.getActivity()).startBlankForm("Nigeria Wards");
        FormEntry.clickOnString(R.string.select_one);
        FormEntry.clickOnText("Adamawa");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.select_one);
        FormEntry.clickOnText("Ganye");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.select_one);
        FormEntry.clickOnText("Jaggu");
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
    }
}
