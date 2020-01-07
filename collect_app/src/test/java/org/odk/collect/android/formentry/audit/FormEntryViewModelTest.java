package org.odk.collect.android.formentry.audit;

import android.net.Uri;

import androidx.lifecycle.LiveData;

import org.javarosa.form.api.FormEntryController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.FormSaver;
import org.odk.collect.android.tasks.SaveResult;
import org.odk.collect.android.tasks.SaveToDiskTask;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.formentry.FormEntryViewModel.SaveResult.State.CHANGE_REASON_REQUIRED;
import static org.odk.collect.android.formentry.FormEntryViewModel.SaveResult.State.CONSTRAINT_ERROR;
import static org.odk.collect.android.formentry.FormEntryViewModel.SaveResult.State.FINALIZE_ERROR;
import static org.odk.collect.android.formentry.FormEntryViewModel.SaveResult.State.SAVED;
import static org.odk.collect.android.formentry.FormEntryViewModel.SaveResult.State.SAVE_ERROR;
import static org.odk.collect.android.formentry.FormEntryViewModel.SaveResult.State.SAVING;

@RunWith(RobolectricTestRunner.class)
public class FormEntryViewModelTest {

    public static final long CURRENT_TIME = 123L;
    private AuditEventLogger logger;
    private FormEntryViewModel viewModel;
    private FormSaver formSaver;

    @Before
    public void setup() {
        // Useful given some methods will execute AsyncTasks
        Robolectric.getBackgroundThreadScheduler().pause();

        logger = mock(AuditEventLogger.class);
        formSaver = mock(FormSaver.class);

        when(logger.isChangeReasonRequired()).thenReturn(false);

        viewModel = new FormEntryViewModel(() -> CURRENT_TIME, formSaver);
        viewModel.setAuditEventLogger(logger);
    }

    @Test
    public void saveReason_logsChangeReasonAuditEvent() {
        viewModel.setReason("Blah");
        viewModel.saveReason(CURRENT_TIME);

        verify(logger).logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, CURRENT_TIME, "Blah");
    }

    @Test
    public void saveReason_whenReasonIsValid_returnsTrue() {
        viewModel.setReason("Blah");
        assertThat(viewModel.saveReason(CURRENT_TIME), equalTo(true));
    }

    @Test
    public void saveReason_whenReasonIsNotValid_returnsFalse() {
        viewModel.setReason("");
        assertThat(viewModel.saveReason(CURRENT_TIME), equalTo(false));

        viewModel.setReason("  ");
        assertThat(viewModel.saveReason(CURRENT_TIME), equalTo(false));
    }

    @Test
    public void saveForm_returnsSaveResult_inSavingState() {
        LiveData<FormEntryViewModel.SaveResult> saveResult = viewModel.saveForm(Uri.parse("file://form"), true, "", false);
        assertThat(saveResult.getValue().getState(), equalTo(SAVING));
    }

    @Test
    public void saveForm_whenReasonRequiredToSave_returnsSaveResult_inChangeReasonRequiredState() {
        whenReasonRequiredToSave();

        LiveData<FormEntryViewModel.SaveResult> saveResult = viewModel.saveForm(Uri.parse("file://form"), true, "", false);
        assertThat(saveResult.getValue().getState(), equalTo(CHANGE_REASON_REQUIRED));
    }

    @Test
    public void whenFormSaverFinishes_saved_setsSaveResultState_toSaved() {
        LiveData<FormEntryViewModel.SaveResult> saveResult = viewModel.saveForm(Uri.parse("file://form"), true, "", false);

        whenFormSaverFinishes(SaveToDiskTask.SAVED);
        assertThat(saveResult.getValue().getState(), equalTo(SAVED));
    }

    @Test
    public void whenFormSaverFinishes_saved_logsFormSavedAuditEvent() {
        viewModel.saveForm(Uri.parse("file://form"), true, "", false);

        whenFormSaverFinishes(SaveToDiskTask.SAVED);
        verify(logger).logEvent(AuditEvent.AuditEventType.FORM_SAVE, false, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_whenViewExiting_logsFormExitAuditEvent() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", true);

        whenFormSaverFinishes(SaveToDiskTask.SAVED);
        verify(logger).logEvent(AuditEvent.AuditEventType.FORM_EXIT, true, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_whenFormComplete_andViewExiting_logsFormExitAndFinalizeAuditEvents() {
        viewModel.saveForm(Uri.parse("file://form"), true, "", true);

        whenFormSaverFinishes(SaveToDiskTask.SAVED);
        verify(logger).logEvent(AuditEvent.AuditEventType.FORM_EXIT, false, CURRENT_TIME);
        verify(logger).logEvent(AuditEvent.AuditEventType.FORM_FINALIZE, true, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_savedAndExit_setsSaveResultState_toSaved() {
        LiveData<FormEntryViewModel.SaveResult> saveResult = viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(SaveToDiskTask.SAVED_AND_EXIT);
        assertThat(saveResult.getValue().getState(), equalTo(SAVED));
    }

    @Test
    public void whenFormSaverFinishes_saveError_setSaveResultState_toSaveErrorWithMessage() {
        LiveData<FormEntryViewModel.SaveResult> saveResult = viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(SaveToDiskTask.SAVE_ERROR, "OH NO");
        assertThat(saveResult.getValue().getState(), equalTo(SAVE_ERROR));
        assertThat(saveResult.getValue().getMessage(), equalTo("OH NO"));
    }

    @Test
    public void whenFormSaverFinishes_saveError_logsSaveErrorAuditEvent() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(SaveToDiskTask.SAVE_ERROR);
        verify(logger).logEvent(AuditEvent.AuditEventType.SAVE_ERROR, true, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_encryptionError_setSaveResultState_toFinalizeErrorWithMessage() {
        LiveData<FormEntryViewModel.SaveResult> saveResult = viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(SaveToDiskTask.ENCRYPTION_ERROR, "OH NO");
        assertThat(saveResult.getValue().getState(), equalTo(FINALIZE_ERROR));
        assertThat(saveResult.getValue().getMessage(), equalTo("OH NO"));
    }

    @Test
    public void whenFormSaverFinishes_encryptionError_logsFinalizeErrorAuditEvent() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(SaveToDiskTask.ENCRYPTION_ERROR);
        verify(logger).logEvent(AuditEvent.AuditEventType.FINALIZE_ERROR, true, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_answerConstraintViolated_setSaveResultState_toConstraintError() {
        LiveData<FormEntryViewModel.SaveResult> saveResult = viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(FormEntryController.ANSWER_CONSTRAINT_VIOLATED);
        assertThat(saveResult.getValue().getState(), equalTo(CONSTRAINT_ERROR));
    }

    @Test
    public void whenFormSaverFinishes_answerConstraintViolated_finalizesAndLogsConstraintErrorAuditEvent() {
        viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(FormEntryController.ANSWER_CONSTRAINT_VIOLATED);

        InOrder verifier = inOrder(logger);
        verifier.verify(logger).exitView();
        verifier.verify(logger).logEvent(AuditEvent.AuditEventType.CONSTRAINT_ERROR, true, CURRENT_TIME);
    }

    @Test
    public void whenFormSaverFinishes_answerRequiredButEmpty_setSaveResultState_toConstraintError() {
        LiveData<FormEntryViewModel.SaveResult> saveResult = viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        whenFormSaverFinishes(FormEntryController.ANSWER_REQUIRED_BUT_EMPTY);
        assertThat(saveResult.getValue().getState(), equalTo(CONSTRAINT_ERROR));
    }

    @Test
    public void whenFormSaverFinishes_isSaving_returnsFalse() {
        assertThat(viewModel.isSaving(), equalTo(false));

        viewModel.saveForm(Uri.parse("file://form"), false, "", false);
        assertThat(viewModel.isSaving(), equalTo(true));

        whenFormSaverFinishes(SaveToDiskTask.SAVED);
        assertThat(viewModel.isSaving(), equalTo(false));
    }

    @Test
    public void whenReasonRequiredToSave_saveReason_setsSaveResultState_toSaving() {
        whenReasonRequiredToSave();
        LiveData<FormEntryViewModel.SaveResult> saveResult = viewModel.saveForm(Uri.parse("file://form"), false, "", false);

        viewModel.setReason("blah");
        viewModel.saveReason(CURRENT_TIME);
        assertThat(saveResult.getValue().getState(), equalTo(SAVING));
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
        SaveResult saveResult = new SaveResult();
        saveResult.setSaveResult(result, true);
        saveResult.setSaveErrorMessage(message);

        when(formSaver.save(any(), anyBoolean(), any(), anyBoolean(), any())).thenReturn(saveResult);
        Robolectric.getBackgroundThreadScheduler().advanceToLastPostedRunnable();
    }
}