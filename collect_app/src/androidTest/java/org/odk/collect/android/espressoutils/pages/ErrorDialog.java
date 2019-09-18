package org.odk.collect.android.espressoutils.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class ErrorDialog extends Page<ErrorDialog> {

    ErrorDialog(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public ErrorDialog assertOnPage() {
        checkIsStringDisplayed(R.string.error_occured);
        return this;
    }

    public <D extends Page<D>> D clickOK(D destination) {
        clickOnId(android.R.id.button1);
        return destination.assertOnPage();
    }
}
