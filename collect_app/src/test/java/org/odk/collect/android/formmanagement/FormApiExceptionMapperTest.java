package org.odk.collect.android.formmanagement;

import org.junit.Test;
import org.odk.collect.android.R;
import org.odk.collect.android.openrosa.api.FormApiException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.openrosa.api.FormApiException.Type.FETCH_ERROR;
import static org.odk.collect.android.openrosa.api.FormApiException.Type.UNKNOWN_HOST;

public class FormApiExceptionMapperTest {

    @Test
    public void fetchErrorType_returnsGenericMessage() {
        FormApiExceptionMapper mapper = new FormApiExceptionMapper();
        assertThat(mapper.getMessage(new FormApiException(FETCH_ERROR)), is(R.string.generic_network_error));
    }

    @Test
    public void unknownHostType_returnsUnknownHostMessage() {
        FormApiExceptionMapper mapper = new FormApiExceptionMapper();
        assertThat(mapper.getMessage(new FormApiException(UNKNOWN_HOST)), is(R.string.unknown_host_error));
    }
}