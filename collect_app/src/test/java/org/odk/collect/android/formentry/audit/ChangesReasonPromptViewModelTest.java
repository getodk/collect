package org.odk.collect.android.formentry.audit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

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
        viewModel.save(123L);

        verify(logger).logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, 123L, "Blah");
    }
}