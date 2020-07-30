package org.odk.collect.android.formmanagement;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.openrosa.api.FormApiException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.openrosa.api.FormApiException.Type.FETCH_ERROR;
import static org.odk.collect.android.openrosa.api.FormApiException.Type.UNKNOWN_HOST;

@RunWith(AndroidJUnit4.class)
public class FormApiExceptionMapperTest {

    private Context context;
    private FormApiExceptionMapper mapper;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        mapper = new FormApiExceptionMapper(context.getResources());
    }

    @Test
    public void fetchErrorType_returnsGenericMessage() {
        String expectedString = context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormApiException(FETCH_ERROR)), is(expectedString));
    }

    @Test
    public void unknownHostType_returnsUnknownHostMessage() {
        String expectedString = context.getString(R.string.unknown_host_error, "http://unknown.com") + " " + context.getString(R.string.report_to_project_lead);
        assertThat(mapper.getMessage(new FormApiException(UNKNOWN_HOST, "http://unknown.com")), is(expectedString));
    }
}