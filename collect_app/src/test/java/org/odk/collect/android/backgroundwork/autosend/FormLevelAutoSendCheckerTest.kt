package org.odk.collect.android.backgroundwork.autosend

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormsRepository
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository

class FormLevelAutoSendCheckerTest {
    private val projectID = "projectID"
    private lateinit var formsRepositoryProvider: FormsRepositoryProvider
    private lateinit var formsRepository: FormsRepository
    private lateinit var formLevelAutoSendChecker: FormLevelAutoSendChecker

    @Before
    fun setup() {
        formsRepository = InMemFormsRepository()
        formsRepositoryProvider = mock<FormsRepositoryProvider>().also {
            whenever(it.get(projectID)).thenReturn(formsRepository)
        }
        formLevelAutoSendChecker = FormLevelAutoSendChecker(formsRepositoryProvider)
    }

    @Test
    fun `if there are no forms with autosend enabled return false`() {
        formsRepository.save(
            Form.Builder()
                .formId("form-1")
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .formFilePath(FormUtils.createXFormFile("2", "1").absolutePath)
                .build()
        )

        assertFalse(formLevelAutoSendChecker.isAutoSendEnabled(projectID))
    }

    @Test
    fun `if there is at least one form with autosend enabled return true`() {
        formsRepository.save(
            Form.Builder()
                .formId("form-1")
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .formFilePath(FormUtils.createXFormFile("2", "1").absolutePath)
                .autoSend("true")
                .build()
        )

        assertTrue(formLevelAutoSendChecker.isAutoSendEnabled(projectID))
    }
}
