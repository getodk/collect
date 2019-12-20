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
import org.odk.collect.android.support.pages.BlankFormSearchPage;
import org.odk.collect.android.support.pages.ExitFormDialog;
import org.odk.collect.android.support.pages.FillBlankFormPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.support.ActivityHelpers;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertNotSame;
import static org.odk.collect.android.support.matchers.DrawableMatcher.withImageDrawable;
import static org.odk.collect.android.support.matchers.RecyclerViewMatcher.withRecyclerView;

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
            .around(new CopyFormRule("nigeria-wards.xml"))
            .around(new CopyFormRule("t21257.xml"))
            .around(new CopyFormRule("test_multiselect_cleared.xml"))
            .around(new CopyFormRule("Birds-encrypted.xml"))
            .around(new CopyFormRule("validate.xml"))
            .around(new CopyFormRule("event-odk-new-repeat.xml"))
            .around(new CopyFormRule("multiple-events.xml"));

    @Test
    public void subtext_ShouldDisplayAdditionalInformation() {

        //TestCase2
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .checkIsFormSubtextDisplayed();

    }

    @Test
    public void exitDialog_ShouldDisplaySaveAndIgnoreOptions() {

        //TestCase6 , TestCase9
        new MainMenuPage(rule)
                .startBlankForm("All widgets")
                .pressBack(new ExitFormDialog("All widgets", rule))
                .checkIsStringDisplayed(R.string.keep_changes)
                .checkIsStringDisplayed(R.string.do_not_save)
                .clickOnString(R.string.do_not_save)
                .checkIsIdDisplayed(R.id.enter_data)
                .checkIsIdDisplayed(R.id.get_forms);
    }

    @Test
    public void searchBar_ShouldSearchForm() {

        //TestCase12
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickMenuFilter()
                .searchInBar("Aaa")
                .pressBack(new BlankFormSearchPage(rule))
                .pressBack(new FillBlankFormPage(rule));
    }

    @Test
    public void navigationButtons_ShouldBeVisibleWhenAreSetInTheMiddleOfForm() {

        //TestCase16
        new MainMenuPage(rule)
                .startBlankForm("All widgets")
                .swipeToNextQuestion()
                .clickOptionsIcon()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickNavigation()
                .clickUseSwipesAndButtons()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new FormEntryPage("All widgets", rule))
                .checkAreNavigationButtonsDisplayed();
    }

    @Test
    public void formsWithDate_ShouldSaveFormsWithSuccess() {

        //TestCase17
        new MainMenuPage(rule)
                .startBlankForm("1560_DateData")
                .checkIsTranslationDisplayed("Jan 01, 1900", "01 ene. 1900")
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
        new MainMenuPage(rule)
                .startBlankFormWithRepeatGroup("formulaire_adherent")
                .clickOnAddGroup(new FormEntryPage("formulaire_adherent", rule))
                .clickOnText("Plante")
                .inputText("Abi")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("Abies")
                .swipeToPreviousQuestion()
                .inputText("Abr")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("Abrotanum alpestre");
    }

    @Test
    public void sortByDialog_ShouldBeTranslatedAndDisplayProperIcons() {

        //TestCase37
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickOnLanguage()
                .clickOnSelectedLanguage("Deutsch");

        new MainMenuPage(rule)
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

        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickOnLanguage()
                .clickOnSelectedLanguage("English");
    }

    @Test
    public void searchExpression_ShouldDisplayWhenItContainsOtherAppearanceName() {

        //TestCase26
        // This form doesn't define an instanceID and also doesn't request encryption so this case
        // would catch regressions for https://github.com/opendatakit/collect/issues/3340
        new MainMenuPage(rule).startBlankForm("CSV error Form")
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
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);
    }

    @Test
    public void predicateWarning_ShouldBeAbleToFillTheForm() {

        //TestCase24
        new MainMenuPage(rule)
                .startBlankForm("predicate-warning")
                .clickOnText("Apple")
                .swipeToNextQuestion()
                .clickOnText("Gala")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickOnText("Gala")
                .clickOnText("Granny Smith")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickSaveAndExit();

    }

    @Test
    public void searchAppearance_ShouldDisplayWhenSearchAppearanceIsSpecified() {

        //TestCase25
        new MainMenuPage(rule)
                .startBlankForm("different-search-appearances")
                .clickOnText("Mango")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("The fruit mango pulled from csv")
                .swipeToNextQuestion()
                .clickOnText("Wolf")
                .swipeToNextQuestion()
                .inputText("w")
                .closeSoftKeyboard()
                .checkIsTextDisplayed("Wolf")
                .checkIsTextDisplayed("Warthog")
                .clickOnText("Wolf")
                .swipeToNextQuestion()
                .inputText("r")
                .closeSoftKeyboard()
                .checkIsTextDisplayed("Warthog")
                .checkIsTextDisplayed("Raccoon")
                .checkIsTextDisplayed("Rabbit")
                .closeSoftKeyboard()
                .clickOnText("Rabbit")
                .swipeToNextQuestion()
                .inputText("r")
                .closeSoftKeyboard()
                .checkIsTextDisplayed("Oranges")
                .checkIsTextDisplayed("Strawberries")
                .clickOnText("Oranges")
                .swipeToNextQuestion()
                .inputText("n")
                .closeSoftKeyboard()
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
                .inputText("w")
                .closeSoftKeyboard()
                .checkIsTextDisplayed("Wolf")
                .checkIsTextDisplayed("Warthog")
                .clickOnText("Wolf")
                .clickOnText("Warthog")
                .swipeToNextQuestion()
                .inputText("r")
                .closeSoftKeyboard()
                .checkIsTextDisplayed("Warthog")
                .checkIsTextDisplayed("Raccoon")
                .checkIsTextDisplayed("Rabbit")
                .clickOnText("Raccoon")
                .clickOnText("Rabbit")
                .swipeToNextQuestion()
                .inputText("m")
                .closeSoftKeyboard()
                .checkIsTextDisplayed("Mango")
                .clickOnText("Mango")
                .swipeToNextQuestion()
                .inputText("n")
                .closeSoftKeyboard()
                .closeSoftKeyboard()
                .checkIsTextDisplayed("Mango")
                .checkIsTextDisplayed("Oranges")
                .clickOnText("Mango")
                .clickOnText("Oranges")
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);
    }

    @Test
    public void values_ShouldBeRandom() {

        //TestCase22
        List<String> firstQuestionAnswers = new ArrayList<>();
        List<String> secondQuestionAnswers = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            FormEntryPage formEntryPage = new MainMenuPage(rule).startBlankForm("random");
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
            FormEntryPage formEntryPage = new MainMenuPage(rule).startBlankForm("random test");
            formEntryPage.inputText("3");
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
        new MainMenuPage(rule)
                .startBlankFormWithError("g6Error")
                .clickOK(new FormEntryPage("g6Error", rule))
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);

        new MainMenuPage(rule).startBlankForm("g6Error2")
                .inputText("bla")
                .swipeToNextQuestionWithError()
                .clickOK(new FormEntryPage("g6Error2", rule))
                .swipeToNextQuestion()
                .inputText("ble")
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);

        new MainMenuPage(rule)
                .startBlankForm("emptyGroupFieldList")
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);

        new MainMenuPage(rule).startBlankForm("emptyGroupFieldList2")
                .inputText("nana")
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);
    }

    @Test
    public void user_ShouldBeAbleToFillTheForm() {

        //TestCase27
        new MainMenuPage(rule)
                .startBlankForm("metadata2")
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);
    }

    @Test
    public void question_ShouldBeVisibleOnTheTopOfHierarchy() {

        //TestCase23
        new MainMenuPage(rule)
                .startBlankForm("manyQ")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickGoToArrow()
                .checkIsTextDisplayed("n1")
                .checkIfTextDoesNotExist("t1")
                .checkIfTextDoesNotExist("t2");
    }

    @Test
    public void bigForm_ShouldBeFilledSuccessfully() {

        //TestCase18
        new MainMenuPage(rule)
                .startBlankForm("Nigeria Wards")
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
        FrameLayout questionContainer = formEntryActivity.findViewById(R.id.text_container);
        TextView questionView = (TextView) questionContainer.getChildAt(0);
        return questionView.getText().toString();
    }

    public void questionValidation_ShouldShowToastOnlyWhenConditionsAreNotMet() {

        //TestCase43
        new MainMenuPage(rule)
                .startBlankForm("t21257")
                .clickOnText("mytext1")
                .inputText("test")
                .swipeToNextQuestion()
                .inputText("17")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .checkIsToastWithMessageDisplayed("mydecimal constraint")
                .inputText("117")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .checkIsToastWithMessageDisplayed("mydecimal constraint")
                .inputText("50")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("16")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .checkIsToastWithMessageDisplayed("mynumbers constraint")
                .inputText("116")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .checkIsToastWithMessageDisplayed("mynumbers constraint")
                .inputText("51")
                .closeSoftKeyboard()
                .swipeToNextQuestion()
                .inputText("test2")
                .swipeToNextQuestion()
                .swipeToNextQuestion();
    }

    public void noDataLost_ShouldRememberAnswersForMultiSelectWidget() {

        //TestCase44
        new MainMenuPage(rule)
                .startBlankForm("test_multiselect_cleared")
                .clickOnText("a")
                .clickOnText("c")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickOnText("b")
                .clickOnText("d")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .swipeToPreviousQuestion()
                .swipeToPreviousQuestion()
                .clickGoToArrow()
                .checkIsTextDisplayed("a, c")
                .checkIsTextDisplayed("b, d")
                .clickJumpEndButton()
                .clickGoToArrow();
    }

    @Test
    public void encryptedFormWithNoInstanceId_shouldNotBeFinalized() {

        //TestCase47
        new MainMenuPage(rule)
                .startBlankForm("Birds")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("This form does not specify an instanceID. You must specify one to enable encryption. Form has not been saved as finalized.")
                .clickEditSavedForm()
                .checkInstanceState("Birds", InstanceProviderAPI.STATUS_INCOMPLETE);
    }

    @Test
    public void typeMismatchErrorMessage_shouldBeDisplayed() {

        //TestCase48
        new MainMenuPage(rule)
                .startBlankForm("validate")
                .clearTheText("2019")
                .swipeToNextQuestion()
                .checkIsStringDisplayed(R.string.error_occured)
                .checkIsTextDisplayedOnDialog("The value \"-01-01\" can't be converted to a date.")
                .clickOKOnDialog()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickSaveAndExit();
    }

    @Test
    public void answers_shouldBeAutoFilled() {

        //TestCase50
        new MainMenuPage(rule)
                .startBlankForm("Event: odk-new-repeat")
                .inputText("3")
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .checkIsTextDisplayed("1")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("5")
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .checkIsTextDisplayed("2")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("5")
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .checkIsTextDisplayed("3")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("5")
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .checkIsTextDisplayed("4")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("5")
                .swipeToNextQuestion()
                .clickOnDoNotAddGroup()
                .inputText("2")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("1")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("2")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickOnDoNotAddGroup()
                .clickSaveAndExit();
    }

    @Test
    public void questions_shouldHavePrefilledValue() {

        //TestCase51
        new MainMenuPage(rule)
                .startBlankForm("Space-separated event list")
                .checkIsTextDisplayed("cheese")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("more cheese")
                .swipeToNextQuestion()
                .checkIsTextDisplayed("5")
                .swipeToNextQuestion()
                .clickSaveAndExit();
    }

}
