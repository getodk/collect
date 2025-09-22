package org.odk.collect.android.formentry.audit

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.javarosawrapper.FormController

@RunWith(AndroidJUnit4::class)
class IdentityPromptViewModelTest {
    @Test
    fun done_setsUserOnAuditEventLogger() {
        val formController = mock<FormController>()
        val auditEventLogger = mock<AuditEventLogger>()
        whenever(formController.getAuditEventLogger()).thenReturn(auditEventLogger)

        val viewModel = IdentityPromptViewModel()

        viewModel.formLoaded(formController)
        viewModel.setIdentity("Picard")
        viewModel.done()
        verify(auditEventLogger).user = "Picard"
    }
}
