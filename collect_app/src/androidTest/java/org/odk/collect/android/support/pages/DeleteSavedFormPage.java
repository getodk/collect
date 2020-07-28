package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;

public class DeleteSavedFormPage extends Page<DeleteSavedFormPage> {

    public DeleteSavedFormPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public DeleteSavedFormPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(R.string.manage_files));
        return this;
    }

    public DeleteSavedFormPage clickBlankForms() {
        clickOnString(R.string.forms);
        return this;
    }

    public DeleteSavedFormPage clickForm(String formName) {
        onData(withRowString(FormsProviderAPI.FormsColumns.DISPLAY_NAME, formName)).perform(click());
        return this;
    }

    public DeleteSelectedDialog clickDeleteSelected(int numberSelected) {
        clickOnString(R.string.delete_file);
        return new DeleteSelectedDialog(numberSelected, this, rule).assertOnPage();
    }
}
