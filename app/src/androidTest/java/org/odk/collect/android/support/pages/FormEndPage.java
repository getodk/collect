package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Build;

import org.odk.collect.android.R;

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

    public <D extends Page<D>> D clickSaveAndExit(D destination) {
        onView(withId(R.id.save_exit_button)).perform(click());
        return destination.assertOnPage();
    }

    public MainMenuPage clickSaveAndExit() {
        return clickSaveAndExit(new MainMenuPage());
    }

    public FormMapPage clickSaveAndExitBackToMap() {
        return clickSaveAndExit(new FormMapPage(formName));
    }

    public FormEntryPage clickSaveAndExitWithError(String errorText) {
        onView(withId(R.id.save_exit_button)).perform(click());
        assertConstraintDisplayed(errorText);
        return new FormEntryPage(formName).assertOnPage();
    }

    public OkDialog clickSaveAndExitWithErrorDialog() {
        return clickSaveAndExit(new OkDialog());
    }

    public ChangesReasonPromptPage clickSaveAndExitWithChangesReasonPrompt() {
        return clickSaveAndExit(new ChangesReasonPromptPage(formName));
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

    private void assertConstraintDisplayed(String constraintText) {
        // Constraints warnings show as dialogs in Android 11+
        if (Build.VERSION.SDK_INT < 30) {
            checkIsToastWithMessageDisplayed(constraintText);
        } else {
            new OkDialog().assertOnPage()
                    .assertText(constraintText)
                    .clickOK(new FormEntryPage(formName));
        }
    }
}
