package org.odk.collect.android.formentry.audit;

import androidx.lifecycle.LiveData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.tasks.SaveResult;
import org.odk.collect.android.tasks.SaveToDiskTask;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.formentry.audit.FormEntryViewModel.SaveRequest.State.CHANGE_REASON_REQUIRED;
import static org.odk.collect.android.formentry.audit.FormEntryViewModel.SaveRequest.State.SAVED;
import static org.odk.collect.android.formentry.audit.FormEntryViewModel.SaveRequest.State.SAVING;

@RunWith(RobolectricTestRunner.class)
public class FormEntryViewModelTest {

    private AuditEventLogger logger;
    private FormEntryViewModel viewModel;

    @Before
    public void setup() {
        logger = mock(AuditEventLogger.class);
        when(logger.isChangeReasonRequired()).thenReturn(false);

        viewModel = new FormEntryViewModel();
        viewModel.setAuditEventLogger(logger);
    }

    @Test
    public void saveReason_logsChangeReasonAuditEvent() {
        when(logger.isChangeReasonRequired()).thenReturn(true);

        viewModel.setReason("Blah");
        viewModel.saveReason(123L);

        verify(logger).logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, 123L, "Blah");
    }

    @Test
    public void promptDismissed_sets_isChangeReasonRequired_false() {
        whenReasonRequiredToSave();

        viewModel.saveForm(true, "", false);
        assertThat(viewModel.requiresReasonToContinue().getValue(), equalTo(true));

        viewModel.promptDismissed();
        assertThat(viewModel.requiresReasonToContinue().getValue(), equalTo(false));
    }

    @Test
    public void saveForm_returnsSaveRequest_inSavingState() {
        LiveData<FormEntryViewModel.SaveRequest> saveRequest = viewModel.saveForm(true, "", false);
        assertThat(saveRequest.getValue().getState(), equalTo(SAVING));
    }

    @Test
    public void saveForm_whenReasonRequiredToSave_returnsSaveRequest_inChangeReasonRequiredState() {
        whenReasonRequiredToSave();

        LiveData<FormEntryViewModel.SaveRequest> saveRequest = viewModel.saveForm(true, "", false);
        assertThat(saveRequest.getValue().getState(), equalTo(CHANGE_REASON_REQUIRED));
    }

    @Test
    public void whenSaveToDiskFinishes_saved_setsSaveRequestState_toSaved() {
        LiveData<FormEntryViewModel.SaveRequest> saveRequest = viewModel.saveForm(true, "", false);

        whenSaveToDiskFinishes(SaveToDiskTask.SAVED, 0L);
        assertThat(saveRequest.getValue().getState(), equalTo(SAVED));
    }

    @Test
    public void whenSaveToDiskFinishes_saved_logsFormSavedAuditEvent() {
        viewModel.saveForm(true, "", false);

        whenSaveToDiskFinishes(SaveToDiskTask.SAVED, 123L);
        verify(logger).logEvent(AuditEvent.AuditEventType.FORM_SAVE, false, 123L);
    }

    @Test
    public void whenSaveToDiskFinishes_whenViewExiting_logsFormExitAuditEvent() {
        viewModel.saveForm(false, "", true);

        whenSaveToDiskFinishes(SaveToDiskTask.SAVED, 123L);
        verify(logger).logEvent(AuditEvent.AuditEventType.FORM_EXIT, true, 123L);
    }

    @Test
    public void whenSaveToDiskFinishes_whenFormComplete_andViewExiting_logsFormExitAndFinalizeAuditEvents() {
        viewModel.saveForm(true, "", true);

        whenSaveToDiskFinishes(SaveToDiskTask.SAVED, 123L);
        verify(logger).logEvent(AuditEvent.AuditEventType.FORM_EXIT, false, 123L);
        verify(logger).logEvent(AuditEvent.AuditEventType.FORM_FINALIZE, true, 123L);
    }

    @Test
    public void whenSaveToDiskFinishes_savedAndExit_setsSaveRequestState_toSaved() {
        LiveData<FormEntryViewModel.SaveRequest> saveRequest = viewModel.saveForm(false, "", false);

        whenSaveToDiskFinishes(SaveToDiskTask.SAVED_AND_EXIT, 0L);
        assertThat(saveRequest.getValue().getState(), equalTo(SAVED));
    }

    @Test
    public void whenReasonRequiredToSave_saveReason_setsSaveRequestState_toSaving() {
        whenReasonRequiredToSave();
        LiveData<FormEntryViewModel.SaveRequest> saveRequest = viewModel.saveForm(false, "", false);

        viewModel.setReason("blah");
        viewModel.saveReason(0L);
        assertThat(saveRequest.getValue().getState(), equalTo(SAVING));
    }

    private void whenReasonRequiredToSave() {
        when(logger.isChangeReasonRequired()).thenReturn(true);
        when(logger.isChangesMade()).thenReturn(true);
        when(logger.isEditing()).thenReturn(true);
    }

    private void whenSaveToDiskFinishes(int result, long l) {
        SaveResult saveResult = new SaveResult();
        saveResult.setSaveResult(result, true);
        viewModel.saveToDiskTaskComplete(saveResult, l);
    }
}