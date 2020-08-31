package org.odk.collect.android.regression;

import android.Manifest;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.support.ActivityHelpers;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.BlankFormSearchPage;
import org.odk.collect.android.support.pages.ExitFormDialog;
import org.odk.collect.android.support.pages.FillBlankFormPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;

import java.util.ArrayList;
import java.util.Arrays;
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
public class FillBlankFormTest {

    public CollectTestRule rule = new CollectTestRule();

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
            .around(new CopyFormRule("multiple-events.xml"))
            .around(new CopyFormRule("CalcTest.xml"))
            .around(new CopyFormRule("3403.xml", Arrays.asList("staff_list.csv", "staff_rights.csv")))
            .around(new CopyFormRule("CalcTest.xml"))
            .around(new CopyFormRule("search_and_select.xml"))
            .around(new CopyFormRule("select_one_external.xml"))
            .around(new CopyFormRule("fieldlist-updates_nocsv.xml"))
            .around(new CopyFormRule("nested-repeats-complex.xml"))
            .around(new CopyFormRule("repeat_group_form.xml"))
            .around(rule);

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
                .assertText(R.string.keep_changes)
                .assertText(R.string.do_not_save)
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
                .swipeToEndScreen()
                .clickSaveAndExit()

                .startBlankForm("1560_IntegerData")
                .assertText("5")
                .swipeToEndScreen()
                .assertText("5")
                .clickSaveAndExit()

                .startBlankForm("1560_IntegerData_instanceID")
                .assertText("5")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void answers_ShouldBeSuggestedInComplianceWithSelectedLetters() {

        //TestCase41
        new MainMenuPage(rule)
                .startBlankFormWithRepeatGroup("formulaire_adherent", "Ajouté une observation")
                .clickOnAdd(new FormEntryPage("formulaire_adherent", rule))
                .clickOnText("Plante")
                .inputText("Abi")
                .swipeToNextQuestion()
                .assertText("Abies")
                .swipeToPreviousQuestion()
                .inputText("Abr")
                .swipeToNextQuestion()
                .assertText("Abrotanum alpestre");
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
                .assertText("Sortieren nach");

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
        // would catch regressions for https://github.com/getodk/collect/issues/3340
        new MainMenuPage(rule).startBlankForm("CSV error Form")
                .clickOnText("Greg Pommen")
                .swipeToNextQuestion()
                .clickOnText("Mountain pine beetle")
                .swipeToNextQuestion()
                .assertText("2018-COE-MPB-001 @ Wellington")
                .swipeToPreviousQuestion()
                .clickOnText("Invasive alien species")
                .swipeToNextQuestion()
                .assertText("2018-COE-IAS-e-001 @ Coronation")
                .swipeToPreviousQuestion()
                .clickOnText("Longhorn beetles")
                .swipeToNextQuestion()
                .assertText("2018-COE-LGH-M-001 @ Acheson")
                .clickOnText("2018-COE-LGH-L-004 @ Acheson")
                .swipeToNextQuestion()
                .clickOnText("No")
                .swipeToNextQuestion()
                .swipeToEndScreen()
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
                .swipeToEndScreen()
                .clickSaveAndExit();

    }

    @Test
    public void searchAppearance_ShouldDisplayWhenSearchAppearanceIsSpecified() {

        //TestCase25
        new MainMenuPage(rule)
                .startBlankForm("different-search-appearances")
                .clickOnText("Mango")
                .swipeToNextQuestion()
                .assertText("The fruit mango pulled from csv")
                .swipeToNextQuestion()
                .clickOnText("Wolf")
                .swipeToNextQuestion()
                .inputText("w")
                .closeSoftKeyboard()
                .assertText("Wolf")
                .assertText("Warthog")
                .clickOnText("Wolf")
                .swipeToNextQuestion()
                .inputText("r")
                .closeSoftKeyboard()
                .assertText("Warthog")
                .assertText("Raccoon")
                .assertText("Rabbit")
                .closeSoftKeyboard()
                .clickOnText("Rabbit")
                .swipeToNextQuestion()
                .inputText("r")
                .closeSoftKeyboard()
                .assertText("Oranges")
                .assertText("Strawberries")
                .clickOnText("Oranges")
                .swipeToNextQuestion()
                .inputText("n")
                .closeSoftKeyboard()
                .assertText("Mango")
                .assertText("Oranges")
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
                .assertText("Wolf")
                .assertText("Warthog")
                .clickOnText("Wolf")
                .clickOnText("Warthog")
                .swipeToNextQuestion()
                .inputText("r")
                .closeSoftKeyboard()
                .assertText("Warthog")
                .assertText("Raccoon")
                .assertText("Rabbit")
                .clickOnText("Raccoon")
                .clickOnText("Rabbit")
                .swipeToNextQuestion()
                .inputText("m")
                .closeSoftKeyboard()
                .assertText("Mango")
                .clickOnText("Mango")
                .swipeToNextQuestion()
                .inputText("n")
                .closeSoftKeyboard()
                .closeSoftKeyboard()
                .assertText("Mango")
                .assertText("Oranges")
                .clickOnText("Mango")
                .clickOnText("Oranges")
                .swipeToEndScreen()
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
            formEntryPage.swipeToEndScreen().clickSaveAndExit();
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
            formEntryPage.swipeToEndScreen().clickSaveAndExit();
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
                .swipeToEndScreen()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);

        new MainMenuPage(rule).startBlankForm("g6Error2")
                .inputText("bla")
                .swipeToNextQuestionWithError()
                .clickOK(new FormEntryPage("g6Error2", rule))
                .swipeToNextQuestion()
                .inputText("ble")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);

        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickOnEmptyForm("emptyGroupFieldList")
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);

        new MainMenuPage(rule).startBlankForm("emptyGroupFieldList2")
                .inputText("nana")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);
    }

    @Test
    public void user_ShouldBeAbleToFillTheForm() {

        //TestCase27
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickOnEmptyForm("metadata2")
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
                .assertText("n1")
                .assertTextDoesNotExist("t1")
                .assertTextDoesNotExist("t2");
    }

    @Test
    public void bigForm_ShouldBeFilledSuccessfully() {
        //TestCase18
        new MainMenuPage(rule)
                .startBlankForm("Nigeria Wards")
                .assertQuestion("State")
                .openSelectMinimalDialog()
                .clickOnText("Adamawa")
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("LGA", true)
                .openSelectMinimalDialog()
                .clickOnText("Ganye")
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Ward", true)
                .openSelectMinimalDialog()
                .clickOnText("Jaggu")
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Comments")
                .swipeToEndScreen()
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
                .assertText("a, c")
                .assertText("b, d")
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
                .checkInstanceState("Birds", Instance.STATUS_INCOMPLETE);
    }

    @Test
    public void typeMismatchErrorMessage_shouldBeDisplayed() {

        //TestCase48
        new MainMenuPage(rule)
                .startBlankForm("validate")
                .clearTheText("2019")
                .swipeToNextQuestion()
                .assertText(R.string.error_occured)
                .checkIsTextDisplayedOnDialog("The value \"-01-01\" can't be converted to a date.")
                .clickOKOnDialog()
                .swipeToNextQuestion()
                .swipeToEndScreen()
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
                .assertText("1")
                .swipeToNextQuestion()
                .assertText("5")
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .assertText("2")
                .swipeToNextQuestion()
                .assertText("5")
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .assertText("3")
                .swipeToNextQuestion()
                .assertText("5")
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .assertText("4")
                .swipeToNextQuestion()
                .assertText("5")
                .swipeToNextQuestion()
                .clickOnDoNotAddGroup()
                .inputText("2")
                .swipeToNextQuestion()
                .assertText("1")
                .swipeToNextQuestion()
                .assertText("2")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickOnDoNotAddGroupEndingForm()
                .clickSaveAndExit();
    }

    @Test
    public void questions_shouldHavePrefilledValue() {

        //TestCase51
        new MainMenuPage(rule)
                .startBlankForm("Space-separated event list")
                .assertText("cheese")
                .swipeToNextQuestion()
                .assertText("more cheese")
                .swipeToNextQuestion()
                .assertText("5")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void when_chooseAnswer_should_beVisibleInNextQuestion() {
        //TestCase52
        new MainMenuPage(rule)
                .startBlankFormWithRepeatGroup("CalcTest", "Fishing gear type")
                .clickOnAdd(new FormEntryPage("CalcTest", rule))
                .clickOnText("Gillnet")
                .swipeToNextQuestion()
                .assertText("* 7.2 What is the size of the mesh for the Gillnet ?")
                .swipeToPreviousQuestion()
                .clickOnText("Seinenet")
                .swipeToNextQuestion()
                .assertText("* 7.2 What is the size of the mesh for the Seinenet ?");
    }

    @Test
    public void when_scrollQuestionsList_should_questionsNotDisappear() {
        //TestCase54
        new MainMenuPage(rule)
                .startBlankForm("3403_ODK Version 1.23.3 Tester")
                .clickOnText("New Farmer Registration")
                .scrollToAndClickText("Insemination")
                .scrollToAndAssertText("New Farmer Registration");
    }

    @Test
    public void missingFileMessage_shouldBeDisplayedIfExternalFIleIsMissing() {
        //TestCase55
        new MainMenuPage(rule)
                .startBlankForm("search_and_select")
                .assertText("File: /storage/emulated/0/odk/forms/search_and_select-media/nombre.csv is missing.")
                .assertText("File: /storage/emulated/0/odk/forms/search_and_select-media/nombre2.csv is missing.")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .startBlankForm("cascading select test")
                .clickOnText("Texas")
                .swipeToNextQuestion()
                .assertText("File: /storage/emulated/0/odk/forms/select_one_external-media/itemsets.csv is missing.")
                .swipeToNextQuestion()
                .assertText("File: /storage/emulated/0/odk/forms/select_one_external-media/itemsets.csv is missing.")
                .swipeToNextQuestion(3)
                .swipeToEndScreen()
                .clickSaveAndExit()

                .startBlankForm("fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnElementInHierarchy(14)
                .clickOnQuestion("Source15")
                .assertText("File: /storage/emulated/0/odk/forms/fieldlist-updates_nocsv-media/fruits.csv is missing.")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void changedName_shouldNotDisappearAfterScreenRotation() {
        //TestCase13
        new MainMenuPage(rule)
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickOnId(R.id.save_name)
                .inputText("submission")
                .closeSoftKeyboard()
                .rotateToLandscape(new FormEntryPage("All widgets", rule))
                .assertText("submission")
                .rotateToPortrait(new FormEntryPage("All widgets", rule))
                .assertText("submission");
    }

    @Test
    public void backwardButton_shouldNotBeClickableOnTheFirstFormPage() {
        //TestCase14
        new MainMenuPage(rule)
                .startBlankForm("All widgets")
                .checkAreNavigationButtonsNotDisplayed()
                .clickOptionsIcon()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickNavigation()
                .clickUseNavigationButtons()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new FormEntryPage("All widgets", rule))
                .checkBackNavigationButtonIsNotsDisplayed()
                .checkNextNavigationButtonIsDisplayed()
                .rotateToLandscape(new FormEntryPage("All widgets", rule))
                .checkBackNavigationButtonIsNotsDisplayed()
                .checkNextNavigationButtonIsDisplayed()
                .rotateToPortrait(new FormEntryPage("All widgets", rule))
                .checkBackNavigationButtonIsNotsDisplayed()
                .checkNextNavigationButtonIsDisplayed();
    }

    @Test
    public void groups_shouldBeVisibleInHierarchyView() {
        //TestCase28
        new MainMenuPage(rule)
                .startBlankForm("nested-repeats-complex")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .inputText("La")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .inputText("Le")
                .swipeToNextQuestion()
                .clickOnAddGroup()
                .inputText("Be")
                .swipeToNextQuestion()
                .clickOnDoNotAddGroup()
                .clickOnDoNotAddGroup()
                .clickOnAddGroup()
                .inputText("Bu")
                .swipeToNextQuestion()
                .clickOnDoNotAddGroup()
                .clickGoToArrow()
                .clickOnText("Friends")
                .checkListSizeInHierarchy(1)
                .clickOnElementInHierarchy(0)
                .clickOnText("Pets")
                .checkListSizeInHierarchy(2)
                .clickGoUpIcon()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .clickOnText("Enemies")
                .checkListSizeInHierarchy(1);
    }

    @Test
    public void hierachyView_shouldNotChangeAfterScreenRotation() {
        //TestCase29
        new MainMenuPage(rule)
                .startBlankFormWithRepeatGroup("Repeat Group", "Grp1")
                .clickOnDoNotAdd(new FormEntryPage("Repeat Group", rule))
                .clickGoToArrow()
                .clickGoUpIcon()
                .checkIfElementInHierarchyMatchesToText("Group Name", 0)
                .rotateToLandscape(new FormEntryPage("Repeat Group", rule))
                .checkIfElementInHierarchyMatchesToText("Group Name", 0)
                .rotateToPortrait(new FormEntryPage("Repeat Group", rule))
                .checkIfElementInHierarchyMatchesToText("Group Name", 0);
    }

    @Test
    public void when_openHierarchyViewFromLastPage_should_mainGroupViewBeVisible() {
        //TestCase30
        new MainMenuPage(rule)
                .startBlankFormWithRepeatGroup("Repeat Group", "Grp1")
                .clickOnDoNotAdd(new FormEntryPage("Repeat Group", rule))
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickGoToArrow()
                .checkIfElementInHierarchyMatchesToText("Group Name", 0);
    }
}
