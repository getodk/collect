package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import org.odk.collect.android.support.WaitFor;

import java.util.concurrent.Callable;

public class ChangesReasonPromptPage extends Page<ChangesReasonPromptPage> {

    private final String formName;

    public ChangesReasonPromptPage(String formName) {
        this.formName = formName;
    }

    @Override
    public ChangesReasonPromptPage assertOnPage() {
        closeSoftKeyboard(); // Might open before assertion has a chance to run

        assertToolbarTitle(formName);
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.reason_for_changes))).check(matches(isDisplayed()));
        return this;
    }

    public ChangesReasonPromptPage enterReason(String reason) {
        onView(withHint(getTranslatedString(org.odk.collect.strings.R.string.reason))).perform(replaceText(reason));
        return this;
    }

    public MainMenuPage clickSave() {
        clickOnString(org.odk.collect.strings.R.string.save);
        return new MainMenuPage().assertOnPage();
    }

    public <D extends Page<D>> D clickSave(D destination) {
        clickOnString(org.odk.collect.strings.R.string.save);

        // Make sure we wait for form saving to finish
        WaitFor.waitFor((Callable<Void>) () -> {
            assertTextDoesNotExist(org.odk.collect.strings.R.string.saving_form);
            return null;
        });

        return destination.assertOnPage();
    }

    public <D extends Page<D>> D pressClose(D destination) {
        onView(withContentDescription(getTranslatedString(org.odk.collect.strings.R.string.close))).perform(click());
        return destination.assertOnPage();
    }

    public ChangesReasonPromptPage clickSaveWithValidationError() {
        clickOnString(org.odk.collect.strings.R.string.save);
        return this.assertOnPage();
    }
}
