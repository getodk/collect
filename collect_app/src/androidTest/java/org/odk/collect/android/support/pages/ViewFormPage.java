package org.odk.collect.android.support.pages;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;

public class ViewFormPage extends Page<ViewFormPage> {
    private final String formName;

    public ViewFormPage(String formName) {
        this.formName = formName;
    }

    @NonNull
    @Override
    public ViewFormPage assertOnPage() {
        assertToolbarTitle(formName);
        assertText(R.string.exit);
        return this;
    }
}
