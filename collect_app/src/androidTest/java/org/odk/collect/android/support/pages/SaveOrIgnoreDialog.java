package org.odk.collect.android.support.pages;

import android.os.Build;

import org.odk.collect.android.R;

public class SaveOrIgnoreDialog<D extends Page<D>> extends Page<SaveOrIgnoreDialog<D>> {

    private final String formName;
    private final D destination;

    public SaveOrIgnoreDialog(String title, D destination) {
        this.formName = title;
        this.destination = destination;
    }

    @Override
    public SaveOrIgnoreDialog assertOnPage() {
        String title = getTranslatedString(R.string.exit) + " " + formName;
        assertText(title);
        return this;
    }

    public D clickSaveChanges() {
        clickOnString(R.string.keep_changes);
        return destination.assertOnPage();
    }

    public D clickSaveChangesWithError(int errorMsg) {
        clickOnString(R.string.keep_changes);

        if (Build.VERSION.SDK_INT < 30) {
            checkIsToastWithMessageDisplayed(errorMsg);
        } else {
            assertText(errorMsg);
            clickOKOnDialog();
        }

        return destination.assertOnPage();
    }

    public D clickIgnoreChanges() {
        clickOnString(R.string.do_not_save);
        return destination.assertOnPage();
    }
}
