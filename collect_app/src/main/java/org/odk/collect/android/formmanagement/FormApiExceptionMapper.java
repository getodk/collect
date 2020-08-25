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
        switch (exception.getType()) {
            case UNREACHABLE:
                return resources.getString(R.string.unknown_host_error, exception.getServerUrl()) + " " + resources.getString(R.string.report_to_project_lead);
            case SECURITY_ERROR:
                return resources.getString(R.string.security_error, exception.getServerUrl()) + " " + resources.getString(R.string.report_to_project_lead);
            default:
                return resources.getString(R.string.report_to_project_lead);
        }
    }
}
