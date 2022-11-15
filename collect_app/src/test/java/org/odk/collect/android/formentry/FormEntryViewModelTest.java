package org.odk.collect.android.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.formentry.FormEntryViewModel.NonFatal;
import static org.odk.collect.androidtest.LiveDataTestUtilsKt.getOrAwaitValue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.audit.AuditEventLogger;
import org.odk.collect.android.formentry.support.InMemFormSessionRepository;
import org.odk.collect.android.javarosawrapper.FailedConstraint;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.testshared.FakeScheduler;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
public class FormEntryViewModelTest {

    private FormEntryViewModel viewModel;
    private FormController formController;
    private FormIndex startingIndex;
    private AuditEventLogger auditEventLogger;
    private FakeScheduler scheduler;
    private final FormSessionRepository formSessionRepository = new InMemFormSessionRepository();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setup() {
        formController = mock(FormController.class);
        startingIndex = new FormIndex(null, 0, 0, new TreeReference());
        when(formController.getFormIndex()).thenReturn(startingIndex);
        when(formController.getFormDef()).thenReturn(new FormDef());

        auditEventLogger = mock(AuditEventLogger.class);
        when(formController.getAuditEventLogger()).thenReturn(auditEventLogger);

        scheduler = new FakeScheduler();

        formSessionRepository.set("blah", formController);
        viewModel = new FormEntryViewModel(mock(Supplier.class), scheduler, formSessionRepository, "blah");
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

    @Test
    public void getQuestionPrompt_returnsPromptForIndex() {
        FormIndex formIndex = new FormIndex(null, 1, 1, new TreeReference());
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder().build();
        when(formController.getQuestionPrompt(formIndex)).thenReturn(prompt);

        assertThat(viewModel.getQuestionPrompt(formIndex), is(prompt));
    }

    @Test
    public void answerQuestion_callsAnswerListener() {
        FormEntryViewModel.AnswerListener answerListener = mock(FormEntryViewModel.AnswerListener.class);
        viewModel.setAnswerListener(answerListener);

        FormIndex index = new FormIndex(null, 1, 1, new TreeReference());
        StringData answer = new StringData("42");
        viewModel.answerQuestion(index, answer);
        verify(answerListener).onAnswer(index, answer);
    }

    @Test
    public void onCleared_removesAnswerListener() {
        FormEntryViewModel.AnswerListener answerListener = mock(FormEntryViewModel.AnswerListener.class);
        viewModel.setAnswerListener(answerListener);

        viewModel.onCleared();

        viewModel.answerQuestion(
                new FormIndex(null, 1, 1, new TreeReference()),
                new StringData("42")
        );
        verify(answerListener, never()).onAnswer(any(), any());
    }

    @Test
    public void updateAnswersForScreen_flushesAuditLoggerAfterSaving() throws Exception {
        viewModel.updateAnswersForScreen(new HashMap<>(), false);

        InOrder verifier = inOrder(formController, auditEventLogger);
        verifier.verify(formController).saveAllScreenAnswers(any(), anyBoolean());
        verifier.verify(auditEventLogger).flush();
    }

    @Test
    public void moveForward_savesAnswersToFormController_andThenStepsToNextEvent_andFlushesLogger() throws Exception {
        HashMap<FormIndex, IAnswerData> answers = new HashMap<>();
        viewModel.moveForward(answers);

        scheduler.runBackground();
        InOrder verifier = inOrder(formController, auditEventLogger);
        verifier.verify(formController).saveAllScreenAnswers(answers, false);
        verifier.verify(auditEventLogger).flush();
        verifier.verify(formController).stepToNextScreenEvent();
        verifier.verify(auditEventLogger).flush();
    }

    @Test
    public void moveForward_updatesIndexAfterSteppingToNextEvent() throws Exception {
        FormIndex nextIndex = new FormIndex(null, 1, 1, new TreeReference());
        when(formController.stepToNextScreenEvent()).thenAnswer((Answer<Integer>) invocation -> {
            when(formController.getFormIndex()).thenReturn(nextIndex);
            return 0;
        });

        viewModel.refresh();
        assertThat(getOrAwaitValue(viewModel.getCurrentIndex()), equalTo(startingIndex));

        viewModel.moveForward(new HashMap<>());
        scheduler.runBackground();
        assertThat(getOrAwaitValue(viewModel.getCurrentIndex()), equalTo(nextIndex));
    }

    @Test
    public void moveForward_whenThereIsAnErrorSteppingToNextEvent_setErrorWithMessage() throws Exception {
        when(formController.stepToNextScreenEvent()).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveForward(new HashMap<>());
        scheduler.runBackground();

        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("OH NO")));
    }

    @Test
    public void moveForward_whenThereIsAFailedConstraint_setsFailedConstraint() throws Exception {
        FailedConstraint failedConstraint = new FailedConstraint(startingIndex, 0);
        when(formController.saveAllScreenAnswers(any(), anyBoolean())).thenReturn(failedConstraint);

        viewModel.moveForward(new HashMap<>());
        scheduler.runBackground();

        assertThat(getOrAwaitValue(viewModel.getFailedConstraint()), equalTo(failedConstraint));
    }

    @Test
    public void moveForward_whenThereIsAFailedConstraint_doesNotStepToNextEvent() throws Exception {
        FailedConstraint failedConstraint = new FailedConstraint(startingIndex, 0);
        when(formController.saveAllScreenAnswers(any(), anyBoolean())).thenReturn(failedConstraint);

        viewModel.moveForward(new HashMap<>());
        scheduler.runBackground();

        verify(formController, never()).stepToNextScreenEvent();
    }

    @Test
    public void moveForward_whenThereIsAnErrorSaving_setsErrorWithMessage() throws Exception {
        when(formController.saveAllScreenAnswers(any(), anyBoolean())).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveForward(new HashMap<>());
        scheduler.runBackground();

        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("OH NO")));
    }

    @Test
    public void moveForward_whenThereIsAnErrorSaving_doesNotStepToNextEvent() throws Exception {
        when(formController.saveAllScreenAnswers(any(), anyBoolean())).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveForward(new HashMap<>());
        scheduler.runBackground();

        verify(formController, never()).stepToNextScreenEvent();
    }

    @Test
    public void moveForward_setsLoadingToTrueWhileBackgroundWorkHappens() throws Exception {
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(false));

        viewModel.moveForward(new HashMap<>());
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(true));

        scheduler.runBackground();
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(false));
    }

    @Test
    public void moveForward_whenEvaluateConstraintsIsTrue_savesAnswersWithEvaluateConstraintsTrue() throws Exception {
        HashMap<FormIndex, IAnswerData> answers = new HashMap<>();
        viewModel.moveForward(answers, true);

        scheduler.runBackground();
        verify(formController).saveAllScreenAnswers(answers, true);
    }

    @Test
    public void moveBackward_savesAnswersToFormController_andThenStepsToPreviousEvent_andFlushesLogger() throws Exception {
        HashMap<FormIndex, IAnswerData> answers = new HashMap<>();
        viewModel.moveBackward(answers);

        scheduler.runBackground();
        InOrder verifier = inOrder(formController, auditEventLogger);
        verifier.verify(formController).saveAllScreenAnswers(answers, false);
        verifier.verify(auditEventLogger).flush();
        verifier.verify(formController).stepToPreviousScreenEvent();
        verifier.verify(auditEventLogger).flush();
    }

    @Test
    public void moveBackward_updatesIndexAfterSteppingToPreviousEvent() throws Exception {
        FormIndex nextIndex = new FormIndex(null, 1, 1, new TreeReference());
        when(formController.stepToPreviousScreenEvent()).thenAnswer((Answer<Integer>) invocation -> {
            when(formController.getFormIndex()).thenReturn(nextIndex);
            return 0;
        });

        viewModel.refresh();
        assertThat(getOrAwaitValue(viewModel.getCurrentIndex()), equalTo(startingIndex));

        viewModel.moveBackward(new HashMap<>());
        scheduler.runBackground();
        assertThat(getOrAwaitValue(viewModel.getCurrentIndex()), equalTo(nextIndex));
    }

    @Test
    public void moveBackward_whenThereIsAnErrorSteppingToPreviousEvent_setErrorWithMessage() throws Exception {
        when(formController.stepToPreviousScreenEvent()).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveBackward(new HashMap<>());
        scheduler.runBackground();

        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("OH NO")));
    }

    @Test
    public void moveBackward_whenThereIsAnErrorSaving_setsErrorWithMessage() throws Exception {
        when(formController.saveAllScreenAnswers(any(), anyBoolean())).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveBackward(new HashMap<>());
        scheduler.runBackground();

        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("OH NO")));
    }

    @Test
    public void moveBackward_whenThereIsAnErrorSaving_doesNotStepToPreviousEvent() throws Exception {
        when(formController.saveAllScreenAnswers(any(), anyBoolean())).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveBackward(new HashMap<>());
        scheduler.runBackground();

        verify(formController, never()).stepToPreviousScreenEvent();
    }

    @Test
    public void moveBackward_setsLoadingToTrueWhileBackgroundWorkHappens() throws Exception {
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(false));

        viewModel.moveBackward(new HashMap<>());
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(true));

        scheduler.runBackground();
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(false));
    }
}
