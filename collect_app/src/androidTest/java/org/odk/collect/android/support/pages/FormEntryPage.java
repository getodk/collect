package org.odk.collect.android.support.pages;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.support.ActivityHelpers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.odk.collect.android.test.CustomMatchers.withIndex;

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

    public FormEntryPage clickJumpEndButton() {
        onView(withId(R.id.jumpEndButton)).perform(click());
        return this;
    }

    public MainMenuPage clickSaveAndExit() {
        onView(withId(R.id.save_exit_button)).perform(click());
        return new MainMenuPage(rule).assertOnPage();
    }

    public FormMapPage clickSaveAndExitBackToMap() {
        onView(withId(R.id.save_exit_button)).perform(click());
        return new FormMapPage(rule).assertOnPage();
    }

    public FormEntryPage clickSaveAndExitWithError() {
        onView(withId(R.id.save_exit_button)).perform(click());
        return this;
    }

    public FormEntryPage swipeToNextQuestion() {
        onView(withId(R.id.questionholder)).perform(swipeLeft());
        return this;
    }

    public FormEntryPage swipeToNextQuestion(int repetitions) {
        for (int i = 0; i < repetitions; i++) {
            swipeToNextQuestion();
        }
        return this;
    }

    public ErrorDialog swipeToNextQuestionWithError() {
        onView(withId(R.id.questionholder)).perform(swipeLeft());
        return new ErrorDialog(rule);
    }

    public FormEntryPage clickOptionsIcon() {
        Espresso.openActionBarOverflowOrOptionsMenu(ActivityHelpers.getActivity());
        return this;
    }

    public FormEntryPage clickOnLaunchButton() {
        onView(withText(getTranslatedString(R.string.launch_app))).perform(click());
        return this;
    }

    public GeneralSettingsPage clickGeneralSettings() {
        onView(withText(getTranslatedString(R.string.general_preferences))).perform(click());
        return new GeneralSettingsPage(rule).assertOnPage();
    }

    public FormEntryPage checkAreNavigationButtonsDisplayed() {
        onView(withId(R.id.form_forward_button)).check(matches(isDisplayed()));
        onView(withId(R.id.form_back_button)).check(matches(isDisplayed()));
        return this;
    }

    public FormEntryPage swipeToPreviousQuestion() {
        onView(withId(R.id.questionholder)).perform(swipeRight());
        return this;
    }

    public FormEntryPage clickGoToArrow() {
        onView(withId(R.id.menu_goto)).perform(click());
        return this;
    }

    public FormEntryPage swipeOnText(String text) {
        onView(withClassName(endsWith("EditText"))).check(matches(withText(text))).perform(swipeLeft());
        return this;
    }

    public FormEntryPage clickWidgetButton() {
        onView(withId(R.id.simple_button)).perform(click());
        return this;
    }

    public FormEntryPage clickRankingButton() {
        onView(withId(R.id.simple_button)).perform(click());
        return this;
    }

    public FormEntryPage putTextOnIndex(int index, String text) {
        onView(withIndex(withClassName(endsWith("Text")), index)).perform(replaceText(text));
        return this;
    }

    public FormEntryPage clickSaveAndExitWhenValidationErrorIsExpected() {
        onView(withId(R.id.save_exit_button)).perform(click());
        return this;
    }

    public FormEntryPage deleteGroup(String questionText) {
        onView(withText(questionText)).perform(longClick());
        onView(withText(R.string.delete_repeat)).perform(click());
        onView(withText(R.string.discard_group)).perform(click());
        return this;
    }

    public FormEntryPage deleteGroup() {
        onView(withId(R.id.menu_delete_child)).perform(click());
        onView(withText(R.string.delete_repeat)).perform(click());
        return this;
    }

    public FormEntryPage clickGoUpIcon() {
        onView(withId(R.id.menu_go_up)).perform(click());
        return this;
    }

    public FormEntryPage showSpinnerMultipleDialog() {
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.select_answer))).perform(click());
        return this;
    }

    public FormEntryPage clickGoToStart() {
        onView(withId(R.id.jumpBeginningButton)).perform(click());
        return this;
    }

    public FormEntryPage clickForwardButton() {
        onView(withText(getTranslatedString(R.string.form_forward))).perform(click());
        return this;
    }

    public FormEntryPage clickBackwardButton() {
        onView(withText(getTranslatedString(R.string.form_backward))).perform(click());
        return this;
    }

    public FormEntryPage clickOnDoNotAddGroup() {
        clickOnString(R.string.dont_add_repeat);
        return this;
    }

    public FormEntryPage clickOnAddGroup() {
        clickOnString(R.string.add_repeat);
        return this;
    }

    public FormEntryPage assertMarkFinishedIsSelected() {
        onView(withId(R.id.mark_finished)).check(matches(isChecked()));
        return this;
    }

    public FormEntryPage assertMarkFinishedIsNotSelected() {
        onView(withId(R.id.mark_finished)).check(matches(isNotChecked()));
        return this;
    }

    public FormEntryPage clickMarkAsFinalized() {
        onView(withId(R.id.mark_finished)).perform(click());
        return this;
    }

    public ChangesReasonPromptPage clickSaveAndExitWithChangesReasonPrompt() {
        onView(withId(R.id.save_exit_button)).perform(click());
        return new ChangesReasonPromptPage(formName, rule).assertOnPage();
    }

    public ChangesReasonPromptPage clickSaveWithChangesReasonPrompt() {
        onView(withId(R.id.menu_save)).perform(click());
        return new ChangesReasonPromptPage(formName, rule).assertOnPage();
    }

    public FormEntryPage checkBackNavigationButtonIsNotsDisplayed() {
        onView(withId(R.id.form_back_button)).check(matches(not(isDisplayed())));
        return this;
    }

    public FormEntryPage checkNextNavigationButtonIsDisplayed() {
        onView(withId(R.id.form_forward_button)).check(matches(isDisplayed()));
        return this;
    }

    public FormEntryPage checkAreNavigationButtonsNotDisplayed() {
        onView(withId(R.id.form_forward_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.form_back_button)).check(matches(not(isDisplayed())));
        return this;
    }

    public FormEntryPage checkIsFormEndScreenVisible() {
        onView(withText(R.string.quit_entry)).check(matches(isDisplayed()));
        return this;
    }
}
