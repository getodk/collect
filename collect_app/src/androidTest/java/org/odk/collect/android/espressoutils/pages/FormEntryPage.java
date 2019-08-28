package org.odk.collect.android.espressoutils.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.FormEntry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class FormEntryPage extends Page<FormEntryPage> {

    private final String formName;

    public FormEntryPage(String formName, ActivityTestRule rule) {
        super(rule);
        this.formName = formName;
    }

    @Override
    public FormEntryPage assertOnPage() {
        onView(allOf(withText(formName), isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()));
        return this;
    }

    public FormEntryPage clickOnGoToIconInForm() {
        FormEntry.clickGoToIconInForm();
        return this;
    }

    public FormEntryPage clickJumpEndButton() {
        FormEntry.clickJumpEndButton();
        return this;
    }

    public MainMenuPage clickSaveAndExit() {
        FormEntry.clickSaveAndExit();
        return new MainMenuPage(rule).assertOnPage();
    }

    public FormEntryPage swipeToNextQuestion() {
        onView(withId(R.id.questionholder)).perform(swipeLeft());
        return this;
    }

    public ErrorDialog swipeToNextQuestionWithError() {
        onView(withId(R.id.questionholder)).perform(swipeLeft());
        return new ErrorDialog(rule);
    }
    
    public FormEntryPage clickOptionsIcon() {
        FormEntry.clickOptionsIcon();
        return this;
    }

    public GeneralSettingsPage clickGeneralSettings() {
        onView(withText(getTranslatedString(R.string.general_preferences))).perform(click());
        return new GeneralSettingsPage(rule).assertOnPage();
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
        onView(withId(R.id.questionholder)).perform(swipeRight());
        return this;
    }

    public FormEntryPage clickGoToIconInForm() {
        FormEntry.clickGoToIconInForm();
        return this;
    }
}
