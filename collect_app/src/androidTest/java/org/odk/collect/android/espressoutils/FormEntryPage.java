package org.odk.collect.android.espressoutils;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class FormEntryPage extends Page<FormEntryPage> {
    public FormEntryPage(ActivityTestRule rule) {
        super(rule);
    }

    public FormEntryPage clickOnGoToIconInForm() {
        FormEntry.clickGoToIconInForm();
        return this;
    }

    public FormEntryPage clickOnText(String text) {
        FormEntry.clickOnText(text);
        return this;
    }

    public FormEntryPage clickOnId(int id) {
        FormEntry.clickOnId(id);
        return this;
    }

    public FormEntryPage clickOnString(int stringID) {
        FormEntry.clickOnString(stringID);
        return this;
    }

    public FormEntryPage clickJumpEndButton() {
        FormEntry.clickJumpEndButton();
        return this;
    }

    public MainMenuPage clickSaveAndExit() {
        FormEntry.clickSaveAndExit();
        return new MainMenuPage(rule);
    }

    public FormEntryPage checkIsIdDisplayed(int id) {
        FormEntry.checkIsIdDisplayed(id);
        return this;
    }

    public FormEntryPage swipeToNextQuestion() {
        FormEntry.swipeToNextQuestion();
        return this;
    }

    public FormEntryPage clickOptionsIcon() {
        FormEntry.clickOptionsIcon();
        return this;
    }
    
    public void clickGeneralSettings() {
        onView(withText(getString(R.string.general_preferences))).perform(click());
    }

    public FormEntryPage checkAreNavigationButtonsDisplayed() {
        FormEntry.checkAreNavigationButtonsDisplayed();
        return this;
    }

    public FormEntryPage putText(String text) {
        FormEntry.putText(text);
        return this;
    }

    public FormEntryPage swipeToPreviousQuestion() {
        FormEntry.swipeToPrevoiusQuestion();
        return this;
    }

    public FormEntryPage clickGoToIconInForm() {
        FormEntry.clickGoToIconInForm();
        return this;
    }

    public FormEntryPage clickOk() {
        FormEntry.clickOk();
        return this;
    }
}
