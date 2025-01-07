package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasFocus;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.odk.collect.android.support.matchers.CustomMatchers.isQuestionView;
import static org.odk.collect.android.support.matchers.CustomMatchers.withIndex;

import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.odk.collect.android.R;
import org.odk.collect.androidtest.DrawableMatcher;
import org.odk.collect.testshared.Interactions;
import org.odk.collect.testshared.ViewActions;
import org.odk.collect.testshared.WaitFor;

import java.util.concurrent.Callable;

public class FormEntryPage extends Page<FormEntryPage> {

    private final String formName;

    public FormEntryPage(String formName) {
        this.formName = formName;
    }

    @Override
    public FormEntryPage assertOnPage() {
        // Make sure we wait for loading to finish
        WaitFor.waitFor((Callable<Void>) () -> {
            assertTextDoesNotExist(org.odk.collect.strings.R.string.loading_form);
            return null;
        });

        WaitFor.waitFor((Callable<Void>) () -> {
            assertToolbarTitle(formName);
            return null;
        });

        // Check we are not on the Form Hierarchy page
        assertTextDoesNotExist(org.odk.collect.strings.R.string.jump_to_beginning);
        assertTextDoesNotExist(org.odk.collect.strings.R.string.jump_to_end);

        return this;
    }

    public FormEntryPage fillOut(QuestionAndAnswer... questionsAndAnswers) {
        FormEntryPage page = this;

        for (int i = 0; i < questionsAndAnswers.length; i++) {
            QuestionAndAnswer current = questionsAndAnswers[i];
            page = page.answerQuestion(current.question, current.isRequired, current.answer);

            if (i < questionsAndAnswers.length - 1) {
                QuestionAndAnswer next = questionsAndAnswers[i + 1];
                page = page.swipeToNextQuestion(next.question, current.isRequired);
            }
        }

        return page;
    }

    public <D extends Page<D>> D fillOutAndSave(D destination, QuestionAndAnswer... questionsAndAnswers) {
        return fillOut(questionsAndAnswers)
                .pressBack(new SaveOrDiscardFormDialog<>(destination))
                .clickSaveChanges();
    }

    public MainMenuPage fillOutAndSave(QuestionAndAnswer... questionsAndAnswers) {
        return fillOut(questionsAndAnswers)
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickSaveChanges();
    }

    public MainMenuPage fillOutAndFinalize(QuestionAndAnswer... questionsAndAnswers) {
        return fillOut(questionsAndAnswers)
                .swipeToEndScreen()
                .clickFinalize();
    }

    public FormEntryPage swipeToNextQuestion(String questionText) {
        return swipeToNextQuestion(questionText, false);
    }

    public FormEntryPage swipeToNextQuestion(String questionText, boolean isRequired) {
        flingLeft();

        if (isRequired) {
            assertQuestionText("* " + questionText);
        } else {
            assertQuestionText(questionText);
        }

        return this;
    }

    public FormEntryPage swipeToPreviousQuestion(String questionText) {
        return swipeToPreviousQuestion(questionText, false);
    }

    public FormEntryPage swipeToPreviousQuestion(String questionText, boolean isRequired) {
        onView(withId(R.id.questionholder)).perform(swipeRight());

        if (isRequired) {
            assertQuestionText("* " + questionText);
        } else {
            assertQuestionText(questionText);
        }

        return this;
    }

    public FormEntryPage swipeToNextRepeat(String repeatLabel, int repeatNumber) {
        waitForText(repeatLabel + " > " + (repeatNumber - 1));
        flingLeft();
        waitForText(repeatLabel + " > " + repeatNumber);
        return this;
    }

    public FormEndPage swipeToEndScreen(String instanceName) {
        flingLeft();
        return WaitFor.waitFor(() -> new FormEndPage(instanceName).assertOnPage());
    }

    public FormEndPage swipeToEndScreen() {
        flingLeft();
        return WaitFor.waitFor(() -> new FormEndPage(formName).assertOnPage());
    }

    public ErrorDialog swipeToNextQuestionWithError(boolean isFatal) {
        flingLeft();
        return new ErrorDialog().assertOnPage(isFatal);
    }

    public FormEntryPage swipeToNextQuestionWithConstraintViolation(int constraintText) {
        flingLeft();
        assertText(constraintText);

        return this;
    }

    public FormEntryPage swipeToNextQuestionWithConstraintViolation(String constraintText) {
        flingLeft();
        assertText(constraintText);

        return this;
    }

    private void assertQuestionText(String text) {
        onView(withIndex(withId(R.id.text_label), 0)).check(matches(withText(containsString(text))));
    }

    public FormEntryPage clickOptionsIcon() {
        return clickOptionsIcon(org.odk.collect.strings.R.string.project_settings);
    }

    public ProjectSettingsPage clickProjectSettings() {
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.project_settings))).perform(click());
        return new ProjectSettingsPage().assertOnPage();
    }

    public FormEntryPage assertNavigationButtonsAreDisplayed() {
        onView(withId(R.id.form_forward_button)).check(matches(isDisplayed()));
        onView(withId(R.id.form_back_button)).check(matches(isDisplayed()));
        return this;
    }

    public FormEntryPage assertNavigationButtonsAreHidden() {
        onView(withId(R.id.form_forward_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.form_back_button)).check(matches(not(isDisplayed())));
        return this;
    }

    public FormHierarchyPage clickGoToArrow() {
        onView(withId(R.id.menu_goto)).perform(click());
        return new FormHierarchyPage(formName).assertOnPage();
    }

    public FormEntryPage clickWidgetButton() {
        onView(withId(R.id.simple_button)).perform(click());
        return this;
    }

    public FormEntryPage clickRankingButton() {
        onView(withId(R.id.rank_items_button)).perform(click());
        return this;
    }

    public FormEntryPage deleteGroup(String questionText) {
        longClickOnText(questionText);
        clickOnTextInPopup(org.odk.collect.strings.R.string.delete_repeat);
        clickOnTextInDialog(org.odk.collect.strings.R.string.discard_group, this);
        return this;
    }

    public FormEntryPage clickForwardButton() {
        closeSoftKeyboard();
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.form_forward))).perform(click());
        return this;
    }

    public FormEndPage clickForwardButtonToEndScreen() {
        closeSoftKeyboard();
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.form_forward))).perform(click());
        return new FormEndPage(formName).assertOnPage();
    }

    public FormEntryPage clickBackwardButton() {
        closeSoftKeyboard();
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.form_backward))).perform(click());
        return this;
    }

    public FormEntryPage clickSave() {
        onView(withId(R.id.menu_save)).perform(click());
        return this;
    }

    public FormEntryPage clickSaveWithError(int errorMsg) {
        onView(withId(R.id.menu_save)).perform(click());

        if (Build.VERSION.SDK_INT < 30) {
            checkIsToastWithMessageDisplayed(errorMsg);
        } else {
            assertText(errorMsg);
            clickOKOnDialog();
        }

        return this;
    }

    public ChangesReasonPromptPage clickSaveWithChangesReasonPrompt() {
        onView(withId(R.id.menu_save)).perform(click());
        return new ChangesReasonPromptPage(formName).assertOnPage();
    }

    public AddNewRepeatDialog clickPlus(String repeatName) {
        onView(withId(R.id.menu_add_repeat)).perform(click());
        return new AddNewRepeatDialog(repeatName).assertOnPage();
    }

    public FormEntryPage longPressOnQuestion(int id, int index) {
        onView(withIndex(withId(id), index)).perform(longClick());
        return this;
    }

    public FormEntryPage longPressOnQuestion(String question) {
        longPressOnQuestion(question, false);
        return this;
    }

    public FormEntryPage longPressOnQuestion(String question, boolean isRequired) {
        WaitFor.tryAgainOnFail(() -> {
            if (isRequired) {
                onView(withText("* " + question)).perform(longClick());
            } else {
                onView(withText(question)).perform(longClick());
            }

            assertText(org.odk.collect.strings.R.string.clear_answer);
        });

        return this;
    }

    public FormEntryPage removeResponse() {
        onView(withText(org.odk.collect.strings.R.string.clear_answer)).perform(click());
        return clickOnTextInDialog(org.odk.collect.strings.R.string.discard_answer, this);
    }

    public AddNewRepeatDialog swipeToNextQuestionWithRepeatGroup(String repeatName) {
        flingLeft();
        return WaitFor.waitFor(() -> new AddNewRepeatDialog(repeatName).assertOnPage());
    }

    public AddNewRepeatDialog swipeToPreviousQuestionWithRepeatGroup(String repeatName) {
        flingRight();
        return WaitFor.waitFor(() -> new AddNewRepeatDialog(repeatName).assertOnPage());
    }

    public FormEntryPage answerQuestion(String question, String answer) {
        answerQuestion(question, false, answer);
        return this;
    }

    public FormEntryPage answerQuestion(String question, boolean isRequired, String answer) {
        String questionText;
        if (isRequired) {
            questionText = "* " + question;
        } else {
            questionText = question;
        }

        Interactions.replaceText(getQuestionFieldMatcher(questionText), answer);
        return this;
    }

    /**
     * @deprecated Use {@link #answerQuestion(String, String)} instead
     */
    @Deprecated
    public FormEntryPage answerQuestion(int index, String answer) {
        onView(withIndex(withClassName(endsWith("EditText")), index)).perform(scrollTo());
        onView(withIndex(withClassName(endsWith("EditText")), index)).perform(replaceText(answer));
        return this;
    }

    public FormEntryPage assertAnswer(String questionText, String answer) {
        onView(getQuestionFieldMatcher(questionText)).check(matches(withText(answer)));
        return this;
    }

    public FormEntryPage clickOnQuestionField(String questionText) {
        Interactions.clickOn(getQuestionFieldMatcher(questionText));
        return this;
    }

    public FormEntryPage assertQuestion(String text) {
        return assertQuestion(text, false);
    }

    public FormEntryPage assertQuestion(String text, boolean isRequired) {
        if (isRequired) {
            waitForText("* " + text);
        } else {
            waitForText(text);
        }

        return this;
    }

    public FormEntryPage assertNoQuestion(String text) {
        return assertNoQuestion(text, false);
    }

    public FormEntryPage assertNoQuestion(String text, boolean isRequired) {
        if (isRequired) {
            assertTextDoesNotExist("* " + text);
        } else {
            assertTextDoesNotExist(text);
        }

        return this;
    }

    private void flingLeft() {
        tryFlakyAction(() -> {
            onView(withId(R.id.questionholder)).perform(swipeLeft());
        });
    }

    private void flingRight() {
        tryFlakyAction(() -> {
            onView(withId(R.id.questionholder)).perform(swipeRight());
        });
    }

    public SelectMinimalDialogPage openSelectMinimalDialog() {
        return openSelectMinimalDialog(0);
    }

    public SelectMinimalDialogPage openSelectMinimalDialog(int index) {
        onView(withIndex(withClassName(Matchers.endsWith("TextInputEditText")), index)).perform(click());
        return new SelectMinimalDialogPage(formName).assertOnPage();
    }

    public FormEntryPage assertSelectMinimalDialogAnswer(@Nullable String answer) {
        if (answer == null) {
            onView(withId(R.id.answer)).check(matches(withText(org.odk.collect.strings.R.string.select_answer)));
        } else {
            onView(withId(R.id.answer)).check(matches(withText(answer)));
        }
        return this;
    }

    public OkDialog swipeToEndScreenWhileRecording() {
        flingLeft();
        OkDialog okDialog = new OkDialog().assertOnPage();
        assertText(org.odk.collect.strings.R.string.recording_warning);
        return okDialog;
    }

    public CancelRecordingDialog clickRecordAudio() {
        clickOnString(org.odk.collect.strings.R.string.record_audio_on);
        return new CancelRecordingDialog(formName);
    }

    public MainMenuPage pressBackAndDiscardChanges() {
        return closeSoftKeyboard()
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickDiscardChanges();
    }

    public <D extends Page<D>> D pressBackAndDiscardChanges(D destination) {
        return closeSoftKeyboard()
                .pressBack(new SaveOrDiscardFormDialog<>(destination))
                .clickDiscardChanges();
    }

    public MainMenuPage pressBackAndSaveAsDraft() {
        return closeSoftKeyboard()
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickSaveChanges();
    }

    public MainMenuPage pressBackAndDiscardForm() {
        return closeSoftKeyboard()
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickDiscardForm();
    }

    public <D extends Page<D>> D pressBackAndDiscardForm(D destination) {
        return closeSoftKeyboard()
                .pressBack(new SaveOrDiscardFormDialog<D>(destination))
                .clickDiscardForm();
    }

    public FormEntryPage assertBackgroundLocationSnackbarShown() {
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText(String.format(ApplicationProvider.getApplicationContext().getString(org.odk.collect.strings.R.string.background_location_enabled), "⋮"))));
        return this;
    }

    public FormEntryPage setRating(float value) {
        onView(allOf(withId(R.id.rating_bar1), isDisplayed())).perform(ViewActions.setRating(value));
        return this;
    }

    public FormEntryPage assertQuestionsOrder(String questionAbove, String questionBelow) {
        onView(withText(questionBelow)).check(isCompletelyBelow(withText(questionAbove)));
        return this;
    }

    public FormEntryPage assertQuestionHasFocus(String questionText) {
        onView(getQuestionFieldMatcher(questionText)).check(matches(isCompletelyDisplayed()));
        onView(getQuestionFieldMatcher(questionText)).check(matches(hasFocus()));
        return this;
    }

    private static @NonNull Matcher<View> getQuestionFieldMatcher(String question) {
        return allOf(
                withClassName(endsWith("EditText")),
                isDescendantOfA(isQuestionView(question))
        );
    }

    public FormEntryPage assertImageViewShowsImage(int resourceid, Bitmap image) {
        onView(withId(resourceid)).check(matches(DrawableMatcher.withBitmap(image)));
        return this;
    }

    public static class QuestionAndAnswer {

        private final String question;
        private final String answer;
        private final boolean isRequired;

        public QuestionAndAnswer(String question, String answer) {
            this(question, answer, false);
        }

        public QuestionAndAnswer(String question, String answer, boolean isRequired) {
            this.question = question;
            this.answer = answer;
            this.isRequired = isRequired;
        }
    }
}
