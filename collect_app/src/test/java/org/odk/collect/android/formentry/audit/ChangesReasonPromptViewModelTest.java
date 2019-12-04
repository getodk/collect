package org.odk.collect.android.formentry.audit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ChangesReasonPromptViewModelTest {

    @Test
    public void save_logsChangeReasonAuditEvent() {
        AuditEventLogger logger = mock(AuditEventLogger.class);
        when(logger.isChangeReasonRequired()).thenReturn(true);

        ChangesReasonPromptViewModel viewModel = new ChangesReasonPromptViewModel();
        viewModel.setAuditEventLogger(logger);

        viewModel.setReason("Blah");
        viewModel.saveReason(123L);

        verify(logger).logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, 123L, "Blah");
    }

    @Test
    public void promptDismissed_sets_isChangeReasonRequired_false() {
        AuditEventLogger logger = mock(AuditEventLogger.class);
        when(logger.isChangeReasonRequired()).thenReturn(true);
        when(logger.isChangesMade()).thenReturn(true);
        when(logger.isEditing()).thenReturn(true);

        ChangesReasonPromptViewModel viewModel = new ChangesReasonPromptViewModel();
        viewModel.setAuditEventLogger(logger);
        viewModel.savingForm();
        assertThat(viewModel.requiresReasonToContinue().getValue(), equalTo(true));

        viewModel.promptDismissed();
        assertThat(viewModel.requiresReasonToContinue().getValue(), equalTo(false));
    }
}