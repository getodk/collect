package org.odk.collect.android.utilities

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.ServerFormDetails

@RunWith(AndroidJUnit4::class)
class FormsDownloadResultInterpreterTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val formsDownloadResultInterpreter = FormsDownloadResultInterpreter(context)
    private val formDetails1 = ServerFormDetails("Form 1", "", "1", "1", "", false, true, null)
    private val formDetails2 = ServerFormDetails("Form 2", "", "5", "4", "", false, true, null)

    private var resultWithoutErrors = mapOf(
        formDetails1 to context.getString(R.string.success),
        formDetails2 to context.getString(R.string.success)
    )

    private var resultWithOneError = mapOf(
        formDetails1 to context.getString(R.string.success),
        formDetails2 to "Exception"
    )

    @Test
    fun `When all forms downloaded successfully getFailures() should return an empty list`() {
        assertThat(formsDownloadResultInterpreter.getFailures(resultWithoutErrors).size, `is`(0))
    }

    @Test
    fun `When not all forms downloaded successfully getFailures() should return list of failures`() {
        assertThat(formsDownloadResultInterpreter.getFailures(resultWithOneError).size, `is`(1))
        assertThat(formsDownloadResultInterpreter.getFailures(resultWithOneError)[0].title, `is`("Form 2"))
        assertThat(formsDownloadResultInterpreter.getFailures(resultWithOneError)[0].secondaryText, `is`(context.getString(R.string.form_details, "5", "4")))
        assertThat(formsDownloadResultInterpreter.getFailures(resultWithOneError)[0].supportingText, `is`("Exception"))
    }

    @Test
    fun `When all forms downloaded successfully getNumberOfFailures() should return zero`() {
        assertThat(formsDownloadResultInterpreter.getNumberOfFailures(resultWithoutErrors), `is`(0))
    }

    @Test
    fun `When not all forms downloaded successfully getNumberOfFailures() should return number of failures`() {
        assertThat(formsDownloadResultInterpreter.getNumberOfFailures(resultWithOneError), `is`(1))
    }

    @Test
    fun `When all forms downloaded successfully allFormsDownloadedSuccessfully() should return true`() {
        assertThat(formsDownloadResultInterpreter.allFormsDownloadedSuccessfully(resultWithoutErrors), `is`(true))
    }

    @Test
    fun `When not all forms downloaded successfully allFormsDownloadedSuccessfully() should return false`() {
        assertThat(formsDownloadResultInterpreter.allFormsDownloadedSuccessfully(resultWithOneError), `is`(false))
    }
}
