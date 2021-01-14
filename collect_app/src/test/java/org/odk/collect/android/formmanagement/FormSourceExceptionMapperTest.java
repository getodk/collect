package org.odk.collect.android.formmanagement;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.forms.FormSourceException;

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
    public void fetchErrorType_returnsGenericMessage() {
        String expectedString = context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormSourceException.FetchError()), is(expectedString));
    }

    @Test
    public void unreachableType_returnsUnknownHostMessage() {
        String expectedString = context.getString(R.string.unreachable_error, "http://unknown.com") + " " + context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormSourceException.Unreachable("http://unknown.com")), is(expectedString));
    }

    @Test
    public void securityErrorType_returnsSecurityMessage() {
        String expectedString = context.getString(R.string.security_error, "http://unknown.com") + " " + context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormSourceException.SecurityError("http://unknown.com")), is(expectedString));
    }
}
