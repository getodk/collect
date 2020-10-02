package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class DeleteSelectedDialog extends Page<DeleteSelectedDialog> {

    private final int numberSelected;
    private final DeleteSavedFormPage destination;

    public DeleteSelectedDialog(int numberSelected, DeleteSavedFormPage destination, ActivityTestRule rule) {
        super(rule);
        this.numberSelected = numberSelected;
        this.destination = destination;
    }

    @Override
    public DeleteSelectedDialog assertOnPage() {
        assertText(getTranslatedString(R.string.delete_confirm, numberSelected));
        return this;
    }

    public DeleteSavedFormPage clickDeleteForms() {
        clickOnString(R.string.delete_yes);
        return destination.assertOnPage();
    }
}
