package org.odk.collect.android.utilities

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.upload.FormUploadException
import org.odk.collect.forms.instances.Instance

@RunWith(AndroidJUnit4::class)
class FormsUploadResultInterpreterTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private val instance1 = Instance.Builder()
        .formId("1")
        .formVersion("1")
        .displayName("Instance 1")
        .instanceFilePath("filepath1")
        .build()

    private val instance2 = Instance.Builder()
        .formId("2")
        .formVersion("2")
        .displayName("Instance 2")
        .instanceFilePath("filepath2")
        .build()

    private var resultWithoutErrors = mapOf<Instance, FormUploadException?>(
        instance1 to null,
        instance2 to null
    )

    private var resultWithOneError = mapOf<Instance, FormUploadException?>(
        instance1 to null,
        instance2 to FormUploadException("Something went wrong!")
    )

    @Test
    fun `When all forms uploaded successfully getFailures() should return an empty list`() {
        assertThat(FormsUploadResultInterpreter.getFailures(resultWithoutErrors, context).size, `is`(0))
    }

    @Test
    fun `When not all forms uploaded successfully getFailures() should return list of failures`() {
        assertThat(FormsUploadResultInterpreter.getFailures(resultWithOneError, context).size, `is`(1))
        assertThat(FormsUploadResultInterpreter.getFailures(resultWithOneError, context)[0].title, `is`("Instance 2"))
        assertThat(FormsUploadResultInterpreter.getFailures(resultWithOneError, context)[0].secondaryText, `is`(context.getString(R.string.form_details, "2", "2")))
        assertThat(FormsUploadResultInterpreter.getFailures(resultWithOneError, context)[0].supportingText, `is`("Something went wrong!"))
    }

    @Test
    fun `When all forms uploaded successfully getNumberOfFailures() should return zero`() {
        assertThat(FormsUploadResultInterpreter.getNumberOfFailures(resultWithoutErrors), `is`(0))
    }

    @Test
    fun `When not all forms uploaded successfully getNumberOfFailures() should return number of failures`() {
        assertThat(FormsUploadResultInterpreter.getNumberOfFailures(resultWithOneError), `is`(1))
    }

    @Test
    fun `When all forms uploaded successfully allFormsUploadedSuccessfully() should return true`() {
        assertThat(FormsUploadResultInterpreter.allFormsUploadedSuccessfully(resultWithoutErrors), `is`(true))
    }

    @Test
    fun `When not all forms uploaded successfully allFormsUploadedSuccessfully() should return false`() {
        assertThat(FormsUploadResultInterpreter.allFormsUploadedSuccessfully(resultWithOneError), `is`(false))
    }
}
