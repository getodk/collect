package org.odk.collect.android.formmanagement

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R

@RunWith(AndroidJUnit4::class)
class FormDownloadExceptionMapperTest {
    private lateinit var context: Context
    private lateinit var mapper: FormDownloadExceptionMapper

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mapper = FormDownloadExceptionMapper(context)
    }

    @Test
    fun formWithNoHashError_returnsFormWithNoHashErrorMessage() {
        val expectedString = context.getString(
            R.string.form_with_no_hash_error,
        ) + " " + context.getString(R.string.report_to_project_lead)
        assertThat(
            mapper.getMessage(FormDownloadException.FormWithNoHash()),
            `is`(expectedString)
        )
    }

    @Test
    fun formParsingError_returnsFormParsingErrorMessage() {
        val expectedString = context.getString(
            R.string.form_parsing_error,
        ) + " " + context.getString(R.string.report_to_project_lead)
        assertThat(
            mapper.getMessage(FormDownloadException.FormParsingError()),
            `is`(expectedString)
        )
    }

    @Test
    fun formSaveError_returnsFormSaveErrorMessage() {
        val expectedString = context.getString(
            R.string.form_save_disk_error,
        ) + " " + context.getString(R.string.report_to_project_lead)
        assertThat(
            mapper.getMessage(FormDownloadException.DiskError()),
            `is`(expectedString)
        )
    }

    @Test
    fun formWithInvalidSubmissionError_returnsFormInvalidSubmissionErrorMessage() {
        val expectedString = context.getString(
            R.string.form_with_invalid_submission_error,
        ) + " " + context.getString(R.string.report_to_project_lead)
        assertThat(
            mapper.getMessage(FormDownloadException.InvalidSubmission()),
            `is`(expectedString)
        )
    }
}
