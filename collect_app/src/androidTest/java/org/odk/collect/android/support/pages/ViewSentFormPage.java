package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;

import org.odk.collect.android.R;
import org.odk.collect.android.database.forms.DatabaseFormColumns;

public class ViewSentFormPage extends Page<ViewSentFormPage> {

    @Override
    public ViewSentFormPage assertOnPage() {
        assertToolbarTitle(R.string.view_sent_forms);
        return this;
    }

    public ViewFormPage clickOnForm(String formName) {
        onData(withRowString(DatabaseFormColumns.DISPLAY_NAME, formName)).perform(click());
        return new ViewFormPage(formName).assertOnPage();
    }
}
