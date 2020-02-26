package org.odk.collect.android.formentry;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.formentry.javarosawrapper.FormController;
import org.robolectric.RobolectricTestRunner;

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

        viewModel = new FormEntryViewModel(() -> formController, analytics);
    }

    @Test
    public void addRepeat_sendsAddRepeatPromptAnalyticsEvent() {
        viewModel.addRepeat();
        verify(analytics, only()).logEvent("AddRepeat", "Prompt");
    }

    @Test
    public void promptForNewRepeat_thenAddRepeat_sendsAddRepeatInlineAnalyticsEvent() {
        viewModel.promptForNewRepeat();
        viewModel.addRepeat();
        verify(analytics, only()).logEvent("AddRepeat", "Inline");
    }

    @Test
    public void promptForNewRepeat_thenCancelRepeatPrompt_sendsAddRepeatInlineDeclineAnalyticsEvent() {
        viewModel.promptForNewRepeat();
        viewModel.cancelRepeatPrompt();
        verify(analytics, only()).logEvent("AddRepeat", "InlineDecline");
    }

    @Test
    public void cancelRepeatPrompt_afterPromptForNewRepeatAndAddRepeat_doesNotJumpBack() {
        viewModel.promptForNewRepeat();
        viewModel.addRepeat();

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
}
