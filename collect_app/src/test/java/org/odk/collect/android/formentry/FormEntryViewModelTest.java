package org.odk.collect.android.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.audit.AuditEventLogger;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.utilities.Clock;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.formentry.FormEntryViewModel.NonFatal;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
public class FormEntryViewModelTest {

    private FormEntryViewModel viewModel;
    private FormController formController;
    private FormIndex startingIndex;

    @Before
    public void setup() {
        formController = mock(FormController.class);
        startingIndex = new FormIndex(null, 0, 0, new TreeReference());
        when(formController.getFormIndex()).thenReturn(startingIndex);
        when(formController.getFormDef()).thenReturn(new FormDef());

        AuditEventLogger auditEventLogger = mock(AuditEventLogger.class);
        when(formController.getAuditEventLogger()).thenReturn(auditEventLogger);

        viewModel = new FormEntryViewModel(mock(Clock.class));
        viewModel.formLoaded(formController);
    }

    @Test
    public void addRepeat_stepsToNextScreenEvent() throws Exception {
        viewModel.addRepeat();
        verify(formController).stepToNextScreenEvent();
    }

    @Test
    public void addRepeat_whenThereIsAnErrorCreatingRepeat_setsErrorWithMessage() {
        doThrow(new RuntimeException(new IOException("OH NO"))).when(formController).newRepeat();

        viewModel.addRepeat();
        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("OH NO")));
    }

    @Test
    public void addRepeat_whenThereIsAnErrorCreatingRepeat_setsErrorWithoutCause() {
        RuntimeException runtimeException = mock(RuntimeException.class);
        when(runtimeException.getCause()).thenReturn(null);
        when(runtimeException.getMessage()).thenReturn("Unknown issue occurred while adding a new group");

        doThrow(runtimeException).when(formController).newRepeat();

        viewModel.addRepeat();
        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("Unknown issue occurred while adding a new group")));
    }

    @Test
    public void addRepeat_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithMessage() throws Exception {
        when(formController.stepToNextScreenEvent()).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.addRepeat();
        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("OH NO")));
    }

    @Test
    public void addRepeat_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithoutCause() throws Exception {
        JavaRosaException javaRosaException = mock(JavaRosaException.class);
        when(javaRosaException.getCause()).thenReturn(null);
        when(javaRosaException.getMessage()).thenReturn("Unknown issue occurred while adding a new group");

        when(formController.stepToNextScreenEvent()).thenThrow(javaRosaException);

        viewModel.addRepeat();
        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("Unknown issue occurred while adding a new group")));
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

    @Test
    public void cancelRepeatPrompt_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithMessage() throws Exception {
        when(formController.stepToNextScreenEvent()).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.cancelRepeatPrompt();
        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("OH NO")));
    }
}
