package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

public class ErrorDialog extends OkDialog {

    @Override
    public ErrorDialog assertOnPage() {
        super.assertOnPage();
        assertText(R.string.error_occured);
        return this;
    }
}
