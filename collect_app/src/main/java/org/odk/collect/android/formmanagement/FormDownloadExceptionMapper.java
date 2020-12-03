package org.odk.collect.android.formmanagement;

import android.content.Context;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.TranslationHandler;

public class FormDownloadExceptionMapper {

    private final Context context;

    public FormDownloadExceptionMapper(Context context) {
        this.context = context;
    }

    public String getMessage(FormDownloadException exception) {
        switch (exception.getType()) {
            case DUPLICATE_FORMID_VERSION:
                // Not localized as we figure out how common this is and whether the wording is helpful
                return "You've already downloaded a form with the same ID and version but with different contents. " +
                        "Before downloading, please send all data you have collected with the existing form and delete the data and blank form.";
            case GENERIC:
            default:
                return TranslationHandler.getString(context, R.string.failure);
        }
    }
}
