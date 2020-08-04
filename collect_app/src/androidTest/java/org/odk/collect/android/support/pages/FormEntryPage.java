package org.odk.collect.android.support.pages;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matchers;
import org.odk.collect.android.R;
import org.odk.collect.android.support.ActivityHelpers;
import org.odk.collect.android.utilities.FlingRegister;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.odk.collect.android.support.CustomMatchers.withIndex;
import static org.odk.collect.android.support.actions.RecyclerViewAction.clickItemWithId;
import static org.odk.collect.android.support.assertions.RecyclerViewAssertions.isItemChecked;
import static org.odk.collect.android.support.assertions.RecyclerViewAssertions.isItemNotChecked;
import static org.odk.collect.android.support.matchers.RecyclerViewMatcher.withRecyclerView;

public class FormEntryPage extends Page<FormEntryPage> {

    private final String formName;

    public FormEntryPage(String formName, ActivityTestRule rule) {
        super(rule);
        this.formName = formName;
    }

    @Override
    public FormEntryPage assertOnPage() {
        assertToolbarTitle(formName);
        return this;
    }

    /**
     * @deprecated use {@link #swipeToNextQuestion(String)} instead
     */
    @Deprecated
    public FormEntryPage swipeToNextQuestion() {
        flingLeft();
        return this;
    }

    public FormEntryPage swipeToNextQuestion(String questionText) {
        return swipeToNextQuestion(questionText, false);
    }

    public FormEntryPage swipeToNextQuestion(String questionText, boolean isRequired) {
        flingLeft();

        if (isRequired) {
            waitForText("* " + questionText);
        } else {
            waitForText(questionText);
        }

        return this;
    }

    /**
     * @deprecated use {@link #swipeToNextQuestion(String)} instead
     */
    @Deprecated
    public FormEntryPage swipeToNextQuestion(int repetitions) {
        for (int i = 0; i < repetitions; i++) {
            swipeToNextQuestion();
        }
        return this;
    }

    public FormEntryPage swipeToNextRepeat(String repeatLabel, int repeatNumber) {
        waitForText(repeatLabel + " > " + (repeatNumber - 1));
        flingLeft();
        waitForText(repeatLabel + " > " + repeatNumber);
        return this;
    }

    public FormEndPage swipeToEndScreen() {
        flingLeft();
        return waitFor(() -> new FormEndPage(formName, rule).assertOnPage());
    }

    public ErrorDialog swipeToNextQuestionWithError() {
        flingLeft();
        return new ErrorDialog(rule).assertOnPage();
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

    /**
     * @deprecated use {@link #swipeToPreviousQuestion(String)} instead
     */
    public FormEntryPage swipeToPreviousQuestion() {
        onView(withId(R.id.questionholder)).perform(swipeRight());
        return this;
    }

    public FormEntryPage swipeToPreviousQuestion(String questionText) {
        onView(withId(R.id.questionholder)).perform(swipeRight());
        assertText(questionText);
        return this;
    }

    public FormHierarchyPage clickGoToArrow() {
        onView(withId(R.id.menu_goto)).perform(click());
        return new FormHierarchyPage(formName, rule).assertOnPage();
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

    public FormEntryPage deleteGroup(String questionText) {
        onView(withText(questionText)).perform(longClick());
        onView(withText(R.string.delete_repeat)).perform(click());
        onView(withText(R.string.discard_group)).perform(click());
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

    public FormEndPage clickForwardButtonToEndScreen() {
        onView(withText(getTranslatedString(R.string.form_forward))).perform(click());
        return new FormEndPage(formName, rule).assertOnPage();
    }

    public FormEntryPage clickBackwardButton() {
        onView(withText(getTranslatedString(R.string.form_backward))).perform(click());
        return this;
    }

    public FormEntryPage clickOnDoNotAddGroup() {
        clickOnString(R.string.dont_add_repeat);
        return this;
    }

    public FormEndPage clickOnDoNotAddGroupEndingForm() {
        clickOnString(R.string.dont_add_repeat);
        return new FormEndPage(formName, rule).assertOnPage();
    }

    public FormEntryPage clickOnAddGroup() {
        clickOnString(R.string.add_repeat);
        return this;
    }

    public FormEntryPage checkIfImageViewIsDisplayed() {
        onView(withTagValue(is("ImageView"))).check(matches(isDisplayed()));
        return this;
    }

    public FormEntryPage checkIfImageViewIsNotDisplayed() {
        onView(withTagValue(is("ImageView"))).check(doesNotExist());
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

    public AddNewRepeatDialog clickPlus(String repeatName) {
        onView(withId(R.id.menu_add_repeat)).perform(click());
        return new AddNewRepeatDialog(repeatName, rule).assertOnPage();
    }

    public FormEntryPage longPressOnView(int id, int index) {
        onView(withIndex(withId(id), index)).perform(longClick());
        return this;
    }

    public FormEntryPage longPressOnView(String text) {
        onView(withText(text)).perform(longClick());
        return this;
    }

    public FormEntryPage removeResponse() {
        onView(withText(R.string.clear_answer)).perform(click());
        onView(withText(R.string.discard_answer)).perform(click());
        return this;
    }

    public AddNewRepeatDialog swipeToNextQuestionWithRepeatGroup(String repeatName) {
        flingLeft();
        return waitFor(() -> new AddNewRepeatDialog(repeatName, rule).assertOnPage());
    }

    public FormEntryPage answerQuestion(String question, String answer) {
        assertText(question);
        inputText(answer);
        closeSoftKeyboard();
        return this;
    }

    public FormEntryPage assertQuestion(String text) {
        waitForText(text);
        return this;
    }

    private void flingLeft() {
        tryAgainOnFail(() -> {
            FlingRegister.attemptingFling();
            onView(withId(R.id.questionholder)).perform(swipeLeft());

            waitFor(() -> {
                if (FlingRegister.isFlingDetected()) {
                    return true;
                } else {
                    throw new RuntimeException("Fling never detected!");
                }
            });
        }, 5);
    }

    public FormEntryPage openSelectMinimalDialog() {
        openSelectMinimalDialog(0);
        return this;
    }

    public FormEntryPage openSelectMinimalDialog(int index) {
        onView(withIndex(withClassName(Matchers.endsWith("TextInputEditText")), index)).perform(click());
        return this;
    }

    public FormEntryPage closeSelectMinimalDialog() {
        onView(allOf(instanceOf(AppCompatImageButton.class), withParent(withId(R.id.toolbar)))).perform(click());
        return this;
    }

    public FormEntryPage assertSelectMinimalDialogAnswer(String answer) {
        onView(withId(R.id.choices_search_box)).check(matches(withText(answer)));
        return this;
    }

    public FormEntryPage assertSearchBoxIsHidden(boolean minimalAppearance) {
        if (minimalAppearance) {
            onView(withId(R.id.search_src_text)).check(doesNotExist());
        } else {
            onView(withId(R.id.choices_search_box)).check(matches(not(isDisplayed())));
        }
        return this;
    }

    public FormEntryPage assertSearchBoxIsVisible(boolean minimalMode) {
        if (minimalMode) {
            onView(withId(R.id.search_src_text)).check(matches(isDisplayed()));
        } else {
            onView(withId(R.id.choices_search_box)).check(matches(isDisplayed()));
        }
        return this;
    }

    public FormEntryPage assertFileNotFoundMsg(String fileName) {
        onView(withText(getTranslatedString(R.string.file_missing, fileName))).check(matches(isDisplayed()));
        return this;
    }

    public FormEntryPage assertFileNotFoundToast(String fileName) {
        checkIsToastWithMessageDisplayed(R.string.file_missing, fileName);
        return this;
    }

    public FormEntryPage clickAudioButton(int position) {
        onView(withId(R.id.choices_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(position, clickItemWithId(R.id.audioButton)));
        return this;
    }

    public FormEntryPage clickImageButton(int position) {
        onView(withId(R.id.choices_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(position, clickItemWithId(R.id.imageView)));
        return this;
    }

    public FormEntryPage clickVideoButton(int position) {
        onView(withId(R.id.choices_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(position, clickItemWithId(R.id.videoButton)));
        return this;
    }

    public FormEntryPage assertItemLabel(int position, String label) {
        onView(withRecyclerView(R.id.choices_recycler_view).atPositionOnView(position, R.id.text_label)).check(matches(withText(label)));
        return this;
    }

    public FormEntryPage assertItemChecked(int position, boolean noButtonsMode) {
        if (noButtonsMode) {
            onView(withRecyclerView(R.id.choices_recycler_view).atPositionOnView(position, R.id.text_label)).check(isItemChecked());
        } else {
            onView(withRecyclerView(R.id.choices_recycler_view).atPositionOnView(position, R.id.text_label)).check(matches(isChecked()));
        }
        return this;
    }

    public FormEntryPage assertItemNotChecked(int position, boolean noButtonsMode) {
        if (noButtonsMode) {
            onView(withRecyclerView(R.id.choices_recycler_view).atPositionOnView(position, R.id.text_label)).check(isItemNotChecked());
        } else {
            onView(withRecyclerView(R.id.choices_recycler_view).atPositionOnView(position, R.id.text_label)).check(matches(isNotChecked()));
        }
        return this;
    }

    public FormEntryPage filterChoices(String text, boolean minimalMode) {
        if (minimalMode) {
            onView(withId(androidx.appcompat.R.id.search_src_text)).perform(replaceText(text));
        } else {
            onView(withId(R.id.choices_search_box)).perform(replaceText(text));
        }
        return this;
    }
}
