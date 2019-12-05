package org.odk.collect.android.formentry.audit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class IdentityPromptViewModelTest {

    @Test
    public void done_setsUserOnAuditEventLogger() {
        AuditEventLogger auditEventLogger = mock(AuditEventLogger.class);
        IdentityPromptViewModel viewModel = new IdentityPromptViewModel();

        viewModel.setAuditEventLogger(auditEventLogger);
        viewModel.setIdentity("Picard");
        viewModel.done();
        verify(auditEventLogger).setUser("Picard");
    }
}