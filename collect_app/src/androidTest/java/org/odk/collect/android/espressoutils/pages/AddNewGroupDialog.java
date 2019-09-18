package org.odk.collect.android.espressoutils.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class AddNewGroupDialog extends Page<AddNewGroupDialog> {

    AddNewGroupDialog(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public AddNewGroupDialog assertOnPage() {
        checkIsStringDisplayed(R.string.entering_repeat_ask);
        return this;
    }

    public FormEntryPage clickOnAddGroup(FormEntryPage destination) {
        clickOnString(R.string.add_another);
        return destination;
    }
}
