package org.odk.collect.android.formmanagement;

import android.content.Context;

import org.odk.collect.android.R;
import org.odk.collect.android.forms.FormSourceException;
import org.odk.collect.android.utilities.TranslationHandler;

public class FormSourceExceptionMapper {

    private final Context context;

    public FormSourceExceptionMapper(Context context) {
        this.context = context;
    }

    public String getMessage(FormSourceException exception) {
        if (exception instanceof FormSourceException.Unreachable) {
            return TranslationHandler.getString(context, R.string.unreachable_error, ((FormSourceException.Unreachable) exception).getServerUrl()) + " " + TranslationHandler.getString(context, R.string.report_to_project_lead);
        } else if (exception instanceof FormSourceException.SecurityError) {
            return TranslationHandler.getString(context, R.string.security_error, ((FormSourceException.SecurityError) exception).getServerUrl()) + " " + TranslationHandler.getString(context, R.string.report_to_project_lead);
        } else {
            return TranslationHandler.getString(context, R.string.report_to_project_lead);
        }
    }
}
