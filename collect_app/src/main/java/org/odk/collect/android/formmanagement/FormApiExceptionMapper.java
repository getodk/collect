package org.odk.collect.android.formmanagement;

import android.content.Context;

import org.odk.collect.android.R;
import org.odk.collect.android.openrosa.api.FormApiException;

public class FormApiExceptionMapper {

    private final Context context;

    public FormApiExceptionMapper(Context context) {
        this.context = context;
    }

    public String getMessage(FormApiException exception) {
        if (exception.getType() == FormApiException.Type.UNKNOWN_HOST) {
            return context.getString(R.string.unknown_host_error);
        }

        return context.getString(R.string.generic_network_error);
    }
}
