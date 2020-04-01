package org.odk.collect.android.formentry.audit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.javarosawrapper.FormController;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class IdentityPromptViewModelTest {

    @Test
    public void done_setsUserOnAuditEventLogger() {
        FormController formController = mock(FormController.class);
        AuditEventLogger auditEventLogger = mock(AuditEventLogger.class);
        when(formController.getAuditEventLogger()).thenReturn(auditEventLogger);

        IdentityPromptViewModel viewModel = new IdentityPromptViewModel();

        viewModel.formLoaded(formController);
        viewModel.setIdentity("Picard");
        viewModel.done();
        verify(auditEventLogger).setUser("Picard");
    }
}