package org.odk.collect.android.support.pages;

public class ErrorDialog extends OkDialog {

    @Override
    public ErrorDialog assertOnPage() {
        super.assertOnPage();
        assertText(org.odk.collect.strings.R.string.error_occured);
        return this;
    }
}
