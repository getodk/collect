package org.odk.collect.android.formmanagement;

import org.odk.collect.android.R;
import org.odk.collect.android.openrosa.api.FormApiException;

public class FormApiExceptionMapper {

    public int getMessage(FormApiException exception) {
        return R.string.generic_network_error;
    }
}
