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

public class SendFinalizedFormPage extends Page<SendFinalizedFormPage> {

    @Override
    public SendFinalizedFormPage assertOnPage() {
        onView(allOf(withText(getTranslatedString(org.odk.collect.strings.R.string.send_data)), isDescendantOfA(withId(org.odk.collect.androidshared.R.id.toolbar)))).check(matches(isDisplayed()));
        return this;
    }

    public ViewFormPage clickOnForm(String formLabel) {
        clickOnText(formLabel);
        return new ViewFormPage(formLabel).assertOnPage();
    }

    public FormHierarchyPage clickOnFormToEdit(String formLabel) {
        clickOnText(formLabel);
        clickOKOnDialog();
        return new FormHierarchyPage(formLabel).assertOnPage();
    }

    public OkDialog clickSendSelected() {
        clickOnText(getTranslatedString(org.odk.collect.strings.R.string.send_selected_data));
        return new OkDialog();
    }

    public ServerAuthDialog clickSendSelectedWithAuthenticationError() {
        clickOnText(getTranslatedString(org.odk.collect.strings.R.string.send_selected_data));
        return new ServerAuthDialog().assertOnPage();
    }

    public SendFinalizedFormPage clickSelectAll() {
        clickOnString(org.odk.collect.strings.R.string.select_all);
        return this;
    }

    public SendFinalizedFormPage selectForm(int index) {
        onView(withIndex(withId(androidx.appcompat.R.id.checkbox), index)).perform(click());
        return this;
    }
}
