package org.odk.collect.android.regression;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertNotSame;
import static org.odk.collect.android.support.matchers.DrawableMatcher.withImageDrawable;
import static org.odk.collect.android.support.matchers.RecyclerViewMatcher.withRecyclerView;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.ActivityHelpers;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.BlankFormSearchPage;
import org.odk.collect.android.support.pages.ExitFormDialog;
import org.odk.collect.android.support.pages.FillBlankFormPage;
import org.odk.collect.android.support.pages.FormEndPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.ProjectSettingsPage;

import java.util.ArrayList;
import java.util.List;

//Issue NODK-244
@RunWith(AndroidJUnit4.class)
public class FillBlankFormTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(new ResetStateRule())
            .around(rule);

    @Test
    public void subtext_ShouldDisplayAdditionalInformation() {
        //TestCase2
        rule.startAtMainMenu()
                .copyForm("All_widgets.xml")
                .clickFillBlankForm()
                .checkIsFormSubtextDisplayed();

    }

    @Test
    public void exitDialog_ShouldDisplaySaveAndIgnoreOptions() {
        //TestCase6 , TestCase9
        rule.startAtMainMenu()
                .copyForm("All_widgets.xml")
                .startBlankForm("All widgets")
                .pressBack(new ExitFormDialog("All widgets"))
                .assertText(R.string.keep_changes)
                .assertText(R.string.do_not_save)
                .clickOnString(R.string.do_not_save)
                .checkIsIdDisplayed(R.id.enter_data)
                .checkIsIdDisplayed(R.id.get_forms);
    }

    @Test
    public void searchBar_ShouldSearchForm() {
        //TestCase12
        rule.startAtMainMenu()
                .clickFillBlankForm()
                .clickMenuFilter()
                .searchInBar("Aaa")
                .pressBack(new BlankFormSearchPage())
                .pressBack(new FillBlankFormPage());
    }

    @Test
    public void navigationButtons_ShouldBeVisibleWhenAreSetInTheMiddleOfForm() {
        //TestCase16
        rule.startAtMainMenu()
                .copyForm("All_widgets.xml")
                .startBlankForm("All widgets")
                .swipeToNextQuestion("String widget")
                .clickOptionsIcon()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .clickNavigation()
                .clickUseSwipesAndButtons()
                .pressBack(new ProjectSettingsPage())
                .pressBack(new FormEntryPage("All widgets"))
                .assertNavigationButtonsAreDisplayed();
    }

    @Test
    public void formsWithDate_ShouldSaveFormsWithSuccess() {
        //TestCase17
        rule.startAtMainMenu()
                .copyForm("1560_DateData.xml")
                .startBlankForm("1560_DateData")
                .checkIsTranslationDisplayed("Jan 01, 1900", "01 ene. 1900")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .copyForm("1560_IntegerData.xml")
                .startBlankForm("1560_IntegerData")
                .assertText("5")
                .swipeToEndScreen()
                .assertText("5")
                .clickSaveAndExit()

                .copyForm("1560_IntegerData_instanceID.xml")
                .startBlankForm("1560_IntegerData_instanceID")
                .assertText("5")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void answers_ShouldBeSuggestedInComplianceWithSelectedLetters() {
        //TestCase41
        rule.startAtMainMenu()
                .copyForm("formulaire_adherent.xml", singletonList("espece.csv"))
                .startBlankFormWithRepeatGroup("formulaire_adherent", "Ajouté une observation")
                .clickOnAdd(new FormEntryPage("formulaire_adherent"))
                .clickOnText("Plante")
                .inputText("Abi")
                .swipeToNextQuestion("Nom latin de l'espece", true)
                .assertText("Abies")
                .swipeToPreviousQuestion("Nom latin de l'espece - au moins 3 lettres", true)
                .inputText("Abr")
                .swipeToNextQuestion("Nom latin de l'espece", true)
                .assertText("Abrotanum alpestre");
    }

    @Test
    public void sortByDialog_ShouldBeTranslatedAndDisplayProperIcons() {
        //TestCase37
        rule.startAtMainMenu()
                .openProjectSettingsDialog()
                .clickSettings()
                .clickOnUserInterface()
                .clickOnLanguage()
                .clickOnSelectedLanguage("Deutsch")
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

        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickSettings()
                .clickOnUserInterface()
                .clickOnLanguage()
                .clickOnSelectedLanguage("English");
    }

    @Test
    public void searchExpression_ShouldDisplayWhenItContainsOtherAppearanceName() {
        //TestCase26
        // This form doesn't define an instanceID and also doesn't request encryption so this case
        // would catch regressions for https://github.com/getodk/collect/issues/3340
        rule.startAtMainMenu()
                .copyForm("CSVerrorForm.xml", singletonList("TrapLists.csv"))
                .startBlankForm("CSV error Form")
                .clickOnText("Greg Pommen")
                .swipeToNextQuestion("* Select trap program:")
                .clickOnText("Mountain pine beetle")
                .swipeToNextQuestion("Pick Trap Name:")
                .assertText("2018-COE-MPB-001 @ Wellington")
                .swipeToPreviousQuestion("* Select trap program:")
                .clickOnText("Invasive alien species")
                .swipeToNextQuestion("Pick Trap Name:")
                .assertText("2018-COE-IAS-e-001 @ Coronation")
                .swipeToPreviousQuestion("* Select trap program:")
                .clickOnText("Longhorn beetles")
                .swipeToNextQuestion("Pick Trap Name:")
                .assertText("2018-COE-LGH-M-001 @ Acheson")
                .clickOnText("2018-COE-LGH-L-004 @ Acheson")
                .swipeToNextQuestion("* Were there specimens in the trap:")
                .clickOnText("No")
                .swipeToNextQuestion("Any other notes?")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);
    }

    @Test
    public void predicateWarning_ShouldBeAbleToFillTheForm() {
        //TestCase24
        rule.startAtMainMenu()
                .copyForm("predicate-warning.xml")
                .startBlankForm("predicate-warning")
                .clickOnText("Apple")
                .swipeToNextQuestion("Variety (absolute reference)")
                .clickOnText("Gala")
                .swipeToNextQuestion("Variety (relative reference)")
                .swipeToNextQuestion("Varieties (absolute reference)")
                .clickOnText("Gala")
                .clickOnText("Granny Smith")
                .swipeToNextQuestion("Varieties (relative reference)")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void searchAppearance_ShouldDisplayWhenSearchAppearanceIsSpecified() {
        //TestCase25
        rule.startAtMainMenu()
                .copyForm("different-search-appearances.xml", singletonList("fruits.csv"))
                .startBlankForm("different-search-appearances")
                .clickOnText("Mango")
                .swipeToNextQuestion("The fruit mango pulled from csv")
                .assertText("The fruit mango pulled from csv")
                .swipeToNextQuestion("Static select with no appearance")
                .clickOnText("Wolf")
                .swipeToNextQuestion("Static select with search appearance")
                .inputText("w")
                .closeSoftKeyboard()
                .assertText("Wolf")
                .assertText("Warthog")
                .clickOnText("Wolf")
                .swipeToNextQuestion("Static select with autocomplete appearance")
                .inputText("r")
                .closeSoftKeyboard()
                .assertText("Warthog")
                .assertText("Raccoon")
                .assertText("Rabbit")
                .closeSoftKeyboard()
                .clickOnText("Rabbit")
                .swipeToNextQuestion("Select from a CSV using search() appearance/function and search appearance")
                .inputText("r")
                .closeSoftKeyboard()
                .assertText("Oranges")
                .assertText("Strawberries")
                .clickOnText("Oranges")
                .swipeToNextQuestion("Select from a CSV using search() appearance/function and autocomplete appearance")
                .inputText("n")
                .closeSoftKeyboard()
                .assertText("Mango")
                .assertText("Oranges")
                .clickOnText("Mango")
                .swipeToNextQuestion("Select from a CSV using search() appearance/function")
                .clickOnText("Mango")
                .clickOnText("Strawberries")
                .swipeToNextQuestion("Static select with no appearance")
                .clickOnText("Raccoon")
                .clickOnText("Rabbit")
                .swipeToNextQuestion("Static select with search appearance")
                .inputText("w")
                .closeSoftKeyboard()
                .assertText("Wolf")
                .assertText("Warthog")
                .clickOnText("Wolf")
                .clickOnText("Warthog")
                .swipeToNextQuestion("Static select with autocomplete appearance")
                .inputText("r")
                .closeSoftKeyboard()
                .assertText("Warthog")
                .assertText("Raccoon")
                .assertText("Rabbit")
                .clickOnText("Raccoon")
                .clickOnText("Rabbit")
                .swipeToNextQuestion("Select from a CSV using search() appearance/function and search appearance")
                .inputText("m")
                .closeSoftKeyboard()
                .assertText("Mango")
                .clickOnText("Mango")
                .swipeToNextQuestion("Select from a CSV using search() appearance/function and autocomplete appearance")
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
        rule.startAtMainMenu()
                .copyForm("random.xml")
                .copyForm("randomTest_broken.xml");

        //TestCase22
        List<String> firstQuestionAnswers = new ArrayList<>();
        List<String> secondQuestionAnswers = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            FormEntryPage formEntryPage = new MainMenuPage().startBlankForm("random");
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
            FormEntryPage formEntryPage = new MainMenuPage().startBlankForm("random test");
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
        rule.startAtMainMenu()
                .copyForm("g6Error.xml")
                .startBlankFormWithError("g6Error")
                .clickOK(new FormEntryPage("g6Error"))
                .swipeToEndScreen()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);

        new MainMenuPage()
                .copyForm("g6Error2.xml")
                .startBlankForm("g6Error2")
                .inputText("bla")
                .swipeToNextQuestionWithError()
                .clickOK(new FormEntryPage("g6Error2"))
                .swipeToEndScreen()
                .inputText("ble")
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);

        new MainMenuPage()
                .copyForm("emptyGroupFieldList.xml")
                .clickFillBlankForm()
                .clickOnEmptyForm("emptyGroupFieldList")
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);

        new MainMenuPage()
                .copyForm("emptyGroupFieldList2.xml")
                .startBlankForm("emptyGroupFieldList2")
                .inputText("nana")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);
    }

    @Test
    public void user_ShouldBeAbleToFillTheForm() {
        //TestCase27
        rule.startAtMainMenu()
                .copyForm("metadata2.xml")
                .clickFillBlankForm()
                .clickOnEmptyForm("metadata2")
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok);
    }

    @Test
    public void question_ShouldBeVisibleOnTheTopOfHierarchy() {
        //TestCase23
        rule.startAtMainMenu()
                .copyForm("manyQ.xml")
                .startBlankForm("manyQ")
                .swipeToNextQuestion("t2")
                .swipeToNextQuestion("n1")
                .clickGoToArrow()
                .assertText("n1")
                .assertTextDoesNotExist("t1")
                .assertTextDoesNotExist("t2");
    }

    @Test
    public void bigForm_ShouldBeFilledSuccessfully() {
        //TestCase18
        rule.startAtMainMenu()
                .copyForm("nigeria-wards.xml")
                .startBlankForm("Nigeria Wards")
                .assertQuestion("State")
                .openSelectMinimalDialog()
                .clickOnText("Adamawa")
                .swipeToNextQuestion("LGA", true)
                .openSelectMinimalDialog()
                .clickOnText("Ganye")
                .swipeToNextQuestion("Ward", true)
                .openSelectMinimalDialog()
                .clickOnText("Jaggu")
                .swipeToNextQuestion("Comments")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void questionValidation_ShouldShowToastOnlyWhenConditionsAreNotMet() {
        //TestCase43
        rule.startAtMainMenu()
                .copyForm("t21257.xml")
                .startBlankForm("t21257")
                .clickOnText("mytext1")
                .inputText("test")
                .swipeToNextQuestion("mydecimal")
                .inputText("17")
                .closeSoftKeyboard()
                .swipeToNextQuestionWithConstraintViolation("mydecimal constraint")
                .inputText("117")
                .closeSoftKeyboard()
                .swipeToNextQuestionWithConstraintViolation("mydecimal constraint")
                .inputText("50")
                .closeSoftKeyboard()
                .swipeToNextQuestion("mynumbers")
                .inputText("16")
                .closeSoftKeyboard()
                .swipeToNextQuestionWithConstraintViolation("mynumbers constraint")
                .inputText("116")
                .closeSoftKeyboard()
                .swipeToNextQuestionWithConstraintViolation("mynumbers constraint")
                .inputText("51")
                .closeSoftKeyboard()
                .swipeToNextQuestion("mytext2")
                .inputText("test2")
                .swipeToNextQuestion("myselect")
                .swipeToEndScreen();
    }

    @Test
    public void noDataLost_ShouldRememberAnswersForMultiSelectWidget() {
        //TestCase44
        rule.startAtMainMenu()
                .copyForm("test_multiselect_cleared.xml")
                .startBlankForm("test_multiselect_cleared")
                .clickOnText("a")
                .clickOnText("c")
                .swipeToNextQuestion("If you go back, the answers are deleted if you selected more than 1 option.")
                .swipeToNextQuestion("choice2", true)
                .clickOnText("b")
                .clickOnText("d")
                .swipeToEndScreen()
                .swipeToPreviousQuestion("choice2", true)
                .swipeToPreviousQuestion("If you go back, the answers are deleted if you selected more than 1 option.")
                .swipeToPreviousQuestion("choice1", true)
                .clickGoToArrow()
                .assertText("a, c")
                .assertText("b, d")
                .clickJumpEndButton()
                .clickGoToArrow();
    }

    @Test
    public void typeMismatchErrorMessage_shouldBeDisplayed() {
        //TestCase48
        rule.startAtMainMenu()
                .copyForm("validate.xml")
                .startBlankForm("validate")
                .clearTheText("2019")
                .swipeToNextQuestionWithError()
                .checkIsTextDisplayedOnDialog("The value \"-01-01\" can't be converted to a date.");
    }

    @Test
    public void answers_shouldBeAutoFilled() {
        //TestCase50
        rule.startAtMainMenu()
                .copyForm("event-odk-new-repeat.xml")
                .startBlankForm("Event: odk-new-repeat")
                .inputText("3")
                .swipeToNextQuestionWithRepeatGroup("null")
                .clickOnAdd(new FormEntryPage("Event: odk-new-repeat"))
                .assertText("1")
                .swipeToNextQuestion("B value")
                .assertText("5")
                .swipeToNextQuestionWithRepeatGroup("null")
                .clickOnAdd(new FormEntryPage("Event: odk-new-repeat"))
                .assertText("2")
                .swipeToNextQuestion("B value")
                .assertText("5")
                .swipeToNextQuestionWithRepeatGroup("null")
                .clickOnAdd(new FormEntryPage("Event: odk-new-repeat"))
                .assertText("3")
                .swipeToNextQuestion("B value")
                .assertText("5")
                .swipeToNextQuestionWithRepeatGroup("null")
                .clickOnAdd(new FormEntryPage("Event: odk-new-repeat"))
                .assertText("4")
                .swipeToNextQuestion("B value")
                .assertText("5")
                .swipeToNextQuestionWithRepeatGroup("null")
                .clickOnDoNotAdd(new FormEntryPage("Event: odk-new-repeat"))
                .inputText("2")
                .swipeToNextQuestion("A value")
                .assertText("1")
                .swipeToNextQuestion("A value")
                .assertText("2")
                .swipeToNextQuestion("C value")
                .swipeToNextQuestion("C value")
                .swipeToNextQuestionWithRepeatGroup("null")
                .clickOnDoNotAdd(new FormEndPage("Event: odk-new-repeat"))
                .clickSaveAndExit();
    }

    @Test
    public void questions_shouldHavePrefilledValue() {
        //TestCase51
        rule.startAtMainMenu()
                .copyForm("multiple-events.xml")
                .startBlankForm("Space-separated event list")
                .assertText("cheese")
                .swipeToNextQuestion("First load group")
                .assertText("more cheese")
                .swipeToNextQuestion("My value")
                .assertText("5")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void when_chooseAnswer_should_beVisibleInNextQuestion() {
        //TestCase52
        rule.startAtMainMenu()
                .copyForm("CalcTest.xml")
                .startBlankFormWithRepeatGroup("CalcTest", "Fishing gear type")
                .clickOnAdd(new FormEntryPage("CalcTest"))
                .clickOnText("Gillnet")
                .swipeToNextQuestion("7.2 What is the size of the mesh for the Gillnet ?", true)
                .swipeToPreviousQuestion("7.1 Select the type of fishing equipment used today to catch the fish present", true)
                .clickOnText("Seinenet")
                .swipeToNextQuestion("7.2 What is the size of the mesh for the Seinenet ?", true);
    }

    @Test
    public void when_scrollQuestionsList_should_questionsNotDisappear() {
        //TestCase54
        rule.startAtMainMenu()
                .copyForm("3403.xml", asList("staff_list.csv", "staff_rights.csv"))
                .startBlankForm("3403_ODK Version 1.23.3 Tester")
                .clickOnText("New Farmer Registration")
                .scrollToAndClickText("Insemination")
                .scrollToAndAssertText("New Farmer Registration");
    }

    @Test
    public void missingFileMessage_shouldBeDisplayedIfExternalFIleIsMissing() {
        String formsDirPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS);

        //TestCase55
        rule.startAtMainMenu()
                .copyForm("search_and_select.xml")
                .startBlankForm("search_and_select")
                .assertText("File: " + formsDirPath + "/search_and_select-media/nombre.csv is missing.")
                .assertText("File: " + formsDirPath + "/search_and_select-media/nombre2.csv is missing.")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .copyForm("select_one_external.xml")
                .startBlankForm("cascading select test")
                .clickOnText("Texas")
                .swipeToNextQuestion()
                .assertText("File: " + formsDirPath + "/select_one_external-media/itemsets.csv is missing.")
                .swipeToNextQuestion()
                .assertText("File: " + formsDirPath + "/select_one_external-media/itemsets.csv is missing.")
                .swipeToNextQuestion(3)
                .swipeToEndScreen()
                .clickSaveAndExit()

                .copyForm("fieldlist-updates_nocsv.xml")
                .startBlankForm("fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnElementInHierarchy(14)
                .clickOnQuestion("Source15")
                .assertText("File: " + formsDirPath + "/fieldlist-updates_nocsv-media/fruits.csv is missing.")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void changedName_shouldNotDisappearAfterScreenRotation() {
        //TestCase13
        rule.startAtMainMenu()
                .copyForm("All_widgets.xml")
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickOnId(R.id.save_name)
                .inputText("submission")
                .closeSoftKeyboard()
                .rotateToLandscape(new FormEntryPage("All widgets"))
                .assertText("submission")
                .rotateToPortrait(new FormEntryPage("All widgets"))
                .assertText("submission");
    }

    @Test
    public void groups_shouldBeVisibleInHierarchyView() {
        //TestCase28
        rule.startAtMainMenu()
                .copyForm("nested-repeats-complex.xml")
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
        rule.startAtMainMenu()
                .copyForm("repeat_group_form.xml")
                .startBlankFormWithRepeatGroup("Repeat Group", "Grp1")
                .clickOnDoNotAdd(new FormEntryPage("Repeat Group"))
                .clickGoToArrow()
                .clickGoUpIcon()
                .checkIfElementInHierarchyMatchesToText("Group Name", 0)
                .rotateToLandscape(new FormEntryPage("Repeat Group"))
                .checkIfElementInHierarchyMatchesToText("Group Name", 0)
                .rotateToPortrait(new FormEntryPage("Repeat Group"))
                .checkIfElementInHierarchyMatchesToText("Group Name", 0);
    }

    @Test
    public void when_openHierarchyViewFromLastPage_should_mainGroupViewBeVisible() {
        //TestCase30
        rule.startAtMainMenu()
                .copyForm("repeat_group_form.xml")
                .startBlankFormWithRepeatGroup("Repeat Group", "Grp1")
                .clickOnDoNotAdd(new FormEntryPage("Repeat Group"))
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickGoToArrow()
                .checkIfElementInHierarchyMatchesToText("Group Name", 0);
    }

    private String getQuestionText() {
        FormEntryActivity formEntryActivity = (FormEntryActivity) ActivityHelpers.getActivity();
        FrameLayout questionContainer = formEntryActivity.findViewById(R.id.text_container);
        TextView questionView = (TextView) questionContainer.getChildAt(0);
        return questionView.getText().toString();
    }
}
