package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.odk.collect.android.support.matchers.CustomMatchers.withIndex;

import org.odk.collect.android.R;

public class SendFinalizedFormPage extends Page<SendFinalizedFormPage> {

    @Override
    public SendFinalizedFormPage assertOnPage() {
        onView(allOf(withText(getTranslatedString(R.string.send_data)), isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()));
        return this;
    }

    public ViewFormPage clickOnForm(String formLabel) {
        clickOnText(formLabel);
        return new ViewFormPage(formLabel).assertOnPage();
    }

    public OkDialog clickSendSelected() {
        clickOnText(getTranslatedString(R.string.send_selected_data));
        return new OkDialog();
    }

    public ServerAuthDialog clickSendSelectedWithAuthenticationError() {
        clickOnText(getTranslatedString(R.string.send_selected_data));
        return new ServerAuthDialog().assertOnPage();
    }

    public SendFinalizedFormPage clickSelectAll() {
        clickOnString(R.string.select_all);
        return this;
    }

    public SendFinalizedFormPage selectForm(int index) {
        onView(withIndex(withId(R.id.checkbox), index)).perform(click());
        return this;
    }
}
