package org.odk.collect.android.formentry;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.javarosawrapper.FormController;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class FormEntryViewModelTest {

    private FormEntryViewModel viewModel;
    private Analytics analytics;
    private FormController formController;
    private FormIndex startingIndex;

    @Before
    public void setup() {
        analytics = mock(Analytics.class);

        formController = mock(FormController.class);
        startingIndex = new FormIndex(null, 0, 0, new TreeReference());
        when(formController.getFormIndex()).thenReturn(startingIndex);
        when(formController.getCurrentFormIdentifierHash()).thenReturn("formIdentifierHash");

        viewModel = new FormEntryViewModel(analytics);
        viewModel.formLoaded(formController);
    }

    @Test
    public void addRepeat_stepsToNextScreenEvent() throws Exception {
        viewModel.addRepeat(true);
        verify(formController).stepToNextScreenEvent();
    }

    @Test
    public void addRepeat_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithMessage() throws Exception {
        when(formController.stepToNextScreenEvent()).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.addRepeat(true);
        assertThat(viewModel.getError().getValue(), equalTo("OH NO"));
    }

    @Test
    public void addRepeat_sendsAddRepeatPromptAnalyticsEvent() {
        viewModel.addRepeat(true);
        verify(analytics, only()).logEvent(AnalyticsEvents.ADD_REPEAT, "Prompt", "formIdentifierHash");
    }

    @Test
    public void addRepeat_whenFromPromptIsFalse_sendsAddHierarchyAnalyticsEvent() {
        viewModel.addRepeat(false);
        verify(analytics, only()).logEvent(AnalyticsEvents.ADD_REPEAT, "Hierarchy", "formIdentifierHash");
    }

    @Test
    public void promptForNewRepeat_thenAddRepeat_sendsAddRepeatInlineAnalyticsEvent() {
        viewModel.promptForNewRepeat();
        viewModel.addRepeat(true);
        verify(analytics, only()).logEvent(AnalyticsEvents.ADD_REPEAT, "Inline", "formIdentifierHash");
    }

    @Test
    public void promptForNewRepeat_thenCancelRepeatPrompt_sendsAddRepeatInlineDeclineAnalyticsEvent() {
        viewModel.promptForNewRepeat();
        viewModel.cancelRepeatPrompt();
        verify(analytics, only()).logEvent(AnalyticsEvents.ADD_REPEAT, "InlineDecline", "formIdentifierHash");
    }

    @Test
    public void cancelRepeatPrompt_afterPromptForNewRepeatAndAddRepeat_doesNotJumpBack() {
        viewModel.promptForNewRepeat();
        viewModel.addRepeat(true);

        viewModel.cancelRepeatPrompt();
        verify(formController, never()).jumpToIndex(startingIndex);
    }

    @Test
    public void cancelRepeatPrompt_afterPromptForNewRepeatAndCancelRepeatPrompt_doesNotJumpBack() {
        viewModel.promptForNewRepeat();
        viewModel.cancelRepeatPrompt();
        verify(formController).jumpToIndex(startingIndex);

        viewModel.cancelRepeatPrompt();
        verify(formController, atMostOnce()).jumpToIndex(startingIndex);
    }

    @Test
    public void cancelRepeatPrompt_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithMessage() throws Exception {
        when(formController.stepToNextScreenEvent()).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.cancelRepeatPrompt();
        assertThat(viewModel.getError().getValue(), equalTo("OH NO"));
    }
}
