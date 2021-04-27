package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class FormEndPage extends Page<FormEndPage> {

    private final String formName;

    public FormEndPage(String formName) {
        this.formName = formName;
    }

    @Override
    public FormEndPage assertOnPage() {
        onView(withText(getTranslatedString(R.string.save_enter_data_description, formName))).check(matches(isDisplayed()));
        return this;
    }

    public MainMenuPage clickSaveAndExit() {
        onView(withId(R.id.save_exit_button)).perform(click());
        return new MainMenuPage().assertOnPage();
    }

    public FormMapPage clickSaveAndExitBackToMap() {
        onView(withId(R.id.save_exit_button)).perform(click());
        return new FormMapPage().assertOnPage();
    }

    public FormEntryPage clickSaveAndExitWithError() {
        onView(withId(R.id.save_exit_button)).perform(click());
        return new FormEntryPage(formName).assertOnPage();
    }

    public OkDialog clickSaveAndExitWithErrorDialog() {
        onView(withId(R.id.save_exit_button)).perform(click());
        return new OkDialog().assertOnPage();
    }

    public ChangesReasonPromptPage clickSaveAndExitWithChangesReasonPrompt() {
        onView(withId(R.id.save_exit_button)).perform(click());
        return new ChangesReasonPromptPage(formName).assertOnPage();
    }

    public FormEndPage assertMarkFinishedIsSelected() {
        onView(withId(R.id.mark_finished)).check(matches(isChecked()));
        return this;
    }

    public FormEndPage assertMarkFinishedIsNotSelected() {
        onView(withId(R.id.mark_finished)).check(matches(isNotChecked()));
        return this;
    }

    public FormEndPage clickMarkAsFinalized() {
        onView(withId(R.id.mark_finished)).perform(click());
        return this;
    }

    public FormHierarchyPage clickGoToArrow() {
        onView(withId(R.id.menu_goto)).perform(click());
        return new FormHierarchyPage(formName);
    }

    public FormEntryPage swipeToPreviousQuestion(String questionText) {
        return new FormEntryPage(formName).swipeToPreviousQuestion(questionText);
    }

    public FormEntryPage swipeToPreviousQuestion(String questionText, boolean isRequired) {
        return new FormEntryPage(formName).swipeToPreviousQuestion(questionText, isRequired);
    }

    public FormEndPage fillInFormName(String formName) {
        inputText(formName);
        return this;
    }
}
