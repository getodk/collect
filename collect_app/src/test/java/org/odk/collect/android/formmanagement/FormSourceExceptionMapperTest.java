package org.odk.collect.android.formmanagement;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.forms.FormSourceException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class FormSourceExceptionMapperTest {

    private Context context;
    private FormSourceExceptionMapper mapper;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        mapper = new FormSourceExceptionMapper(context);
    }

    @Test
    public void fetchError_returnsGenericMessage() {
        String expectedString = context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormSourceException.FetchError()), is(expectedString));
    }

    @Test
    public void unreachable_returnsUnknownHostMessage() {
        String expectedString = context.getString(R.string.unreachable_error, "http://unknown.com") + " " + context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormSourceException.Unreachable("http://unknown.com")), is(expectedString));
    }

    @Test
    public void securityError_returnsSecurityMessage() {
        String expectedString = context.getString(R.string.security_error, "http://unknown.com") + " " + context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormSourceException.SecurityError("http://unknown.com")), is(expectedString));
    }

    @Test
    public void serverError_returnsServerErrorMessage() {
        String expectedString = context.getString(R.string.server_error, "http://unknown.com", 500) + " " + context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormSourceException.ServerError(500, "http://unknown.com")), is(expectedString));
    }

    @Test
    public void parseError_returnsParserErrorMessage() {
        String expectedString = context.getString(R.string.invalid_response, "http://unknown.com") + " " + context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormSourceException.ParseError("http://unknown.com")), is(expectedString));
    }

    @Test
    public void serverNotOpenRosaError_returnsNotOpenRosaMessage() {
        String expectedString = "This server does not correctly implement the OpenRosa formList API. " + context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormSourceException.ServerNotOpenRosaError()), is(expectedString));
    }
}
