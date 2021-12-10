package org.odk.collect.android.utilities

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.FormDownloadException
import org.odk.collect.android.formmanagement.FormDownloadExceptionMapper
import org.odk.collect.android.formmanagement.ServerFormDetails

@RunWith(AndroidJUnit4::class)
class FormsDownloadResultInterpreterTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val formDetails1 = ServerFormDetails("Form 1", "", "1", "1", "", false, true, null)
    private val formDetails2 = ServerFormDetails("Form 2", "", "5", "4", "", false, true, null)

    private var resultWithoutErrors = mapOf<ServerFormDetails, FormDownloadException?>(
        formDetails1 to null,
        formDetails2 to null
    )

    private var resultWithOneError = mapOf<ServerFormDetails, FormDownloadException?>(
        formDetails1 to null,
        formDetails2 to FormDownloadException.FormParsingError()
    )

    @Test
    fun `When all forms downloaded successfully getFailures() should return an empty list`() {
        assertThat(FormsDownloadResultInterpreter.getFailures(resultWithoutErrors, context).size, `is`(0))
    }

    @Test
    fun `When not all forms downloaded successfully getFailures() should return list of failures`() {
        assertThat(FormsDownloadResultInterpreter.getFailures(resultWithOneError, context).size, `is`(1))
        assertThat(FormsDownloadResultInterpreter.getFailures(resultWithOneError, context)[0].title, `is`("Form 2"))
        assertThat(FormsDownloadResultInterpreter.getFailures(resultWithOneError, context)[0].secondaryText, `is`(context.getString(R.string.form_details, "5", "4")))
        assertThat(FormsDownloadResultInterpreter.getFailures(resultWithOneError, context)[0].supportingText, `is`(FormDownloadExceptionMapper(context).getMessage(resultWithOneError[formDetails2])))
    }

    @Test
    fun `When all forms downloaded successfully getNumberOfFailures() should return zero`() {
        assertThat(FormsDownloadResultInterpreter.getNumberOfFailures(resultWithoutErrors, context), `is`(0))
    }

    @Test
    fun `When not all forms downloaded successfully getNumberOfFailures() should return number of failures`() {
        assertThat(FormsDownloadResultInterpreter.getNumberOfFailures(resultWithOneError, context), `is`(1))
    }

    @Test
    fun `When all forms downloaded successfully allFormsDownloadedSuccessfully() should return true`() {
        assertThat(FormsDownloadResultInterpreter.allFormsDownloadedSuccessfully(resultWithoutErrors, context), `is`(true))
    }

    @Test
    fun `When not all forms downloaded successfully allFormsDownloadedSuccessfully() should return false`() {
        assertThat(FormsDownloadResultInterpreter.allFormsDownloadedSuccessfully(resultWithOneError, context), `is`(false))
    }
}
