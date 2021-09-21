package org.odk.collect.android.formmanagement

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.forms.FormSourceException

@RunWith(AndroidJUnit4::class)
class FormSourceExceptionMapperTest {
    private lateinit var context: Context
    private lateinit var mapper: FormSourceExceptionMapper

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mapper = FormSourceExceptionMapper(context)
    }

    @Test
    fun fetchError_returnsGenericMessage() {
        val expectedString = context.getString(R.string.report_to_project_lead)
        assertThat(mapper.getMessage(FormSourceException.FetchError()), `is`(expectedString))
    }

    @Test
    fun unreachable_returnsUnknownHostMessage() {
        val expectedString = context.getString(
            R.string.unreachable_error,
            "http://unknown.com"
        ) + " " + context.getString(R.string.report_to_project_lead)
        assertThat(
            mapper.getMessage(FormSourceException.Unreachable("http://unknown.com")),
            `is`(expectedString)
        )
    }

    @Test
    fun securityError_returnsSecurityMessage() {
        val expectedString = context.getString(
            R.string.security_error,
            "http://unknown.com"
        ) + " " + context.getString(R.string.report_to_project_lead)
        assertThat(
            mapper.getMessage(FormSourceException.SecurityError("http://unknown.com")),
            `is`(expectedString)
        )
    }

    @Test
    fun serverError_returnsServerErrorMessage() {
        val expectedString = context.getString(
            R.string.server_error,
            "http://unknown.com",
            500
        ) + " " + context.getString(R.string.report_to_project_lead)
        assertThat(
            mapper.getMessage(
                FormSourceException.ServerError(
                    500,
                    "http://unknown.com"
                )
            ),
            `is`(expectedString)
        )
    }

    @Test
    fun parseError_returnsParserErrorMessage() {
        val expectedString = context.getString(
            R.string.invalid_response,
            "http://unknown.com"
        ) + " " + context.getString(R.string.report_to_project_lead)
        assertThat(
            mapper.getMessage(FormSourceException.ParseError("http://unknown.com")),
            `is`(expectedString)
        )
    }

    @Test
    fun serverNotOpenRosaError_returnsNotOpenRosaMessage() {
        val expectedString =
            "This server does not correctly implement the OpenRosa formList API. " + context.getString(
                R.string.report_to_project_lead
            )
        assertThat(
            mapper.getMessage(FormSourceException.ServerNotOpenRosaError()),
            `is`(expectedString)
        )
    }
}
