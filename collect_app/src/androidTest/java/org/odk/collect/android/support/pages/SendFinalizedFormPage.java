package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

public class SendFinalizedFormPage extends Page<SendFinalizedFormPage> {

    @Override
    public SendFinalizedFormPage assertOnPage() {
        onView(allOf(withText(getTranslatedString(R.string.send_data)), isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()));
        return this;
    }

    public SendFinalizedFormPage clickOnForm(String formLabel) {
        clickOnText(formLabel);
        return this;
    }

    public OkDialog clickSendSelected() {
        clickOnText(getTranslatedString(R.string.send_selected_data));
        return new OkDialog();
    }

    public ServerAuthDialog clickSendSelectedWithAuthenticationError() {
        clickOnText(getTranslatedString(R.string.send_selected_data));
        return new ServerAuthDialog().assertOnPage();
    }
}
