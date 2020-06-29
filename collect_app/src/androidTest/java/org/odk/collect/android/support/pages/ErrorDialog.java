package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class ErrorDialog extends OkDialog {
    ErrorDialog(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public ErrorDialog assertOnPage() {
        super.assertOnPage();
        assertText(R.string.error_occured);
        return this;
    }
}
