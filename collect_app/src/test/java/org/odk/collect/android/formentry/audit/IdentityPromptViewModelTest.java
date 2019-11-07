package org.odk.collect.android.formentry.audit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class IdentityPromptViewModelTest {

    @Test
    public void setIdentity_setsUserOnAuditEventLogger() {
        AuditEventLogger auditEventLogger = mock(AuditEventLogger.class);
        IdentityPromptViewModel viewModel = new IdentityPromptViewModel(auditEventLogger);

        viewModel.setIdentity("Picard");
        verify(auditEventLogger).setUser("Picard");
    }
}