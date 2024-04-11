package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import org.odk.collect.strings.R.string;

public class GetBlankFormPage extends Page<GetBlankFormPage> {

    @Override
    public GetBlankFormPage assertOnPage() {
        onView(withText(getTranslatedString(string.get_forms))).check(matches(isDisplayed()));
        return this;
    }

    public FormsDownloadResultPage clickGetSelected() {
        onView(withText(getTranslatedString(string.download))).perform(click());
        return new FormsDownloadResultPage().assertOnPage();
    }

    public GetBlankFormPage clickClearAll() {
        clickOnString(string.clear_all);
        return this;
    }

    public GetBlankFormPage clickForm(String formName) {
        clickOnText(formName);
        return this;
    }
}
