package org.odk.collect.android.formentry.audit;

import android.net.Uri;

import androidx.lifecycle.LiveData;

import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.formentry.saving.FormSaver;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.tasks.SaveFormToDisk;
import org.odk.collect.android.tasks.SaveToDiskResult;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.javarosa.form.api.FormEntryController.EVENT_GROUP;
import static org.javarosa.form.api.FormEntryController.EVENT_QUESTION;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.formentry.saving.FormSaveViewModel.SaveResult.State.CHANGE_REASON_REQUIRED;
import static org.odk.collect.android.formentry.saving.FormSaveViewModel.SaveResult.State.CONSTRAINT_ERROR;
import static org.odk.collect.android.formentry.saving.FormSaveViewModel.SaveResult.State.FINALIZE_ERROR;
import static org.odk.collect.android.formentry.saving.FormSaveViewModel.SaveResult.State.SAVED;
import static org.odk.collect.android.formentry.saving.FormSaveViewModel.SaveResult.State.SAVE_ERROR;
import static org.odk.collect.android.formentry.saving.FormSaveViewModel.SaveResult.State.SAVING;

@RunWith(RobolectricTestRunner.class)
public class FormSaveViewModelTest {

    private static final long CURRENT_TIME = 123L;
    private AuditEventLogger logger;
    private FormSaveViewModel viewModel;
    private FormSaver formSaver;
    private FormController formController;

    @Before
    public void setup() {
        // Useful given some methods will execute AsyncTasks
        Robolectric.getBackgroundThreadScheduler().pause();

        formController = mock(FormController.class);
        logger = mock(AuditEventLogger.class);
        formSaver = mock(FormSaver.class);
        Analytics analytics = mock(Analytics.class);

        when(formController.getAuditEventLogger()).thenReturn(logger);
        when(logger.isChangeReasonRequired()).thenReturn(false);

        viewModel = new FormSaveViewModel(() -> CURRENT_TIME, formSaver, analytics);
        viewModel.setFormController(formController);
    }

    @Test
    public void saveForm_returnsSaveResult_inSavingState() {
        viewModel.saveForm(Uri.parse("file://form"), true, "", false);
        FormSaveViewModel.SaveResult saveResult1 = viewModel.getSaveResult().getValue();
        assertThat(saveResult1.getState(), equalTo(SAVING));
    }

    @Test
    public void saveForm_wontRunMultipleSavesAtOnce() {
        viewModel.saveForm(Uri.parse("file://form"), true, "", false);
        viewModel.saveForm(Uri.parse("file://form"), true, "", false);

        whenFormSaverFinishes(SaveFormToDisk.SAVED);
        verify(formSaver).save(any(), any(), anyBoolean(), anyBoolean(), any(), any(), any());

        Robolectric.getBackgroundThreadScheduler().advanceToLastPostedRunnable(); // Run any other queued tasks
        verify(formSaver, times(1)).save(any(), any(), anyBoolean(), anyBoolean(), any(), any(), any());
    }

    @Test
    public void saveForm_whenReasonRequiredToSave_returnsSaveResult_inChangeReasonRequiredState() {
        whenReasonRequiredToSave();

        LiveData<FormSaveViewModel.SaveResult> saveResult = viewModel.getSaveResult();
        viewModel.saveForm(Uri.parse("file://form"), true, "", false);
        assertThat(saveResult.getValue().getState(), equalTo(CHANGE_REASON_REQUIRED));
    }

    @Test
    public void whenFormSaverFinishes_saved_setsSaveResultState_toSaved() {
        viewModel.saveForm(Uri.parse("file://form"), true, "", false);
        LiveData<FormSaveViewModel.SaveResult> saveResult = viewModel.getSaveResult();

        whenFormSaverFinishes(SaveFormToDisk.SAVED);
        assertThat(saveResult.getValue().getState(), equalTo(SAVED));
    }

    @Test
    public void whenFormSaverFinishes_saved_andFormIsCurrentlyOnQuestion_logsSaveAndQuestionAuditEventsAfterFlush() {
        when(formController.getEvent()).thenReturn(EVENT_QUESTION);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withIndex("index1")
                .withAnswerDisplayText("answer")
                .build();
        when(formController.getQuestionPrompts()).thenReturn(Arrays.asList(prompt).toArray(new FormEntryPrompt[]{}));

        viewModel.saveForm(Uri.parse("file://form"), true, "", false);
        whenFormSaverFinishes(SaveFormToDisk.SAVED);

        InOrder verifier = inOrder(logger);
        verifier.verify(logger).flush();
        verifier.verify(logger).logEvent(
                AuditEvent.AuditEventType.FORM_SAVE,
                false,
                CURRENT_TIME
        );
        verifier.verify(logger).logEvent(
                AuditEvent.AuditEventType.QUESTION,
                prompt.getIndex(),
                true,
                prompt.getAnswerValue().getDisplayText(),
                CURRENT_TIME,
                null
        );
    }

    @Test
    public void whenFormSaverFinishes_saved_andFormIsCurrentlyOnGroup_logsSaveAndQuestionAuditEventsAfterFlush() {
        when(formController.getEvent()).thenReturn(EVENT_GROUP);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withIndex("index1")
                .withAnswerDisplayText("answer")
                .build();
        when(formController.getQuestionPrompts()).thenReturn(Arrays.asList(prompt).toArray(new FormEntryPrompt[]{}));

        viewModel.saveForm(Uri.parse("file://form"), true, "", false);
        whenFormSaverFinishes(SaveFormToDisk.SAVED);

        InOrder verifier = inOrder(logger);
        verifier.verify(logger).flush();
        verifier.verify(logger).logEvent(
                AuditEvent.AuditEventType.FORM_SAVE,
                false,
                CURRENT_TIME
        );
        verifier.verify(logger).logEvent(
                AuditEvent.AuditEventType.QUESTION,
                prompt.getIndex(),
                true,
                prompt.getAnswerValue().getDisplayText(),
                CURRENT_TIME,
                null
        );
    }

    @Test
    public void whenFormSaverFinishes_saved_andFormIsCurrentlyOnRepeat_logsSaveAndQuestionAuditEventsAfterFlush() {
        when(formController.getEvent()).thenReturn(EVENT_REPEAT);
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withIndex("index1")
                .withAnswerDisplayText("answer")
                .build();
        when(formController.getQuestionPrompts()).thenReturn(Arrays.asList(prompt).toArray(new FormEntryPrompt[]{}));

        viewModel.saveForm(Uri.parse("file://form"), true, "", false);
        whenFormSaverFinishes(SaveFormToDisk.SAVED);

        InOrder verifier = inOrder(logger);
        verifier.verify(logger).flush();
        verifier.verify(logger).logEvent(
                AuditEvent.AuditEventType.FORM_SAVE,
                false,
                CURRENT_TIME
        );
        verifier.verify(logger).logEvent(
                AuditEvent.AuditEventType.QUESTION,
                prompt.getIndex(),
                true,
                prompt.getAnswerValue().getDisplayText(),
                CURRENT_TIME,
                null
        );
    }

    @Test
    public void whenFormSaverFinishes_whenViewExiting_logsFormSaveAndFormExitAuditEventAfterFlush() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", true);

        whenFormSaverFinishes(SaveFormToDisk.SAVED);

        InOrder verifier = inOrder(logger);
        verifier.verify(logger).flush();
        verifier.verify(logger).logEvent(
                AuditEvent.AuditEventType.FORM_SAVE,
                false,
                CURRENT_TIME
        );
        verifier.verify(logger).logEvent(AuditEvent.AuditEventType.FORM_EXIT, true, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_whenFormComplete_andViewExiting_logsFormExitAndFinalizeAuditEventsAfterFlush() {
        viewModel.saveForm(Uri.parse("file://form"), true, "", true);

        whenFormSaverFinishes(SaveFormToDisk.SAVED);

        InOrder verifier = inOrder(logger);
        verifier.verify(logger).flush();
        verifier.verify(logger).logEvent(
                AuditEvent.AuditEventType.FORM_SAVE,
                false,
                CURRENT_TIME
        );
        verifier.verify(logger).logEvent(AuditEvent.AuditEventType.FORM_EXIT, false, CURRENT_TIME);
        verifier.verify(logger).logEvent(AuditEvent.AuditEventType.FORM_FINALIZE, true, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_savedAndExit_setsSaveResultState_toSaved() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);
        LiveData<FormSaveViewModel.SaveResult> saveResult = viewModel.getSaveResult();

        whenFormSaverFinishes(SaveFormToDisk.SAVED_AND_EXIT);
        assertThat(saveResult.getValue().getState(), equalTo(SAVED));
    }

    @Test
    public void whenFormSaverFinishes_saveError_setSaveResultState_toSaveErrorWithMessage() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);
        LiveData<FormSaveViewModel.SaveResult> saveResult = viewModel.getSaveResult();

        whenFormSaverFinishes(SaveFormToDisk.SAVE_ERROR, "OH NO");
        assertThat(saveResult.getValue().getState(), equalTo(SAVE_ERROR));
        assertThat(saveResult.getValue().getMessage(), equalTo("OH NO"));
    }

    @Test
    public void whenFormSaverFinishes_saveError_logsSaveErrorAuditEvenAfterFlush() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(SaveFormToDisk.SAVE_ERROR);

        InOrder verifier = inOrder(logger);
        verifier.verify(logger).flush();
        verifier.verify(logger).logEvent(AuditEvent.AuditEventType.SAVE_ERROR, true, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_encryptionError_setSaveResultState_toFinalizeErrorWithMessage() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);
        LiveData<FormSaveViewModel.SaveResult> saveResult = viewModel.getSaveResult();

        whenFormSaverFinishes(SaveFormToDisk.ENCRYPTION_ERROR, "OH NO");
        assertThat(saveResult.getValue().getState(), equalTo(FINALIZE_ERROR));
        assertThat(saveResult.getValue().getMessage(), equalTo("OH NO"));
    }

    @Test
    public void whenFormSaverFinishes_encryptionError_logsFinalizeErrorAuditEventAfterFlush() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(SaveFormToDisk.ENCRYPTION_ERROR);

        InOrder verifier = inOrder(logger);
        verifier.verify(logger).flush();
        verifier.verify(logger).logEvent(AuditEvent.AuditEventType.FINALIZE_ERROR, true, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_answerConstraintViolated_setSaveResultState_toConstraintError() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);
        LiveData<FormSaveViewModel.SaveResult> saveResult = viewModel.getSaveResult();

        whenFormSaverFinishes(FormEntryController.ANSWER_CONSTRAINT_VIOLATED);
        assertThat(saveResult.getValue().getState(), equalTo(CONSTRAINT_ERROR));
    }

    @Test
    public void whenFormSaverFinishes_answerConstraintViolated_finalizesAndLogsConstraintErrorAuditEvent() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(FormEntryController.ANSWER_CONSTRAINT_VIOLATED);

        InOrder verifier = inOrder(logger);
        verifier.verify(logger).flush();
        verifier.verify(logger).logEvent(AuditEvent.AuditEventType.CONSTRAINT_ERROR, true, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_answerRequiredButEmpty_setSaveResultState_toConstraintError() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);
        LiveData<FormSaveViewModel.SaveResult> saveResult = viewModel.getSaveResult();

        whenFormSaverFinishes(FormEntryController.ANSWER_REQUIRED_BUT_EMPTY);
        assertThat(saveResult.getValue().getState(), equalTo(CONSTRAINT_ERROR));
    }

    @Test
    public void whenFormSaverFinishes_isSaving_returnsFalse() {
        assertThat(viewModel.isSaving(), equalTo(false));

        viewModel.saveForm(Uri.parse("file://form"), false, "", false);
        assertThat(viewModel.isSaving(), equalTo(true));

        whenFormSaverFinishes(SaveFormToDisk.SAVED);
        assertThat(viewModel.isSaving(), equalTo(false));
    }

    @Test
    public void whenReasonRequiredToSave_saveReason_setsSaveResultState_toSaving() {
        whenReasonRequiredToSave();
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);
        LiveData<FormSaveViewModel.SaveResult> saveResult = viewModel.getSaveResult();

        viewModel.setReason("blah");
        viewModel.saveReason();
        assertThat(saveResult.getValue().getState(), equalTo(SAVING));
    }

    @Test
    public void saveReason_logsChangeReasonAuditEvent() {
        viewModel.setReason("Blah");
        viewModel.saveReason();

        verify(logger).logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, CURRENT_TIME, "Blah");
    }

    @Test
    public void saveReason_whenReasonIsValid_returnsTrue() {
        viewModel.setReason("Blah");
        assertThat(viewModel.saveReason(), equalTo(true));
    }

    @Test
    public void saveReason_whenReasonIsNotValid_returnsFalse() {
        viewModel.setReason("");
        assertThat(viewModel.saveReason(), equalTo(false));

        viewModel.setReason("  ");
        assertThat(viewModel.saveReason(), equalTo(false));
    }

    @Test
    public void resumeFormEntry_clearsSaveResult() {
        LiveData<FormSaveViewModel.SaveResult> saveResult = viewModel.getSaveResult();
        viewModel.saveForm(Uri.parse("file://form"), true, "", false);
        viewModel.resumeFormEntry();
        assertThat(saveResult.getValue(), equalTo(null));
    }

    private void whenReasonRequiredToSave() {
        when(logger.isChangeReasonRequired()).thenReturn(true);
        when(logger.isChangesMade()).thenReturn(true);
        when(logger.isEditing()).thenReturn(true);
    }

    private void whenFormSaverFinishes(int result) {
        whenFormSaverFinishes(result, null);
    }

    private void whenFormSaverFinishes(int result, String message) {
        SaveToDiskResult saveToDiskResult = new SaveToDiskResult();
        saveToDiskResult.setSaveResult(result, true);
        saveToDiskResult.setSaveErrorMessage(message);

        when(formSaver.save(any(), any(), anyBoolean(), anyBoolean(), any(), any(), any())).thenReturn(saveToDiskResult);
        Robolectric.getBackgroundThreadScheduler().runOneTask();
    }
}