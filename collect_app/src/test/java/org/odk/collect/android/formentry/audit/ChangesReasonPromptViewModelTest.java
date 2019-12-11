package org.odk.collect.android.formentry.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ChangesReasonPromptViewModelTest {

    private AuditEventLogger logger;

    @Before
    public void setup() {
        logger = mock(AuditEventLogger.class);
        when(logger.isChangeReasonRequired()).thenReturn(false);
    }

    @Test
    public void save_logsChangeReasonAuditEvent() {
        when(logger.isChangeReasonRequired()).thenReturn(true);

        ChangesReasonPromptViewModel viewModel = new ChangesReasonPromptViewModel();
        viewModel.setAuditEventLogger(logger);

        viewModel.setReason("Blah");
        viewModel.saveReason(123L);

        verify(logger).logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, 123L, "Blah");
    }

    @Test
    public void promptDismissed_sets_isChangeReasonRequired_false() {
        when(logger.isChangeReasonRequired()).thenReturn(true);
        when(logger.isChangesMade()).thenReturn(true);
        when(logger.isEditing()).thenReturn(true);

        ChangesReasonPromptViewModel viewModel = new ChangesReasonPromptViewModel();
        viewModel.setAuditEventLogger(logger);
        viewModel.saveForm(true, "", true);
        assertThat(viewModel.requiresReasonToContinue().getValue(), equalTo(true));

        viewModel.promptDismissed();
        assertThat(viewModel.requiresReasonToContinue().getValue(), equalTo(false));
    }

    @Test
    public void saveComplete_sets_saveRequest_null() {
        ChangesReasonPromptViewModel viewModel = new ChangesReasonPromptViewModel();
        viewModel.setAuditEventLogger(logger);
        viewModel.saveForm(true, "", true);
        assertThat(viewModel.saveRequest().getValue(), notNullValue());

        viewModel.saveComplete();
        assertThat(viewModel.saveRequest().getValue(), nullValue());
    }
}