package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class CancelRecordingDialog extends Page<CancelRecordingDialog> {

    private final String formName;

    CancelRecordingDialog(String formName, ActivityTestRule rule) {
        super(rule);
        this.formName = formName;
    }

    @Override
    public CancelRecordingDialog assertOnPage() {
        assertText(R.string.stop_recording_confirmation);
        return this;
    }

    public FormEntryPage clickOk() {
        clickOKOnDialog();
        return new FormEntryPage(formName, rule);
    }
}
