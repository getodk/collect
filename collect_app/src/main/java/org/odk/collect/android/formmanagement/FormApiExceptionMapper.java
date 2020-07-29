package org.odk.collect.android.formmanagement;

import android.content.res.Resources;

import org.odk.collect.android.R;
import org.odk.collect.android.openrosa.api.FormApiException;

public class FormApiExceptionMapper {

    private final Resources resources;

    public FormApiExceptionMapper(Resources resources) {
        this.resources = resources;
    }

    public String getMessage(FormApiException exception) {
        if (exception.getType() == FormApiException.Type.UNKNOWN_HOST) {
            return resources.getString(R.string.unknown_host_error, exception.getServerUrl()) + " " + resources.getString(R.string.report_to_project_lead);
        }

        return resources.getString(R.string.report_to_project_lead);
    }
}
