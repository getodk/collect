package org.odk.collect.android.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.javarosa.core.model.Constants.CONTROL_SELECT_ONE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.external.FormUriActivityKt.FORM_ENTRY_TOKEN;
import static org.odk.collect.androidtest.LiveDataTestUtilsKt.getOrAwaitValue;
import static java.util.Arrays.asList;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.measure.Measure;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.audit.AuditEventLogger;
import org.odk.collect.android.formentry.support.InMemFormSessionRepository;
import org.odk.collect.android.javarosawrapper.FailedValidationResult;
import org.odk.collect.android.javarosawrapper.FakeFormController;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.utilities.ChangeLocks;
import org.odk.collect.androidshared.data.Consumable;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.formstest.InMemFormsRepository;
import org.odk.collect.shared.locks.BooleanChangeLock;
import org.odk.collect.testshared.FakeScheduler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class FormEntryViewModelTest {

    private FormEntryViewModel viewModel;
    private FakeFormController formController;
    private FormIndex startingIndex;
    private AuditEventLogger auditEventLogger;
    private FakeScheduler scheduler;
    private final Form form = new Form.Builder().formFilePath("blah").build();
    private final FormSessionRepository formSessionRepository = new InMemFormSessionRepository();
    private final FormsRepository formsRepository = new InMemFormsRepository();
    private final ChangeLocks changeLocks = new ChangeLocks(new BooleanChangeLock(), new BooleanChangeLock());

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setup() {
        startingIndex = new FormIndex(null, 0, 0, new TreeReference());
        auditEventLogger = mock(AuditEventLogger.class);
        formController = new FakeFormController(startingIndex, auditEventLogger);

        scheduler = new FakeScheduler();

        formSessionRepository.set("blah", formController, form);
        viewModel = new FormEntryViewModel(() -> 0L, scheduler, formSessionRepository, "blah", formsRepository, changeLocks);
    }

    @Test
    public void refresh_whenEventIsBeginningOfForm_stepsForwards() {
        formController.setCurrentEvent(FormEntryController.EVENT_BEGINNING_OF_FORM);

        viewModel.refresh();
        scheduler.flush();
        assertThat(formController.getStepPosition(), equalTo(1));
    }

    @Test
    public void refresh_whenEventIsBeginningOfForm_andThereIsAnErrorSteppingForward_setsError() {
        formController.setCurrentEvent(FormEntryController.EVENT_BEGINNING_OF_FORM);
        formController.setNextStepError(new JavaRosaException(new IOException("OH NO")));

        viewModel.refresh();
        scheduler.flush();
        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("OH NO")));
    }

    @Test
    public void addRepeat_stepsToNextScreenEvent() throws Exception {
        viewModel.addRepeat();
        scheduler.flush();
        assertThat(formController.getStepPosition(), equalTo(1));
    }

    @Test
    public void addRepeat_whenThereIsAnErrorCreatingRepeat_setsErrorWithMessage() {
        formController.setNewRepeatError(new RuntimeException(new IOException("OH NO")));

        viewModel.addRepeat();
        scheduler.flush();
        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("OH NO")));
    }

    @Test
    public void addRepeat_whenThereIsAnErrorCreatingRepeat_setsErrorWithoutCause() {
        RuntimeException runtimeException = mock(RuntimeException.class);
        when(runtimeException.getCause()).thenReturn(null);
        when(runtimeException.getMessage()).thenReturn("Unknown issue occurred while adding a new group");
        formController.setNewRepeatError(runtimeException);

        viewModel.addRepeat();
        scheduler.flush();

        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("Unknown issue occurred while adding a new group")));
    }

    @Test
    public void addRepeat_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithMessage() throws Exception {
        formController.setNextStepError(new JavaRosaException(new IOException("OH NO")));

        viewModel.addRepeat();
        scheduler.flush();
        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("OH NO")));
    }

    @Test
    public void addRepeat_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithoutCause() throws Exception {
        JavaRosaException javaRosaException = mock(JavaRosaException.class);
        when(javaRosaException.getCause()).thenReturn(null);
        when(javaRosaException.getMessage()).thenReturn("Unknown issue occurred while adding a new group");
        formController.setNextStepError(javaRosaException);

        viewModel.addRepeat();
        scheduler.flush();

        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("Unknown issue occurred while adding a new group")));
    }

    @Test
    public void cancelRepeatPrompt_afterPromptForNewRepeatAndAddRepeat_stepsToNextRatherThanJumpingBack() {
        FormIndex originalIndex = formController.getFormIndex();
        formController.setNextRepeatPrompt(new FormIndex(null, originalIndex.getLocalIndex() + 1, 0, new TreeReference()));

        viewModel.promptForNewRepeat();
        scheduler.flush();

        viewModel.addRepeat();
        scheduler.flush();

        FormIndex newIndex = new FormIndex(null, originalIndex.getLocalIndex() + 2, 0, new TreeReference());
        formController.jumpToIndex(newIndex);
        viewModel.cancelRepeatPrompt();
        scheduler.flush();
        assertThat(formController.getFormIndex(), equalTo(new FormIndex(null, newIndex.getLocalIndex() + 1, 0, new TreeReference())));
    }

    @Test
    public void cancelRepeatPrompt_afterPromptForNewRepeatAndCancelRepeatPrompt_stepsToNextRatherThanJumpingBack() {
        FormIndex originalIndex = formController.getFormIndex();
        formController.setNextRepeatPrompt(new FormIndex(null, originalIndex.getLocalIndex() + 1, 0, new TreeReference()));

        viewModel.promptForNewRepeat();
        scheduler.flush();

        viewModel.cancelRepeatPrompt();
        scheduler.flush();

        assertThat(formController.getFormIndex(), equalTo(originalIndex));

        FormIndex newIndex = new FormIndex(null, originalIndex.getLocalIndex() + 2, 0, new TreeReference());
        formController.jumpToIndex(newIndex);
        viewModel.cancelRepeatPrompt();
        scheduler.flush();
        assertThat(formController.getFormIndex(), equalTo(new FormIndex(null, newIndex.getLocalIndex() + 1, 0, new TreeReference())));
    }

    @Test
    public void cancelRepeatPrompt_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithMessage() {
        formController.setNextStepError(new JavaRosaException(new IOException("OH NO")));

        viewModel.cancelRepeatPrompt();
        scheduler.flush();
        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("OH NO")));
    }

    @Test
    public void getQuestionPrompt_returnsPromptForIndex() {
        FormIndex formIndex = new FormIndex(null, 1, 1, new TreeReference());
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder().build();
        formController.setPrompt(formIndex, prompt);

        assertThat(viewModel.getQuestionPrompt(formIndex), is(prompt));
    }

    @Test
    public void moveForward_whenThereIsAnErrorSteppingToNextEvent_setErrorWithMessage() {
        formController.setNextStepError(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveForward(new HashMap<>());
        scheduler.flush();

        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("OH NO")));
    }

    @Test
    public void moveForward_withEvaluateConstraints_whenThereIsAFailedConstraint_setsFailedConstraint() {
        Consumable<FailedValidationResult> failedValidationResult =
                new Consumable<>(new FailedValidationResult(startingIndex, 0, null, org.odk.collect.strings.R.string.invalid_answer_error));
        formController.setFailedConstraint(failedValidationResult.getValue());

        HashMap<FormIndex, IAnswerData> answers = new HashMap<>();
        answers.put(startingIndex, new StringData("answer"));
        viewModel.moveForward(answers, true);
        scheduler.flush();

        assertThat(getOrAwaitValue(viewModel.getValidationResult()), equalTo(failedValidationResult));
    }

    /**
     * We don't want to flush the log before answers are actually committed.
     */
    @Test
    public void moveForward_withEvaluateConstraints_whenThereIsAFailedConstraint_doesNotFlushAuditLog() throws Exception {
        FailedValidationResult failedValidationResult = new FailedValidationResult(startingIndex, 0, null, org.odk.collect.strings.R.string.invalid_answer_error);
        formController.setFailedConstraint(failedValidationResult);

        HashMap<FormIndex, IAnswerData> answers = new HashMap<>();
        answers.put(startingIndex, new StringData("answer"));
        viewModel.moveForward(answers, true);
        scheduler.flush();

        verify(auditEventLogger, never()).flush();
    }

    @Test
    public void moveForward_withEvaluateConstraints_whenThereIsAFailedConstraint_doesNotStepToNextEvent() throws Exception {
        FailedValidationResult failedValidationResult = new FailedValidationResult(startingIndex, 0, null, org.odk.collect.strings.R.string.invalid_answer_error);
        formController.setFailedConstraint(failedValidationResult);

        HashMap<FormIndex, IAnswerData> answers = new HashMap<>();
        answers.put(startingIndex, new StringData("answer"));
        viewModel.moveForward(answers, true);
        scheduler.flush();

        assertThat(formController.getStepPosition(), equalTo(0));
    }

    @Test
    public void moveForward_whenThereIsAnErrorSaving_setsErrorWithMessage() {
        formController.setSaveError(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveForward(new HashMap<>());
        scheduler.flush();

        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("OH NO")));
    }

    @Test
    public void moveForward_whenThereIsAnErrorSaving_doesNotStepToNextEvent() {
        formController.setSaveError(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveForward(new HashMap<>());
        scheduler.flush();

        assertThat(formController.getStepPosition(), equalTo(0));
    }

    @Test
    public void moveForward_setsLoadingToTrueWhileBackgroundWorkHappens() {
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(false));

        viewModel.moveForward(new HashMap<>());
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(true));

        scheduler.flush();
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(false));
    }

    @Test
    public void moveBackward_whenThereIsAnErrorSteppingToPreviousEvent_setErrorWithMessage() throws Exception {
        formController.setPreviousStepError(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveBackward(new HashMap<>());
        scheduler.flush();

        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("OH NO")));
    }

    @Test
    public void moveBackward_whenThereIsAnErrorSaving_setsErrorWithMessage() {
        formController.setSaveError(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveBackward(new HashMap<>());
        scheduler.flush();

        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("OH NO")));
    }

    @Test
    public void moveBackward_whenThereIsAnErrorSaving_doesNotStepToPreviousEvent() throws Exception {
        formController.setPreviousStepError((new JavaRosaException(new IOException("OH NO"))));

        viewModel.moveBackward(new HashMap<>());
        scheduler.flush();

        assertThat(formController.getStepPosition(), equalTo(0));
    }

    /**
     * We don't want to flush the log before answers are actually committed.
     */
    @Test
    public void moveBackward_whenThereIsAnErrorSaving_doesNotFlushAuditLog() throws Exception {
        formController.setSaveError(new JavaRosaException(new IOException("OH NO")));

        viewModel.moveBackward(new HashMap<>());
        scheduler.flush();

        verify(auditEventLogger, never()).flush();
    }

    @Test
    public void moveBackward_setsLoadingToTrueWhileBackgroundWorkHappens() throws Exception {
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(false));

        viewModel.moveBackward(new HashMap<>());
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(true));

        scheduler.flush();
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(false));
    }

    @Test
    public void validate_setsLoadingToTrueWhileBackgroundWorkHappens() {
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(false));

        viewModel.validateForm();
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(true));

        scheduler.flush();
        assertThat(getOrAwaitValue(viewModel.isLoading()), equalTo(false));
    }

    @Test
    public void validate_whenThereIsAnErrorValidating_setsError() {
        formController.setValidationError(new JavaRosaException(new IOException("OH NO")));

        viewModel.validateForm();
        scheduler.flush();
        assertThat(viewModel.getError().getValue(), equalTo(new FormError.NonFatal("OH NO")));
    }

    @Test
    public void refresh_whenThereIsASelectOnePrompt_preloadsSelectChoices() {
        formController.setCurrentEvent(FormEntryController.EVENT_QUESTION);

        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(CONTROL_SELECT_ONE)
                .build();
        formController.setQuestionPrompts(asList(prompt));

        int loadCount = Measure.withMeasure(asList("LoadSelectChoices"), () -> {
            viewModel.refresh();
            scheduler.runBackground();
        });
        assertThat(loadCount, equalTo(1));

        loadCount = Measure.withMeasure(asList("LoadSelectChoices"), () -> {
            scheduler.runForeground();
        });
        assertThat(loadCount, equalTo(0));

        loadCount = Measure.withMeasure(asList("LoadSelectChoices"), () -> {
            try {
                viewModel.loadSelectChoices(prompt);
            } catch (FileNotFoundException | XPathSyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        assertThat(loadCount, equalTo(0));
    }

    @Test
    public void isFormEditableAfterFinalization_returnsFalse_whenSubmissionProfileIsMissing() {
        assertThat(viewModel.isFormEditableAfterFinalization(), equalTo(false));
    }

    @Test
    public void isFormEditableAfterFinalization_returnsFalse_whenSubmissionProfileHasNoClientEditableAttribute() {
        SubmissionProfile submissionProfile = new SubmissionProfile(null, null, null, null, new HashMap<>());

        FormDef formDef = new FormDef();
        formDef.setDefaultSubmission(submissionProfile);
        formController.setFormDef(formDef);

        assertThat(viewModel.isFormEditableAfterFinalization(), equalTo(false));
    }

    @Test
    public void isFormEditableAfterFinalization_returnsFalse_whenClientEditableAttributeIsNotTrue() {
        HashMap<String, String> attributeMap = new HashMap<>();
        attributeMap.put("client-editable", "blah");

        SubmissionProfile submissionProfile = new SubmissionProfile(null, null, null, null, attributeMap);

        FormDef formDef = new FormDef();
        formDef.setDefaultSubmission(submissionProfile);
        formController.setFormDef(formDef);

        assertThat(viewModel.isFormEditableAfterFinalization(), equalTo(false));
    }

    @Test
    public void isFormEditableAfterFinalization_returnsTrue_whenClientEditableAttributeIsTrue() {
        HashMap<String, String> attributeMap = new HashMap<>();
        attributeMap.put("client-editable", "true");

        SubmissionProfile submissionProfile = new SubmissionProfile(null, null, null, null, attributeMap);

        FormDef formDef = new FormDef();
        formDef.setDefaultSubmission(submissionProfile);
        formController.setFormDef(formDef);

        assertThat(viewModel.isFormEditableAfterFinalization(), equalTo(true));
    }

    @Test
    public void exit_releasesFormsLock() {
        ((BooleanChangeLock) changeLocks.getFormsLock()).lock(FORM_ENTRY_TOKEN);

        viewModel.exit();
        assertThat(changeLocks.getFormsLock().tryLock(FORM_ENTRY_TOKEN), equalTo(true));
    }

    @Test
    public void answerQuestion_savesAnswerToFormController() {
        TreeReference reference = new TreeReference();
        reference.add("blah", TreeReference.INDEX_UNBOUND);
        FormIndex formIndex = new FormIndex(null, 1, 1, reference);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder().build();
        formController.setPrompt(formIndex, prompt);

        viewModel.answerQuestion(formIndex, new StringData("answer"));
        scheduler.flush(true);
        assertThat(formController.getAnswer(formIndex.getReference()).getValue(), equalTo("answer"));
    }

    @Test
    public void validateAnswerConstraint_updatesValidationResult() {
        FormDef formDef = mock();
        when(formDef.evaluateConstraint(any(), any())).thenReturn(false);
        formController.setFormDef(formDef);

        TreeReference reference = new TreeReference();
        reference.add("blah", TreeReference.INDEX_UNBOUND);
        FormIndex formIndex = new FormIndex(null, 1, 1, reference);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder().build();
        formController.setPrompt(formIndex, prompt);

        FailedValidationResult failedValidationResult = new FailedValidationResult(formIndex, 0, null, org.odk.collect.strings.R.string.invalid_answer_error);
        formController.setFailedConstraint(failedValidationResult);

        viewModel.validateAnswerConstraint(formIndex, new StringData("answer"));
        scheduler.flush(true);
        assertThat(viewModel.getValidationResult().getValue().getValue(), equalTo(failedValidationResult));
    }

    @Test
    public void answerQuestion_whenQuestionIsAutoAdvance_movesForward() {
        TreeReference reference = new TreeReference();
        reference.add("blah", TreeReference.INDEX_UNBOUND);
        FormIndex formIndex = new FormIndex(null, 1, 1, reference);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(CONTROL_SELECT_ONE)
                .withAppearance(Appearances.QUICK)
                .build();
        formController.setPrompt(formIndex, prompt);

        FormIndex originalIndex = formController.getFormIndex();
        viewModel.answerQuestion(formIndex, new StringData("answer"));
        scheduler.flush(true);
        assertThat(formController.getFormIndex(), equalTo(new FormIndex(null, originalIndex.getLocalIndex() + 1, 0, new TreeReference())));
    }

    @Test
    public void answerQuestion_whenQuestionIsAutoAdvance_andAnswerViolatesConstraint_setsFailedConstraint() {
        TreeReference reference = new TreeReference();
        reference.add("blah", TreeReference.INDEX_UNBOUND);
        FormIndex formIndex = new FormIndex(null, 1, 1, reference);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(CONTROL_SELECT_ONE)
                .withAppearance(Appearances.QUICK)
                .build();
        formController.setPrompt(formIndex, prompt);

        FailedValidationResult failedValidationResult = new FailedValidationResult(startingIndex, 0, null, org.odk.collect.strings.R.string.invalid_answer_error);
        formController.setFailedConstraint(failedValidationResult);

        FormIndex originalIndex = formController.getFormIndex();
        viewModel.answerQuestion(formIndex, new StringData("answer"));
        scheduler.flush(true);
        assertThat(getOrAwaitValue(viewModel.getCurrentIndex()).getThird(), equalTo(failedValidationResult));
        assertThat(formController.getFormIndex(), equalTo(new FormIndex(null, originalIndex.getLocalIndex(), 0, new TreeReference())));
    }

    @Test
    public void answerQuestion_setsQuestionIndexToUpdatedQuestionIndex() {
        TreeReference reference = new TreeReference();
        reference.add("blah", TreeReference.INDEX_UNBOUND);
        FormIndex formIndex = new FormIndex(null, 1, 1, reference);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .build();
        formController.setPrompt(formIndex, prompt);

        FormIndex originalIndex = formController.getFormIndex();
        viewModel.answerQuestion(formIndex, new StringData("answer"));
        scheduler.flush(true);
        assertThat(
                getOrAwaitValue(viewModel.getCurrentIndex()).getFirst(),
                equalTo(originalIndex)
        );
        assertThat(
                getOrAwaitValue(viewModel.getCurrentIndex()).getSecond(),
                equalTo(formIndex)
        );
    }

    @Test
    public void answerQuestion_whenQuestionIsAutoAdvance_setsScreenIndexToNextScreenIndex() {
        TreeReference reference = new TreeReference();
        reference.add("blah", TreeReference.INDEX_UNBOUND);
        FormIndex formIndex = new FormIndex(null, 1, 1, reference);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(CONTROL_SELECT_ONE)
                .withAppearance(Appearances.QUICK)
                .build();
        formController.setPrompt(formIndex, prompt);

        FormIndex originalIndex = formController.getFormIndex();
        viewModel.answerQuestion(formIndex, new StringData("answer"));
        scheduler.flush(true);
        assertThat(
                getOrAwaitValue(viewModel.getCurrentIndex()).getFirst(),
                equalTo(new FormIndex(null, originalIndex.getLocalIndex() + 1, 0, new TreeReference()))
        );
        assertThat(
                getOrAwaitValue(viewModel.getCurrentIndex()).getSecond(),
                equalTo(null)
        );
    }

    @Test
    public void answerQuestion_whenQuestionIsAutoAdvance_andAnswerViolatesConstraint_setsScreenIndexToCurrentScreenIndex() {
        TreeReference reference = new TreeReference();
        reference.add("blah", TreeReference.INDEX_UNBOUND);
        FormIndex formIndex = new FormIndex(null, 1, 1, reference);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withControlType(CONTROL_SELECT_ONE)
                .withAppearance(Appearances.QUICK)
                .build();
        formController.setPrompt(formIndex, prompt);

        FailedValidationResult failedValidationResult = new FailedValidationResult(startingIndex, 0, null, org.odk.collect.strings.R.string.invalid_answer_error);
        formController.setFailedConstraint(failedValidationResult);

        FormIndex originalIndex = formController.getFormIndex();
        viewModel.answerQuestion(formIndex, new StringData("answer"));
        scheduler.flush(true);
        assertThat(
                getOrAwaitValue(viewModel.getCurrentIndex()).getFirst(),
                equalTo(originalIndex)
        );
        assertThat(
                getOrAwaitValue(viewModel.getCurrentIndex()).getSecond(),
                equalTo(null)
        );
    }
}
